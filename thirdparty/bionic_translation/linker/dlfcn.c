/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include <dlfcn.h>
#include <gelf.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <time.h>

#include "config.h"
#include "linker.h"
#include "linker_format.h"

#include "../wrapper/verbose.h"
#include "../wrapper/wrapper.h"
#include "linker_debug.h"

#include "dlfcn.h"

/* This file hijacks the symbols stubbed out in libdl.so. */

#define DL_SUCCESS		      0
#define DL_ERR_CANNOT_LOAD_LIBRARY    1
#define DL_ERR_INVALID_LIBRARY_HANDLE 2
#define DL_ERR_BAD_SYMBOL_NAME	      3
#define DL_ERR_SYMBOL_NOT_FOUND	      4
#define DL_ERR_SYMBOL_NOT_GLOBAL      5

static char dl_err_buf[1024];
static const char *dl_err_str;

static const char *dl_errors[] = {
    [DL_ERR_CANNOT_LOAD_LIBRARY] = "Cannot load library",
    [DL_ERR_INVALID_LIBRARY_HANDLE] = "Invalid library handle",
    [DL_ERR_BAD_SYMBOL_NAME] = "Invalid symbol name",
    [DL_ERR_SYMBOL_NOT_FOUND] = "Symbol not found",
    [DL_ERR_SYMBOL_NOT_GLOBAL] = "Symbol is not global",
};

static pthread_mutex_t apkenv_dl_lock = PTHREAD_MUTEX_INITIALIZER;

static void set_dlerror(int err)
{
	format_buffer(dl_err_buf, sizeof(dl_err_buf), "%s: %s", dl_errors[err],
		      apkenv_linker_get_error());
	dl_err_str = (const char *)&dl_err_buf[0];
}

void *bionic_dlopen(const char *filename, int flag)
{
//	verbose("%s (%d)", filename, flag);
	soinfo *ret;
	pthread_mutex_lock(&apkenv_dl_lock);
	void *glibc_handle = NULL;
	ret = apkenv_find_library(filename, true, flag, &glibc_handle); // flag only used for glibc dlopen

	if (ret) {
		apkenv_call_constructors_recursive(ret);
		ret->refcount++;
	} else if (glibc_handle) {
		ret = glibc_handle;
	} else {
		set_dlerror(DL_ERR_CANNOT_LOAD_LIBRARY);
	}
	pthread_mutex_unlock(&apkenv_dl_lock);
	return ret;
}

struct android_dlextinfo {
	uint64_t flags;
	void*   reserved_addr;
	size_t  reserved_size;
	int     relro_fd;
	int     library_fd;
	off64_t library_fd_offset;
	struct android_namespace_t* library_namespace;
};

void *bionic_android_dlopen_ext(const char *filename, int flags, const struct android_dlextinfo *info)
{
	if (info) {
		fprintf(stderr, "ERROR: android_dlopen_ext with android_dlextinfo not implemented\n");
	}
	return bionic_dlopen(filename, flags);
}

const char *bionic_dlerror(void)
{
	const char *tmp = dl_err_str;
	dl_err_str = NULL;
	return (const char *)tmp;
}

enum {
	WRAPPER_DYNHOOK,
};

#define MIN(a, b) (((a) < (b)) ? (a) : (b))

void *bionic_dlsym(void *handle, const char *symbol)
{
	verbose("bionic_dlsym(%p, %s) called\n", handle, symbol);

	soinfo *found;
	ElfW(Sym) *sym;
	unsigned bind;

	pthread_mutex_lock(&apkenv_dl_lock);

	if (unlikely(symbol == 0)) {
		set_dlerror(DL_ERR_BAD_SYMBOL_NAME);
		goto err;
	}

	char wrap_sym_name[1024] = {'b', 'i', 'o', 'n', 'i', 'c', '_'};
	memcpy(wrap_sym_name + 7, symbol, MIN(sizeof(wrap_sym_name) - 7, strlen(symbol)));

	/* technically for RTLD_NEXT / RTLD_DEFAULT we don't know, but it will be dealt with later */
	bool is_this_our_handle = (handle == RTLD_NEXT || handle == RTLD_DEFAULT);
	if(!is_this_our_handle)
		is_this_our_handle = do_we_have_this_handle(handle);

	if (!is_this_our_handle) { // if the handle is not our handle, we can probably just try calling glibc dlsym
		if ((sym = dlsym(RTLD_DEFAULT, wrap_sym_name))) { // TODO: this is not ideal, we should probably translate all android system libary names to ..._android.so.0 and have those either be symlinks or small libs which depend on the actual lib and in addition implement bionic_ overrides
			pthread_mutex_unlock(&apkenv_dl_lock);
			verbose("system dlopen handle: found bionic_ version");
			return wrapper_create(symbol, sym);
		} else if ((sym = dlsym(handle, symbol))) {
			pthread_mutex_unlock(&apkenv_dl_lock);
			verbose("system dlopen handle: found system version");
			return wrapper_create(symbol, sym);
		}

		/* it's not our handle, so unless it's a special value might as well bail now */
		set_dlerror(DL_ERR_SYMBOL_NOT_FOUND);
	}

	/* allow overriding stuff even when the app specifies a particular .so; TODO: we should probably not do this */
	if ((sym = dlsym(RTLD_DEFAULT, wrap_sym_name))) {
		pthread_mutex_unlock(&apkenv_dl_lock);
		verbose("RTLD_DEFAULT: found bionic_ version");
		return wrapper_create(symbol, sym);
	} else {
		verbose("RTLD_DEFAULT: haven't found bionic_ nor system version; dlerror: >%s<", dlerror());
	}

	if (handle == RTLD_DEFAULT) {
		sym = apkenv_lookup(symbol, &found, NULL);
	} else if (handle == RTLD_NEXT) {
		void *ret_addr = __builtin_return_address(0);
		soinfo *si = apkenv_find_containing_library(ret_addr);

		sym = NULL;
		if (si && si->next) {
			sym = apkenv_lookup(symbol, &found, si->next);
		}
	} else if (is_this_our_handle) {
		found = (soinfo *)handle;
		sym = apkenv_lookup_in_library(found, symbol);
	} else {
		sym = 0;
	}

	if (likely(sym != 0)) {
		bind = ELF32_ST_BIND(sym->st_info);

		if (likely((bind == STB_GLOBAL) && (sym->st_shndx != 0))) {
			intptr_t ret = sym->st_value + found->base;
			pthread_mutex_unlock(&apkenv_dl_lock);
			return wrapper_create((char *)symbol, (void *)ret);
		}

		set_dlerror(DL_ERR_SYMBOL_NOT_GLOBAL);
	} else
		set_dlerror(DL_ERR_SYMBOL_NOT_FOUND);

err:
	verbose("symbol %s has not been hooked\n", symbol);
	pthread_mutex_unlock(&apkenv_dl_lock);
	return 0;
}

int bionic_dladdr(const void *addr, Dl_info *info)
{
	int ret = 0;

	pthread_mutex_lock(&apkenv_dl_lock);

	/* Determine if this address can be found in any library currently mapped */
	soinfo *si = apkenv_find_containing_library(addr);

	if (si) {
		memset(info, 0, sizeof(*info));

		info->dli_fname = si->name;
		info->dli_fbase = (void *)(uintptr_t)si->base;

		/* Determine if any symbol in the library contains the specified address */
		ElfW(Sym) *sym = apkenv_find_containing_symbol(addr, si);

		if (sym != NULL) {
			info->dli_sname = si->strtab + sym->st_name;
			info->dli_saddr = (void *)(uintptr_t)(si->base + sym->st_value);
		}

		ret = 1;
	}

	pthread_mutex_unlock(&apkenv_dl_lock);

	/* don't use else because this shouldn't be inside the critical zone */
	if (!si)
		return dladdr(addr, info);

	return ret;
}

int bionic_dlclose(void *handle)
{
	if (!do_we_have_this_handle(handle))
		return dlclose(handle);

#if 0
	if (is_builtin_lib_handle(handle))
		return 0;
#endif

	pthread_mutex_lock(&apkenv_dl_lock);
	(void)apkenv_unload_library((soinfo *)handle);
	pthread_mutex_unlock(&apkenv_dl_lock);
	return 0;
}

#if defined(__arm__)
//					 0000000 00011111 111112 22222222 2333333 333344444444445555555
//					 0123456 78901234 567890 12345678 9012345 678901234567890123456
#define ANDROID_LIBDL_STRTAB \
	"dlopen\0dlclose\0dlsym\0dlerror\0dladdr\0dl_unwind_find_exidx\0"

_Unwind_Ptr bionic_dl_unwind_find_exidx(_Unwind_Ptr pc, int *pcount);

#elif defined(__aarch64__) || defined(__i386__) || defined(__mips__) || defined(__x86_64__)
//					 0000000 00011111 111112 22222222 2333333 3333444444444455
//					 0123456 78901234 567890 12345678 9012345 6789012345678901
#define ANDROID_LIBDL_STRTAB \
	"dlopen\0dlclose\0dlsym\0dlerror\0dladdr\0dl_iterate_phdr\0"
int bionic_dl_iterate_phdr(int (*cb)(struct dl_phdr_info *info, size_t size, void *data), void *data);

#else
#error Unsupported architecture. Only ARM and x86 are presently supported.
#endif

static ElfW(Sym) apkenv_libdl_symtab[7];

/* Fake out a hash table with a single bucket.
 * A search of the hash table will look through
 * apkenv_libdl_symtab starting with index [1], then
 * use apkenv_libdl_chains to find the next index to
 * look at.  apkenv_libdl_chains should be set up to
 * walk through every element in apkenv_libdl_symtab,
 * and then end with 0 (sentinel value).
 *
 * I.e., apkenv_libdl_chains should look like
 * { 0, 2, 3, ... N, 0 } where N is the number
 * of actual symbols, or nelems(apkenv_libdl_symtab)-1
 * (since the first element of apkenv_libdl_symtab is not
 * a real symbol).
 *
 * (see _elf_lookup())
 *
 * Note that adding any new symbols here requires
 * stubbing them out in libdl.
 */
static unsigned apkenv_libdl_buckets[1] = {1};
static unsigned apkenv_libdl_chains[7] = {0, 2, 3, 4, 5, 6, 0};

soinfo apkenv_libdl_info = {
    .name = "libdl.so",
    .flags = FLAG_LINKED,

    .strtab = ANDROID_LIBDL_STRTAB,
    .symtab = apkenv_libdl_symtab,

    .nbucket = 1,
    .nchain = 7,
    .bucket = apkenv_libdl_buckets,
    .chain = apkenv_libdl_chains,
};

struct override_map lib_override_map;

__attribute__((constructor)) void construct(void)
{
	const ElfW(Sym) symtab[sizeof(apkenv_libdl_symtab) / sizeof(apkenv_libdl_symtab[0])] = {
		// total length of apkenv_libdl_info.strtab, including trailing 0
		// This is actually the the STH_UNDEF entry. Technically, it's
		// supposed to have st_name == 0, but instead, it points to an index
		// in the strtab with a \0 to make iterating through the symtab easier.
		{
		  .st_name = sizeof(ANDROID_LIBDL_STRTAB) - 1,
		}, {
		  .st_name = 0,   // starting index of the name in apkenv_libdl_info.strtab
		  .st_value = (ElfW(Addr))(uintptr_t)bionic_dlopen,
		  .st_info = STB_GLOBAL << 4,
		  .st_shndx = 1,
		}, {
		  .st_name = 7,
		  .st_value = (ElfW(Addr))(uintptr_t)bionic_dlclose,
		  .st_info = STB_GLOBAL << 4,
		  .st_shndx = 1,
		}, {
		  .st_name = 15,
		  .st_value = (ElfW(Addr))(uintptr_t)bionic_dlsym,
		  .st_info = STB_GLOBAL << 4,
		  .st_shndx = 1,
		}, {
		  .st_name = 21,
		  .st_value = (ElfW(Addr))(uintptr_t)bionic_dlerror,
		  .st_info = STB_GLOBAL << 4,
		  .st_shndx = 1,
		}, {
		  .st_name = 29,
		  .st_value = (ElfW(Addr))(uintptr_t)bionic_dladdr,
		  .st_info = STB_GLOBAL << 4,
		  .st_shndx = 1,
		},
#if defined(__arm__)
		{
		  .st_name = 36,
		  .st_value = (ElfW(Addr))(uintptr_t)bionic_dl_unwind_find_exidx,
		  .st_info = STB_GLOBAL << 4,
		  .st_shndx = 1,
		},
#elif defined(__aarch64__) || defined(__i386__) || defined(__mips__) || defined(__x86_64__)
		{
		  .st_name = 36,
		  .st_value = (ElfW(Addr))(uintptr_t)bionic_dl_iterate_phdr,
		  .st_info = STB_GLOBAL << 4,
		  .st_shndx = 1,
		},
#endif
	};
	memcpy(apkenv_libdl_symtab, symtab, sizeof(symtab));

	// the config files contain overrides like libc.so -> libc_bio.so.0
	const char *xdg_data_dirs = getenv("XDG_DATA_DIRS") ?: "/usr/local/share:/usr/share";
	char *cfg_path = malloc(strlen(xdg_data_dirs) + sizeof("/bionic_translation/cfg.d"));
	while (*xdg_data_dirs) {
		size_t len = strcspn(xdg_data_dirs, ":");
		memcpy(cfg_path, xdg_data_dirs, len);
		memcpy(cfg_path + len, "/bionic_translation/cfg.d", sizeof("/bionic_translation/cfg.d"));
		read_cfg_dir(&lib_override_map, cfg_path);
		xdg_data_dirs += len;
		xdg_data_dirs += strspn(xdg_data_dirs, ":");
	}
	free(cfg_path);
	read_cfg_dir(&lib_override_map, "/etc/bionic_translation/cfg.d");


	// since it seems to not be particularly trivial to figure out which
	// libs we should link ourselves and which libs we should leave to glibc,
	// we make the following design decision:
	// libs linked against bionic are precisely the libs found here
	const char *bionic_ld_library_path = getenv("BIONIC_LD_LIBRARY_PATH");

	// XXX SECURITY NOTE: There would normally be an suid check done here to make
	// extra sure we're not (as a linker) compromising the security guarantees
	// of suid executables.
	// However, since nobody sane should ever load this shim linker from an suid process,
	// we don't currently do said check
	if (bionic_ld_library_path) {
		dl_parse_library_path(bionic_ld_library_path, ":");
	} // it might make sense to not specify the env, for example translation layer code calls dl_parse_library_path directly with the app's lib dir
}

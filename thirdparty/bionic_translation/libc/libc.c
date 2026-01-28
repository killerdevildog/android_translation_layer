#include "../wrapper/verbose.h"
#include <assert.h>
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <limits.h>
#include <math.h>
#include <netdb.h> // h_errno
#include <setjmp.h>
#include <signal.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <unistd.h>

#include <sys/stat.h>
#include <sys/syscall.h>
#include <sys/types.h>
#include <sys/user.h> // PAGE_SIZE, PAGE_SHIFT

#ifndef PAGE_SIZE
#define PAGE_SIZE PAGESIZE
#endif

struct bionic_dirent {
	uint64_t d_ino;
	int64_t d_off;
	unsigned short d_reclen;
	unsigned char d_type;
	char d_name[256];
};

#ifndef __LP64__
typedef unsigned long bionic_sigset_t;
struct bionic_sigaction {
	union {
		void (*bsa_handler)(int);
		void (*bsa_sigaction)(int, void *, void *);
	};
	bionic_sigset_t sa_mask;
	int sa_flags;
	void (*sa_restorer)(void);
};
#else // for 64bit arches, `sa_flags` is in a different place
typedef unsigned long bionic_sigset_t;
struct bionic_sigaction {
	unsigned int sa_flags;
	union {
		void (*bsa_handler)(int);
		void (*bsa_sigaction)(int, void *, void *);
	};
	bionic_sigset_t sa_mask;
	void (*sa_restorer)(void);
};
#endif

// Stuff that doesn't exist in glibc

#define PROP_NAME_MAX  32
#define PROP_VALUE_MAX 92

int __system_property_get(const char *name, char *value)
{
	verbose("%s", name);

	if (!strcmp(name, "ro.build.version.sdk"))
		return snprintf(value, PROP_VALUE_MAX, "%d", 21);

	*value = 0;
	return 0;
}

pid_t gettid(void)
{
	return syscall(SYS_gettid);
}

int tgkill(int tgid, int tid, int sig)
{
	verbose("%d, %d, %d", tgid, tid, sig);
	return syscall(SYS_tgkill, tgid, tid, sig);
}

int tkill(int tid, int sig)
{
	verbose("%d, %d", tid, sig);
	return syscall(SYS_tkill, tid, sig);
}

// Stuff needed for runtime compatibility, but not neccessary for linking
// Also stuff that exists in glibc, but needs to be wrapped for runtime compatibility

// Some defines from app-stdio.c as per GNU linker's manual for --wrap:
//    You may wish to provide a __real_malloc function as well, so that links without the
//    --wrap option will succeed. If you do this, you should not put the definition of
//    __real_malloc in the same file as __wrap_malloc; if you do, the assembler may resolve
//    the call before the linker has a chance to wrap it to malloc.

size_t __real_IO_file_xsputn(FILE *f, const void *buf, size_t n) { return 0; }

#include "libc-ctype.h"

#ifndef __attribute_const__
#define __attribute_const__ __attribute__((const))
#endif

const unsigned int bionic___page_size = PAGE_SIZE;

__attribute_const__ int *
bionic___errno(void)
{
	return __errno_location();
}

__attribute_const__ int *
bionic___get_h_errno(void)
{
	return &h_errno;
}

// this doesn't seem to be needed for 64bit glibc or musl
// 32bit glibc seems to work without as well but it doesn't hurt
#ifndef __LP64__
typedef uint32_t bionic_time_t;

struct bionic_timespec {
	bionic_time_t tv_sec;
	long tv_nsec;
};

struct bionic_stat {
	unsigned long long st_dev;
	unsigned int pad0;
	unsigned long __st_ino;
	unsigned int st_mode;
	int st_nlink;
	int st_uid;
	int st_gid;
	unsigned long long st_rdev;
	unsigned int pad3;
	long long st_size;
	unsigned long st_blksize;
	unsigned long long st_blocks;
	struct bionic_timespec st_atim;
	struct bionic_timespec st_mtim;
	struct bionic_timespec st_ctim;
	unsigned long long st_ino;
};

int bionic_stat(const char *restrict path, struct bionic_stat *restrict buf)
{
	verbose("%s", path);
	struct stat native_stat;

	int ret = stat(path, &native_stat);
	*buf = (struct bionic_stat) {
		.st_dev = native_stat.st_dev,
		.st_ino = native_stat.st_ino,
		.st_mode = native_stat.st_mode,
		.st_nlink = native_stat.st_nlink,
		.st_uid = native_stat.st_uid,
		.st_gid = native_stat.st_gid,
		.st_rdev = native_stat.st_rdev,
		.st_blksize = native_stat.st_blksize,
		.st_blocks = native_stat.st_blocks,
		.st_atim.tv_sec = (bionic_time_t)native_stat.st_atim.tv_sec,
		.st_atim.tv_nsec = native_stat.st_atim.tv_nsec,
		.st_mtim.tv_sec = (bionic_time_t)native_stat.st_mtim.tv_sec,
		.st_mtim.tv_nsec = native_stat.st_mtim.tv_nsec,
		.st_ctim.tv_sec = (bionic_time_t)native_stat.st_ctim.tv_sec,
		.st_ctim.tv_nsec = native_stat.st_ctim.tv_nsec,
	};
	return ret;
}
#else
int bionic_stat(const char *restrict path, struct stat *restrict buf)
{
	verbose("%s", path);
	return stat(path, buf);
}
#endif

int bionic_lstat(const char *restrict path, struct stat *restrict buf)
{
	verbose("%s", path);
	return lstat(path, buf);
}

int bionic_fstat(int fd, struct stat *buf)
{
	verbose("%d", fd);
	return fstat(fd, buf);
}

int bionic_fstatat(int dirfd, const char *pathname, void *buf, int flags)
{
	verbose("%d, %s", dirfd, pathname);
	return fstatat(dirfd, pathname, buf, flags);
}

static void
glibc_dirent_to_bionic_dirent(const struct dirent *de, struct bionic_dirent *bde)
{
	assert(bde && de);
	*bde = (struct bionic_dirent){
	    .d_ino = de->d_ino,
	    .d_off = de->d_off,
	    .d_reclen = de->d_reclen,
	    .d_type = de->d_type,
	};
	_Static_assert(sizeof(bde->d_name) >= sizeof(de->d_name), "bionic_dirent can't hold dirent's d_name");
	memcpy(bde->d_name, de->d_name, sizeof(bde->d_name));
}

struct bionic_dirent *
bionic_readdir(DIR *dirp)
{
	assert(dirp);
	static struct bionic_dirent bde;
	struct dirent *de;
	if (!(de = readdir(dirp)))
		return NULL;
	glibc_dirent_to_bionic_dirent(de, &bde);
	return &bde;
}

// readdir_r is deprecated in glibc, but as long as it exists, it makes sense to use it for implementing the bionic variant
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wdeprecated-declarations"
int bionic_readdir_r(DIR *dirp, struct bionic_dirent *entry, struct bionic_dirent **result)
{
	assert(dirp && entry && result);
	struct dirent de, *der = NULL;

	int ret;
	if ((ret = readdir_r(dirp, &de, &der)) != 0 || !der) {
		*result = NULL;
		return ret;
	}

	glibc_dirent_to_bionic_dirent(der, entry);
	*result = entry;
	return 0;
}
#pragma GCC diagnostic pop

// Need to wrap bunch of signal crap
// https://android.googlesource.com/platform/bionic/+/master/docs/32-bit-abi.md

int bionic_sigaddset(const bionic_sigset_t *set, int sig)
{
	int bit = sig - 1; // Signal numbers start at 1, but bit positions start at 0.
	unsigned long *local_set = (unsigned long *)set;
	if (!set || bit < 0 || bit >= (int)(8 * sizeof(*set))) {
		errno = EINVAL;
		return -1;
	}
	local_set[bit / LONG_BIT] |= 1UL << (bit % LONG_BIT);
	return 0;
}

int bionic_sigismember(const bionic_sigset_t *set, int sig)
{
	int bit = sig - 1; // Signal numbers start at 1, but bit positions start at 0.
	const unsigned long *local_set = (const unsigned long *)set;
	if (!set || bit < 0 || bit >= (int)(8 * sizeof(*set))) {
		errno = EINVAL;
		return -1;
	}
	return (int)((local_set[bit / LONG_BIT] >> (bit % LONG_BIT)) & 1);
}

int bionic_sigaction(int sig, const struct bionic_sigaction *restrict act, struct bionic_sigaction *restrict oact)
{
	verbose("%d, %p, %p", sig, (void *)act, (void *)oact);

	// THREAD_SIGNAL on android used by libbacktrace
	if (sig == 33)
		sig = SIGRTMIN;

	struct sigaction goact = {0}, gact = {0};
	if (act) {
		gact.sa_handler = act->bsa_handler;
		gact.sa_flags = act->sa_flags;
		gact.sa_restorer = act->sa_restorer;

		// delete reserved signals
		// 32 (__SIGRTMIN + 0)        POSIX timers
		// 33 (__SIGRTMIN + 1)        libbacktrace
		// 34 (__SIGRTMIN + 2)        libcore
		// 35 (__SIGRTMIN + 3)        debuggerd -b
		assert(35 < SIGRTMAX);
		for (int signo = 35; signo < SIGRTMAX; ++signo) {
			if (bionic_sigismember(&act->sa_mask, signo))
				sigaddset(&gact.sa_mask, signo);
		}
	}

	const int ret = sigaction(sig, (act ? &gact : NULL), (oact ? &goact : NULL));

	if (oact) {
		*oact = (struct bionic_sigaction){0};
		oact->bsa_handler = goact.sa_handler;
		oact->sa_flags = goact.sa_flags;
		oact->sa_restorer = goact.sa_restorer;

		for (int signo = SIGRTMIN + 3; signo < SIGRTMAX; ++signo) {
			if (sigismember(&goact.sa_mask, signo))
				bionic_sigaddset(&oact->sa_mask, signo);
		}
	}

	return ret;
}

int bionic___isfinitef(float f)
{
	return isfinite(f);
}

int bionic___isfinite(float f)
{
	return isfinite(f);
}

void bionic___assert2(const char *file, int line, const char *function, const char *failed_expression)
{
	fprintf(stderr, "%s:%d: %s: assertion \"%s\" failed\n", file, line, function, failed_expression);
	abort();
}

/* in most if not all cases this is not what the app will use, see main-executable/bionic_compat.c */
uintptr_t bionic___stack_chk_guard = 4;

__attribute__((noreturn))
void bionic___stack_chk_fail(void)
{
	abort();
}

#include "libc-sysconf.h"

long bionic_sysconf(int name)
{
	verbose("0x%x", name);
	return sysconf(bionic_sysconf_to_glibc_sysconf(name));
}

static void
__libc_fini(int signal, void *array)
{
	void **fini_array = (void **)array;

	if (!array || (size_t)fini_array[0] != (size_t)~0)
		return;

	fini_array += 1;

	int count;
	for (count = 0; fini_array[count]; ++count)
		;

	for (; count > 0; --count) {
		const union {
			void *ptr;
			void (*fun)(void);
		} fini = {.ptr = fini_array[count]};

		if ((size_t)fini.ptr != (size_t)~0)
			fini.fun();
	}
}

struct bionic_structors {
	void (**preinit_array)(void);
	void (**init_array)(void);
	void (**fini_array)(void);
};

static const struct bionic_structors *__structors;
static void __atexit_libc_fini() { __libc_fini(0, __structors->fini_array); }

__attribute__((noreturn)) void
bionic___libc_init(void *raw_args, void (*onexit)(void), int (*slingshot)(int, char **, char **), struct bionic_structors const *const structors)
{
	// linker has already called the constructors

	union {
		struct s {
			uintptr_t argc;
			char **argv;
		} s;
		char bytes[sizeof(struct s)];
	} arg;

	memcpy(arg.bytes, raw_args, sizeof(arg.bytes));

	__structors = structors;
	if (structors->fini_array && atexit(__atexit_libc_fini)) {
		fprintf(stderr, "__cxa_atexit failed\n");
		abort();
	}

	exit(slingshot(arg.s.argc, arg.s.argv, arg.s.argv + arg.s.argc + 1));
}

#ifndef __LP64__
int bionic_clock_gettime(clockid_t clockid, struct bionic_timespec *bionic_tp)
{
	struct timespec tp;
	int ret = clock_gettime(clockid, &tp);
	bionic_tp->tv_sec = (bionic_time_t)tp.tv_sec;
	bionic_tp->tv_nsec = tp.tv_nsec;
	return ret;
}
#endif

/* position of ai_canonname and ai_addr are swapped between bionic and glibc/musl */
struct bionic_addrinfo {
	int ai_flags;           /* AI_PASSIVE, AI_CANONNAME, AI_NUMERICHOST */
	int ai_family;          /* PF_xxx */
	int ai_socktype;        /* SOCK_xxx */
	int ai_protocol;        /* 0 or IPPROTO_xxx for IPv4 and IPv6 */
	socklen_t ai_addrlen;   /* length of ai_addr */
	char *ai_canonname;     /* canonical name for hostname */
	struct sockaddr *ai_addr;    /* binary address */
	struct bionic_addrinfo *ai_next;    /* next structure in linked list */
};

int bionic_getaddrinfo(const char *hostname, const char *servname, const struct bionic_addrinfo *bionic_hints, struct bionic_addrinfo **res)
{
	struct addrinfo hints;
	if (bionic_hints) {
		memcpy(&hints, bionic_hints, sizeof(hints));
		hints.ai_canonname = bionic_hints->ai_canonname;
		hints.ai_addr = bionic_hints->ai_addr;
	}
	int result = getaddrinfo(hostname, servname, bionic_hints ? &hints : NULL, (struct addrinfo **)res);

	if(result == 0) {
		struct bionic_addrinfo *it = *res;
		while (it) {
			char *ai_canonname = ((struct addrinfo *)it)->ai_canonname;
			struct sockaddr *ai_addr = ((struct addrinfo *)it)->ai_addr;
			it->ai_canonname = ai_canonname;
			it->ai_addr = ai_addr;
			it = it->ai_next;
		}
	}
	return result;
}

void bionic_freeaddrinfo(struct bionic_addrinfo *ai)
{
	struct bionic_addrinfo *it = ai;
	while (it) {
		char *ai_canonname = it->ai_canonname;
		struct sockaddr *ai_addr = it->ai_addr;
		((struct addrinfo *)it)->ai_canonname = ai_canonname;
		((struct addrinfo *)it)->ai_addr = ai_addr;
		it = it->ai_next;
	}
	freeaddrinfo((struct addrinfo *)ai);
}

#ifdef VERBOSE_FUNCTIONS
#include "libc-verbose.h"
#endif

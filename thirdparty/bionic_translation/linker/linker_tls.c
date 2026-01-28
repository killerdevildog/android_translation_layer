#include <assert.h>
#include <malloc.h>
#include <pthread.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#include "linker_tls.h"

/* TLS Module Descriptor */
typedef struct {
	void *template_base;
	size_t template_size;
	size_t size;
	int align;
} tls_module_desc_t;

/* global TLS module registry */
typedef struct {
	tls_module_desc_t *modules;
	size_t count;
	size_t capacity;
	pthread_mutex_t lock;
} tls_module_registry_t;

/* static global registry */
static tls_module_registry_t global_module_registry = {
	.modules = NULL,
	.count = 0,
	.capacity = 0,
	.lock = PTHREAD_MUTEX_INITIALIZER
};

/* per-thread TLS module tracking key */
static pthread_key_t tls_module_key;
static pthread_once_t tls_module_key_once = PTHREAD_ONCE_INIT;

static void tls_module_key_destructor(void *ptr)
{
	free(ptr);
}

static void create_tls_module_key(void)
{
	pthread_key_create(&tls_module_key, tls_module_key_destructor);
}

size_t __tls_register_module(void *template_base, size_t template_size, size_t size, int align)
{
	pthread_once(&tls_module_key_once, create_tls_module_key);

	pthread_mutex_lock(&global_module_registry.lock);

	// grow registry if needed
	if (global_module_registry.count >= global_module_registry.capacity) {
		size_t new_capacity = global_module_registry.capacity == 0 ? 8 : global_module_registry.capacity * 2;
		tls_module_desc_t *new_modules = realloc(
		    global_module_registry.modules,
		    new_capacity * sizeof(tls_module_desc_t));

		if (!new_modules) {
			pthread_mutex_unlock(&global_module_registry.lock);
			return (size_t)-1;
		}

		global_module_registry.modules = new_modules;
		global_module_registry.capacity = new_capacity;
	}

	// add new module descriptor
	size_t slot = global_module_registry.count++;
	global_module_registry.modules[slot] = (tls_module_desc_t){
	    .template_base = template_base,
	    .template_size = template_size,
	    .size = size,
	    .align = align,
	};

	pthread_mutex_unlock(&global_module_registry.lock);

	return slot;
}

/* look up TLS module for current thread */
void *__tls_get_module(size_t slot)
{
	pthread_once(&tls_module_key_once, create_tls_module_key);

	// Fetch or create per-thread module tracking
	void **thread_modules = pthread_getspecific(tls_module_key);
	if (!thread_modules) {
		thread_modules = calloc(global_module_registry.count, sizeof(void *));
		if (!thread_modules)
			return NULL;

		pthread_setspecific(tls_module_key, thread_modules);
	}

	if (!thread_modules[slot]) {
		if (slot >= global_module_registry.count)
			return NULL;

		tls_module_desc_t desc = global_module_registry.modules[slot];

		// Allocate thread-local storage for this module
		void *tls_block = memalign(desc.align, desc.size);
		if (!tls_block)
			return NULL;

		if(desc.template_base) {
			assert(desc.template_size <= desc.size);
			memcpy(tls_block, desc.template_base, desc.template_size);
		} else {
			assert(desc.template_size == 0);
		}

		memset(tls_block + desc.template_size, 0, desc.size - desc.template_size);

		// Store the allocated TLS block for this thread
		thread_modules[slot] = tls_block;
	}

	return thread_modules[slot];
}

struct tls_index
{
	size_t module;
	size_t offset;
};

/* this is called by the .so with the module_id (== slot) that we've put in during relocation */
#if defined(__i386__)
/* 32 bit x86 uses a different symbol name and calling convention, because
 * GNU decided that passing the parameter in a register is preferable and didn't
 * want the name to conflict with the de-facto standard by Sun (which always uses
 * native calling convention)
 *
 * glibc technically provides both, but bionic only supports the GNU version */
#define bionic___tls_get_addr bionic____tls_get_addr

__attribute__((regparm(1)))
#endif
uintptr_t bionic___tls_get_addr(struct tls_index *idx)
{
	void *module_base = __tls_get_module(idx->module);
	if (!module_base)
		return 0;

	return (uintptr_t)module_base + idx->offset;
}

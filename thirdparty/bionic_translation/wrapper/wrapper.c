#include "wrapper.h"
#include <dlfcn.h>
#include <stdio.h>
#include <stdarg.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>
#include <sys/mman.h>
#include "verbose.h"
#include <pthread.h>

#define ANDROID_LOG_VERBOSE 2

/* TODO: this file used to host a tracing mechanism. if this is ever desired,
 * feel free to reimplement it using the copyable functions */

typedef int __android_log_vprint_type(int prio, const char *tag, const char *fmt, va_list ap);

static int fallback_verbose_log(int prio, const char *tag, const char *fmt, va_list ap)
{
	int ret;

	static pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
	pthread_mutex_lock(&mutex);
	static char buf[1024];
	ret = vsnprintf(buf, sizeof(buf), fmt, ap);
	fprintf(stderr, "%lu: %s\n", pthread_self(), buf);
	pthread_mutex_unlock(&mutex);

	return ret;
}

static int android_log_vprintf(int prio, const char *tag, const char *fmt, va_list ap)
{

	static __android_log_vprint_type *_android_log_vprintf = NULL;
	if(!_android_log_vprintf) {
		_android_log_vprintf = dlsym(RTLD_DEFAULT, "__android_log_vprint");

		if(!_android_log_vprintf) {
			_android_log_vprintf = &fallback_verbose_log;
		}
	}

	return _android_log_vprintf(prio, tag, fmt, ap);
}

int android_log_printf(int prio, const char *tag, const char *fmt, ...)
{
	int ret;

	va_list ap;
	va_start(ap, fmt);

	ret = android_log_vprintf(prio, tag, fmt, ap);

	va_end(ap);

	return ret;
}

void verbose_log(const char *fmt, ...)
{
	va_list ap;
	va_start(ap, fmt);

	android_log_vprintf(ANDROID_LOG_VERBOSE, "[bionic_translation]", fmt, ap);

	va_end(ap);
}

void wrapper_set_cpp_demangler(void *function)
{

}

void * wrapper_create(const char *const symbol, void *function)
{
	assert(symbol);

	if (!function) {
		verbose_log("FIXME: unimplemented symbol: %s", symbol);
		return NULL;
	}

	return function;
}

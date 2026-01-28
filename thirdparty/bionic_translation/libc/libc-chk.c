// SPDX-License-Identifier: MIT AND Apache-2.0

// portions of this file Copyright (C) The Android Open Source Project
// (specifically most non-noop implementations are adapted from actual bionic code)

// runtime implementations of _FORTIFY_SOURCE _chk functions (some with the checking removed)


#include <sys/select.h>
#include <sys/stat.h>

#include <assert.h>
#include <fcntl.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <limits.h>

/* musl needs these, glibc implements them (and probably did even before bionic) */
#ifndef __GLIBC__

char *bionic___strncpy_chk(char *__restrict dest, const char *__restrict src,
			   size_t len, size_t dest_len)
{
	return strncpy(dest, src, len);
}

int bionic___vsnprintf_chk(char *dest, size_t supplied_size, int /*flags*/,
			   size_t dest_len_from_compiler, const char *format, va_list va)
{
	return vsnprintf(dest, supplied_size, format, va);
}

int bionic___snprintf_chk(char *dest, size_t supplied_size, int flags,
			  size_t dest_len_from_compiler, const char *format, ...)
{
	va_list va;
	va_start(va, format);
	int result = bionic___vsnprintf_chk(dest, supplied_size, flags, dest_len_from_compiler, format, va);
	va_end(va);
	return result;
}

int bionic___vsprintf_chk(char *dest, int /*flags*/,
			  size_t dest_len_from_compiler, const char *format, va_list va)
{
	int result = vsnprintf(dest, dest_len_from_compiler, format, va);
	return result;
}

int bionic___sprintf_chk(char *dest, int flags,
			 size_t dest_len_from_compiler, const char *format, ...)
{
	va_list va;
	va_start(va, format);
	int result = bionic___vsprintf_chk(dest, flags, dest_len_from_compiler, format, va);
	va_end(va);
	return result;
}

void *bionic___memcpy_chk(void *dest, const void *src,
			  size_t copy_amount, size_t dest_len)
{
	return memcpy(dest, src, copy_amount);
}

void *bionic___memmove_chk(void *dest, const void *src,
			   size_t len, size_t dest_len)
{
	return memmove(dest, src, len);
}

ssize_t bionic___read_chk(int fd, void *buf, size_t count, size_t buf_size)
{
	return read(fd, buf, count);
}

int bionic___open_2(const char *pathname, int flags)
{
	flags |= O_LARGEFILE;
	return open(pathname, flags, 0);
}

void *bionic___memset_chk(void *dest, int c, size_t n, size_t dest_len)
{
	return memset(dest, c, n);
}

char *bionic___strcpy_chk(char *dest, const char *src, size_t dest_len)
{
	return strcpy(dest, src);
}

char * bionic___stpncpy_chk(char* __restrict dest, const char* __restrict src, size_t len, size_t dest_len) {
	if (__builtin_expect(len > dest_len, 0)) {
		fprintf(stderr, "stpncpy: prevented write past end of buffer");
	}
	return stpncpy(dest, src, len);
}

#endif

/* these exist in glibc, but have incompatible ABI */

char *bionic___fgets_chk(char *dest, int supplied_size, FILE* stream, size_t dest_len_from_compiler)
{
	if (supplied_size < 0) {
		fprintf(stderr, "fgets: buffer size < 0");
		abort();
	}
	if (((size_t) supplied_size) > dest_len_from_compiler) {
		fprintf(stderr, "fgets: prevented write past end of buffer");
		abort();
	}
	return fgets(dest, supplied_size, stream);
}

/* these don't exist in glibc either, which also means that by definition their ABI is not copied from glibc */

int bionic___FD_ISSET_chk(int fd, fd_set *set)
{
	return FD_ISSET(fd, set);
}

void bionic___FD_CLR_chk(int fd, fd_set *set)
{
	FD_CLR(fd, set);
}

void bionic___FD_SET_chk(int fd, fd_set *set)
{
	FD_SET(fd, set);
}

size_t bionic___fwrite_chk(const void *__restrict buf, size_t size, size_t count, FILE *__restrict stream, size_t buf_size)
{
	size_t total;
	if (__builtin_expect(__builtin_mul_overflow(size, count, &total), 0)) {
		// overflow: trigger the error path in fwrite
		return fwrite(buf, size, count, stream);
	}

	if (__builtin_expect(total > buf_size, 0)) {
		fprintf(stderr, "*** fwrite read overflow detected ***\n");
		abort();
	}

	return fwrite(buf, size, count, stream);
}

size_t bionic___strlen_chk(const char *s, size_t s_len)
{
	size_t ret = strlen(s);
	if (__builtin_expect(ret >= s_len, 0)) {
		fprintf(stderr, "strlen read overflow detected\n");
		abort();
	}
	return ret;
}

char *bionic___strchr_chk(const char *p, int ch, size_t s_len)
{
	for (;; ++p, s_len--) {
		if (__builtin_expect(s_len == 0, 0)) {
			fprintf(stderr, "*** strchr buffer overrun detected ***\n");
			abort();
		}

		if (*p == ch)
			return (char *)p;
		else if (!*p)
			return NULL;
	}
	assert(0 && "should not happen");
}

char *bionic___strrchr_chk(const char *p, int ch, size_t s_len)
{
	const char *save;
	for (save = NULL;; ++p, s_len--) {
		if (__builtin_expect(s_len == 0, 0)) {
			fprintf(stderr, "*** strchr buffer overrun detected ***\n");
			abort();
		}

		if (*p == ch)
			save = p;
		else if (!*p)
			return (char *)save;
	}
	assert(0 && "should not happen");
}

mode_t bionic___umask_chk(mode_t mode) {
	if (__builtin_expect((mode & 0777) != mode, 0)) {
		fprintf(stderr, "umask called with invalid mask");
	}
	return umask(mode);
}

ssize_t bionic___write_chk(int fd, const void* buf, size_t count, size_t buf_size) {
	if (__builtin_expect(count > buf_size, 0)) {
		fprintf(stderr, "write: prevented read past end of buffer");
		abort();
	}

	if (__builtin_expect(count > SSIZE_MAX, 0)) {
		fprintf(stderr, "write: count > SSIZE_MAX");
		abort();
	}

	return write(fd, buf, count);
}

/* NOTE: fortify level 2 is not meant for production, so arguably if some app uses it we should
 * fix it for them and have _chk2 functions not actually check anything */

char * bionic___stpncpy_chk2(char* __restrict dst, const char* __restrict src, size_t n, size_t dest_len, size_t src_len)
{
	if (__builtin_expect(n > dest_len, 0)) {
		fprintf(stderr, "stpncpy: prevented write past end of buffer");
	}
	if (n != 0) {
		char* d = dst;
		const char* s = src;
		do {
			if ((*d++ = *s++) == 0) {
				/* NUL pad the remaining n-1 bytes */
				while (--n != 0)
					*d++ = 0;
				break;
			}
		} while (--n != 0);
		size_t s_copy_len = (size_t)(s - src);
		if (__builtin_expect(s_copy_len > src_len, 0)) {
			fprintf(stderr, "stpncpy: prevented read past end of buffer");
		}
	}
	return dst;
}

char *bionic___strncpy_chk2(char *__restrict dst, const char *__restrict src,
			    size_t n, size_t dest_len, size_t src_len)
{
	if (n != 0) {
		char *d = dst;
		const char *s = src;
		do {
			if ((*d++ = *s++) == 0) {
				/* NUL pad the remaining n-1 bytes */
				while (--n != 0) {
					*d++ = 0;
				}
				break;
			}
		} while (--n != 0);
	}
	return dst;
}

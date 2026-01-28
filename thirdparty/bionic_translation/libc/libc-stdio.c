#include <stdio.h>

#include "libc-stdio.h"

// Bionic standard stream support pre-M Android
// Post-M it's saner and they point to stdin/stdout/stderr symbols instead
const struct bionic___sFILE bionic___sF[3] = {
    {{'s', 't', 'd', 'i', 'n'}},
    {{'s', 't', 'd', 'o', 'u', 't'}},
    {{'s', 't', 'd', 'e', 'r', 'r'}}};

// these only exist in bionic AFAICT

int __srget(FILE *fp)
{
	return fgetc(bionic_file_to_glibc_file(fp));
}

int __swbuf(int c, FILE *fp)
{
	return fputc(c, bionic_file_to_glibc_file(fp));
}

// libstdc++ uses these directly for standard streams, thus we need to wrap em
// and IO_file wraps aren't enough.

int bionic_fflush(FILE *f)
{
	return fflush(bionic_file_to_glibc_file(f));
}

size_t
bionic_fwrite(const void *ptr, size_t size, size_t nmemb, FILE *stream)
{
	return fwrite(ptr, size, nmemb, bionic_file_to_glibc_file(stream));
}

int bionic_putc(int ch, FILE *f)
{
	return putc(ch, bionic_file_to_glibc_file(f));
}

#ifdef __GLIBC__

// Wrapping internal glibc VTABLE functions to handle bionic's pre-M crap
// We define __real_IO_file_xsputn in libc.c so linker will link our library,
// it's not used however for anything.

// no such luck with bionic, we need to wrap every single function that gets passed a FILE *

extern size_t __real_IO_file_xsputn(FILE *f, const void *buf, size_t n);

size_t __wrap_IO_file_xsputn(FILE *f, const void *buf, size_t n)
{
	return __real_IO_file_xsputn(bionic_file_to_glibc_file(f), buf, n);
}

#endif

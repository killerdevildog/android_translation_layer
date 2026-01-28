#ifndef LIBC_STDIO_H
#define LIBC_STDIO_H

#include <stdio.h>

struct bionic___sFILE {
#if defined(__LP64__)
	char __private[152];
#else
	char __private[84];
#endif
} __attribute__((aligned(sizeof(void *))));

extern const struct bionic___sFILE bionic___sF[3];

static inline FILE *
bionic_file_to_glibc_file(FILE *f)
{
	if (f == (void *)&bionic___sF[0])
		return stdin;
	else if (f == (void *)&bionic___sF[1])
		return stdout;
	else if (f == (void *)&bionic___sF[2])
		return stderr;
	return f;
}

#endif

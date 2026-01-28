#ifndef STRLCPY_H
#define STRLCPY_H

#include <string.h>
#include <sys/types.h>

size_t
apkenv_strlcpy(char *dst, const char *src, size_t siz);

#endif

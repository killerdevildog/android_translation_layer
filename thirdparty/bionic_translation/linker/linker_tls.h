#ifndef LINKER_TLS_H
#define LINKER_TLS_H

#include <stddef.h>

size_t __tls_register_module(void *template_base, size_t template_size, size_t size, int align);

#endif

#pragma once

#define RTLD_LAZY	  0x00001 /* Lazy function call binding.  */
#define RTLD_NOW	  0x00002 /* Immediate function call binding.  */
#define RTLD_BINDING_MASK 0x3	  /* Mask of binding time value.  */
#define RTLD_NOLOAD	  0x00004 /* Do not load the object.  */
#define RTLD_DEEPBIND	  0x00008 /* Use deep binding.  */

#ifdef __cplusplus
extern "C" {
#endif

void dl_parse_library_path(const char *path, char *delim);
void *bionic_dlopen(const char *filename, int flag);
const char *bionic_dlerror(void);
void *bionic_dlsym(void *handle, const char *symbol);
int bionic_dlclose(void *handle);

#ifdef __cplusplus
}
#endif

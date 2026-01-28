#include <aio.h>
#include <dirent.h>
#include <fcntl.h>
#include <ftw.h>
#include <glob.h>
#include <locale.h>
#include <mntent.h>
#include <stdarg.h>
#include <stdint.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdio_ext.h>
#include <string.h>
#include <sys/mman.h>
#include <sys/resource.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/vfs.h>
#include <unistd.h>
#include <wchar.h>

#include "libc-stdio.h"

#ifndef __GLIBC__

/* TODO: implement bionic_error, which will use these. */
unsigned int bionic_error_message_count = 0;
typeof(void (*)(void)) bionic_error_print_progname = NULL;

long long int bionic_strtoll_l(const char *restrict nptr, char **restrict endptr, int base, locale_t loc)
{
	return strtoll(nptr, endptr, base);
}

unsigned long long bionic_strtoull_l(const char *nptr, char **endptr, int base, locale_t) {
	return strtoull(nptr, endptr, base);
}

struct cmsghdr * bionic___cmsg_nxthdr(struct msghdr *msg, struct cmsghdr *cmsg) {
	return CMSG_NXTHDR(msg, cmsg);
}

char *bionic___strcat_chk(char *restrict dst, const char *restrict src)
{
	return strcat(dst, src);
}

/* musl has 64bit off_t on 32bit platforms. This is reasonable, but incompatible with bionic/glibc */

#ifndef __LP64__

typedef uint32_t off32_t;

void * bionic_mmap(void *address, size_t length, int protect, int flags, int filedes, off32_t offset)
{
	return mmap(address, length, protect, flags, filedes, offset);
}

int bionic_posix_fallocate(int fd, off32_t offset, off32_t length)
{
	return posix_fallocate(fd, offset, length);
}

off32_t bionic_lseek(int filedes, off32_t offset, int whence)
{
	return (off32_t)lseek(filedes, offset, whence);
}

ssize_t bionic_pread(int filedes, void *buffer, size_t size, off32_t offset)
{
	return pread(filedes, buffer, size, offset);
}

ssize_t bionic_pwrite(int filedes, const void *buffer, size_t size, off32_t offset)
{
	return pwrite(filedes, buffer, size, offset);
}

int bionic_truncate(const char *filename, off32_t length)
{
	return truncate(filename, length);
}

int bionic_ftruncate(int fd, off32_t length)
{
	return ftruncate(fd, length);
}

off32_t bionic_ftello(FILE *stream)
{
	return (off32_t)ftello(bionic_file_to_glibc_file(stream));
}

int bionic_fseeko(FILE *stream, off32_t offset, int whence)
{
	return fseeko(bionic_file_to_glibc_file(stream), offset, whence);
}
#else

off_t bionic_ftello(FILE *stream)
{
	return ftello(bionic_file_to_glibc_file(stream));
}

int bionic_fseeko(FILE *stream, off_t offset, int whence)
{
	return fseeko(bionic_file_to_glibc_file(stream), offset, whence);
}

#endif

/* since these are always 64bit on musl, the *64 versions don't necessarily exist */

int bionic_stat64(const char *filename, struct stat *buf)
{
	return stat(filename, buf);
}

int bionic_fstat64(int filedes, struct stat *buf)
{
	return fstat(filedes, buf);
}

int bionic_lstat64(const char *filename, struct stat *buf)
{
	return lstat(filename, buf);
}

int bionic_statfs64(const char *file, struct statfs *buf)
{
	return statfs(file, buf);
}
int bionic_statvfs64(const char *file, struct statfs *buf)
{
	return statfs(file, buf);
}

void * bionic_mmap64(void *address, size_t length, int protect, int flags, int filedes, off_t offset)
{
	return mmap(address, length, protect, flags, filedes, offset);
}

struct dirent * bionic_readdir64(DIR *dirstream)
{
	return readdir(dirstream);
}

int bionic_posix_fallocate64(int fd, off_t offset, off_t length)
{
	return posix_fallocate(fd, offset, length);
}

#define OPEN_NEEDS_MODE(flags) ((flags & O_CREAT) || (flags & O_TMPFILE))
int bionic_open(char *path, int oflag, ...);
int bionic_open64(char *filename, int flags, ...)
{
	int fd;
	int mode;

	if(OPEN_NEEDS_MODE(flags)) {
		va_list arg;
		va_start(arg, flags);
		mode = va_arg(arg, int);
		va_end(arg);

		fd = bionic_open(filename, flags, mode);
	} else {
		fd = bionic_open(filename, flags);
	}

	return fd;
}

int bionic_getrlimit64(int resource, struct rlimit *rlp)
{
	return getrlimit(resource, rlp);
}

int bionic_setrlimit64(int resource, const struct rlimit *rlp)
{
	return setrlimit(resource, rlp);
}

int bionic_aio_read64(struct aiocb *aiocbp)
{
	return aio_read(aiocbp);
}

int bionic_aio_write64(struct aiocb *aiocbp)
{
	return aio_write(aiocbp);
}

int bionic_lio_listio64(int mode, struct aiocb *const list[], int nent, struct sigevent *sig)
{
	return lio_listio(mode, list, nent, sig);
}

void bionic_globfree64(glob_t *pglob)
{
	globfree(pglob);
}

off_t bionic_lseek64(int filedes, off_t offset, int whence)
{
	return lseek(filedes, offset, whence);
}

ssize_t bionic_pread64(int filedes, void *buffer, size_t size, off_t offset)
{
	return pread(filedes, buffer, size, offset);
}

ssize_t bionic_pwrite64(int filedes, const void *buffer, size_t size, off_t offset)
{
	return pwrite(filedes, buffer, size, offset);
}

int bionic_truncate64(const char *name, off_t length)
{
	return truncate(name, length);
}

int bionic_ftruncate64(int id, off_t length)
{
	return ftruncate(id, length);
}

int bionic_ftw64(const char *filename, int (*fn)(const char *, const struct stat *, int), int descriptors)
{
	return ftw(filename, fn, descriptors);
}

int bionic_nftw64(const char *filename, int (*fn)(const char *, const struct stat *, int, struct FTW *), int descriptors, int flag)
{
	return nftw(filename, fn, descriptors, flag);
}

off_t bionic_ftello64(FILE *stream)
{
	return ftello(bionic_file_to_glibc_file(stream));
}

int bionic_fseeko64(FILE *stream, off_t offset, int whence)
{
	return fseeko(bionic_file_to_glibc_file(stream), offset, whence);
}

int bionic_aio_error64(const struct aiocb *aiocbp)
{
	return aio_error(aiocbp);
}

ssize_t bionic_aio_return64(struct aiocb *aiocbp)
{
	return aio_return(aiocbp);
}

int bionic_aio_cancel64(int fildes, struct aiocb *aiocbp)
{
	return aio_cancel(fildes, aiocbp);
}

FILE *bionic_fopen(char *path, const char *restrict mode);
FILE * bionic_fopen64(char *filename, const char *opentype)
{
	return bionic_fopen(filename, opentype);
}

FILE * bionic_freopen64(const char *filename, const char *opentype, FILE *stream)
{
	return freopen(filename, opentype, bionic_file_to_glibc_file(stream));
}

int bionic_scandir64(const char *dir, struct dirent ***namelist, int (*selector)(const struct dirent *), int (*cmp) (const struct dirent **, const struct dirent **))
{
	return scandir(dir, namelist, selector, cmp);
}

int bionic_alphasort64(const struct dirent **a, const struct dirent **b)
{
	return alphasort(a, b);
}

int bionic_versionsort64(const struct dirent **a, const struct dirent **b)
{
	return versionsort(a, b);
}

int bionic_fgetpos64(FILE *stream, fpos_t *position)
{
	return fgetpos(bionic_file_to_glibc_file(stream), position);
}

int bionic_fsetpos64(FILE *stream, const fpos_t *position)
{
	return fsetpos(bionic_file_to_glibc_file(stream), position);
}

FILE * bionic_tmpfile64(void)
{
	return tmpfile();
}

int bionic_glob64(const char *pattern, int flags, int(*errfunc)(const char *, int), glob_t *vector_ptr)
{
	return glob(pattern, flags, errfunc, vector_ptr);
}

int bionic_aio_fsync64(int op, struct aiocb *aiocbp)
{
	return aio_fsync(op, aiocbp);
}

int bionic_aio_suspend64(const struct aiocb *const list[], int nent, const struct timespec *timeout)
{
	return aio_suspend(list, nent, timeout);
}

/* the rest of functions that use FILE (see libc-stdio) */

ssize_t bionic_getline(char **lineptr, size_t *n, FILE *stream)
{
	return getline(lineptr, n, bionic_file_to_glibc_file(stream));
}

ssize_t bionic_getdelim(char **lineptr, size_t *n, int delimiter, FILE *stream)
{
	return getdelim(lineptr, n, delimiter, bionic_file_to_glibc_file(stream));
}

char * bionic_fgets(char *s, int count, FILE *stream)
{
	return fgets(s, count, bionic_file_to_glibc_file(stream));
}

wchar_t * bionic_fgetws(wchar_t *ws, int count, FILE *stream)
{
	return fgetws(ws, count, bionic_file_to_glibc_file(stream));
}

char * bionic_fgets_unlocked(char *s, int count, FILE *stream)
{
	return fgets_unlocked(s, count, bionic_file_to_glibc_file(stream));
}

wchar_t * bionic_fgetws_unlocked(wchar_t *ws, int count, FILE *stream)
{
	return fgetws_unlocked(ws, count, bionic_file_to_glibc_file(stream));
}

int bionic_endmntent(FILE *stream)
{
	return endmntent(bionic_file_to_glibc_file(stream));
}

struct mntent * bionic_getmntent(FILE *stream)
{
	return getmntent(bionic_file_to_glibc_file(stream));
}

struct mntent * bionic_getmntent_r(FILE *stream, struct mntent *result, char *buffer, int bufsize)
{
	return getmntent_r(bionic_file_to_glibc_file(stream), result, buffer, bufsize);
}

int bionic_addmntent(FILE *stream, const struct mntent *mnt)
{
	return addmntent(bionic_file_to_glibc_file(stream), mnt);
}

int bionic_setvbuf(FILE *stream, char *buf, int mode, size_t size)
{
	return setvbuf(bionic_file_to_glibc_file(stream), buf, mode, size);
}

void bionic_setbuf(FILE *stream, char *buf)
{
	setbuf(bionic_file_to_glibc_file(stream), buf);
}

void bionic_setbuffer(FILE *stream, char *buf, size_t size)
{
	setbuffer(bionic_file_to_glibc_file(stream), buf, size);
}

void bionic_setlinebuf(FILE *stream)
{
	setlinebuf(bionic_file_to_glibc_file(stream));
}

int bionic___flbf(FILE *stream)
{
	return __flbf(bionic_file_to_glibc_file(stream));
}

size_t bionic___fbufsize(FILE *stream)
{
	return __fbufsize(bionic_file_to_glibc_file(stream));
}

size_t bionic___fpending(FILE *stream)
{
	return __fpending(bionic_file_to_glibc_file(stream));
}

int bionic_fclose(FILE *stream)
{
	return fclose(bionic_file_to_glibc_file(stream));
}

int bionic_fprintf(FILE *restrict f, const char *restrict fmt, ...)
{
	int ret;
	va_list ap;
	va_start(ap, fmt);
	ret = vfprintf(bionic_file_to_glibc_file(f), fmt, ap);
	va_end(ap);
	return ret;
}

int bionic_vfprintf(FILE *stream, const char *template, va_list ap)
{
	return vfprintf(bionic_file_to_glibc_file(stream), template, ap);
}

int bionic_vfwprintf(FILE *stream, const wchar_t *template, va_list ap)
{
	return vfwprintf(bionic_file_to_glibc_file(stream), template, ap);
}

int bionic_fgetc(FILE *stream)
{
	return fgetc(bionic_file_to_glibc_file(stream));
}

wint_t bionic_fgetwc(FILE *stream)
{
	return fgetwc(bionic_file_to_glibc_file(stream));
}

int bionic_fgetc_unlocked(FILE *stream)
{
	return fgetc_unlocked(bionic_file_to_glibc_file(stream));
}

wint_t bionic_fgetwc_unlocked(FILE *stream)
{
	return fgetwc_unlocked(bionic_file_to_glibc_file(stream));
}

int bionic_getc(FILE *stream)
{
	return getc(bionic_file_to_glibc_file(stream));
}

wint_t bionic_getwc(FILE *stream)
{
	return getwc(bionic_file_to_glibc_file(stream));
}

int bionic_getc_unlocked(FILE *stream)
{
	return getc_unlocked(bionic_file_to_glibc_file(stream));
}

wint_t bionic_getwc_unlocked(FILE *stream)
{
	return getwc_unlocked(bionic_file_to_glibc_file(stream));
}

int bionic_getw(FILE *stream)
{
	return getw(bionic_file_to_glibc_file(stream));
}

void bionic_flockfile(FILE *stream)
{
	flockfile(bionic_file_to_glibc_file(stream));
}

int bionic_ftrylockfile(FILE *stream)
{
	return ftrylockfile(bionic_file_to_glibc_file(stream));
}

void bionic_funlockfile(FILE *stream)
{
	funlockfile(bionic_file_to_glibc_file(stream));
}

int bionic___fsetlocking(FILE *stream, int type)
{
	return __fsetlocking(bionic_file_to_glibc_file(stream), type);
}

void bionic_clearerr(FILE *stream)
{
	clearerr(bionic_file_to_glibc_file(stream));
}

void bionic_clearerr_unlocked(FILE *stream)
{
	clearerr_unlocked(bionic_file_to_glibc_file(stream));
}

int bionic_fflush_unlocked(FILE *stream)
{
	return fflush_unlocked(bionic_file_to_glibc_file(stream));
}

void bionic___fpurge(FILE *stream)
{
	__fpurge(bionic_file_to_glibc_file(stream));
}

int bionic_feof(FILE *stream)
{
	return feof(bionic_file_to_glibc_file(stream));
}

int bionic_feof_unlocked(FILE *stream)
{
	return feof_unlocked(bionic_file_to_glibc_file(stream));
}

int bionic_ferror(FILE *stream)
{
	return ferror(bionic_file_to_glibc_file(stream));
}

int bionic_ferror_unlocked(FILE *stream)
{
	return ferror_unlocked(bionic_file_to_glibc_file(stream));
}

int bionic_fputc(int c, FILE *stream)
{
	return fputc(c, bionic_file_to_glibc_file(stream));
}

wint_t bionic_fputwc(wchar_t wc, FILE *stream)
{
	return fputwc(wc, bionic_file_to_glibc_file(stream));
}

int bionic_fputc_unlocked(int c, FILE *stream)
{
	return fputc_unlocked(c, bionic_file_to_glibc_file(stream));
}

wint_t bionic_fputwc_unlocked(wchar_t wc, FILE *stream)
{
	return fputwc_unlocked(wc, bionic_file_to_glibc_file(stream));
}

wint_t bionic_putwc(wchar_t wc, FILE *stream)
{
	return putwc(wc, bionic_file_to_glibc_file(stream));
}

int bionic_putc_unlocked(int c, FILE *stream)
{
	return putc_unlocked(c, bionic_file_to_glibc_file(stream));
}

wint_t bionic_putwc_unlocked(wchar_t wc, FILE *stream)
{
	return putwc_unlocked(wc, bionic_file_to_glibc_file(stream));
}

int bionic_fputs(const char *s, FILE *stream)
{
	return fputs(s, bionic_file_to_glibc_file(stream));
}

int bionic_fputws(const wchar_t *ws, FILE *stream)
{
	return fputws(ws, bionic_file_to_glibc_file(stream));
}

int bionic_fputs_unlocked(const char *s, FILE *stream)
{
	return fputs_unlocked(s, bionic_file_to_glibc_file(stream));
}

int bionic_fputws_unlocked(const wchar_t *ws, FILE *stream)
{
	return fputws_unlocked(ws, bionic_file_to_glibc_file(stream));
}

int bionic_putw(int w, FILE *stream)
{
	return putw(w, bionic_file_to_glibc_file(stream));
}

int bionic_ungetc(int c, FILE *stream)
{
	return ungetc(c, bionic_file_to_glibc_file(stream));
}

wint_t bionic_ungetwc(wint_t wc, FILE *stream)
{
	return ungetwc(wc, bionic_file_to_glibc_file(stream));
}

int bionic_pclose(FILE *stream)
{
	return pclose(bionic_file_to_glibc_file(stream));
}

long int bionic_ftell(FILE *stream)
{
	return ftell(bionic_file_to_glibc_file(stream));
}

int bionic_fseek(FILE *stream, long int offset, int whence)
{
	return fseek(bionic_file_to_glibc_file(stream), offset, whence);
}

void bionic_rewind(FILE *stream)
{
	rewind(bionic_file_to_glibc_file(stream));
}

FILE * bionic_freopen(const char *filename, const char *opentype, FILE *stream)
{
	return freopen(filename, opentype, bionic_file_to_glibc_file(stream));
}

int bionic___freadable(FILE *stream)
{
	return __freadable(bionic_file_to_glibc_file(stream));
}

int bionic___fwritable(FILE *stream)
{
	return __fwritable(bionic_file_to_glibc_file(stream));
}

int bionic___freading(FILE *stream)
{
	return __freading(bionic_file_to_glibc_file(stream));
}

int bionic___fwriting(FILE *stream)
{
	return __fwriting(bionic_file_to_glibc_file(stream));
}

int bionic_fileno(FILE *stream)
{
	return fileno(bionic_file_to_glibc_file(stream));
}

int bionic_fileno_unlocked(FILE *stream)
{
	return fileno_unlocked(bionic_file_to_glibc_file(stream));
}

size_t bionic_fread(void *data, size_t size, size_t count, FILE *stream)
{
	return fread(data, size, count, bionic_file_to_glibc_file(stream));
}

size_t bionic_fread_unlocked(void *data, size_t size, size_t count, FILE *stream)
{
	return fread_unlocked(data, size, count, bionic_file_to_glibc_file(stream));
}

size_t bionic_fwrite_unlocked(const void *data, size_t size, size_t count, FILE *stream)
{
	return fwrite_unlocked(data, size, count, bionic_file_to_glibc_file(stream));
}

int bionic_fgetpos(FILE *stream, fpos_t *position)
{
	return fgetpos(bionic_file_to_glibc_file(stream), position);
}

int bionic_fsetpos(FILE *stream, const fpos_t *position)
{
	return fsetpos(bionic_file_to_glibc_file(stream), position);
}

int bionic_vfscanf(FILE *stream, const char *template, va_list ap)
{
	return vfscanf(bionic_file_to_glibc_file(stream), template, ap);
}

int bionic_vfwscanf(FILE *stream, const wchar_t *template, va_list ap)
{
	return vfwscanf(bionic_file_to_glibc_file(stream), template, ap);
}

int bionic_fwide(FILE *stream, int mode)
{
	return fwide(bionic_file_to_glibc_file(stream), mode);
}

#endif

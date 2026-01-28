#include <errno.h>
#include <locale.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// this seems to not be a stable ABI, so hopefully nobody treats it as such
// FIXME: move this out into it's own file whenever we get to implementing it more properly
struct prop_info {
	const char *name;
};

#define PROP_NAME_MAX  32
#define PROP_VALUE_MAX 92

const struct prop_info *bionic___system_property_find(const char *name)
{
	struct prop_info *ret = malloc(sizeof(struct prop_info));
	ret->name = name;

	return ret;
}

int bionic___system_property_read(const struct prop_info *prop_info, char *name, char *value)
{
	if (name)
		strncpy(name, prop_info->name, PROP_NAME_MAX);

	if (!value)
		return 0;

	if (!strcmp(prop_info->name, "ro.build.fingerprint")) {
		strncpy(value, "", PROP_VALUE_MAX); // there is no good reason that apps should need this, so just return an empty string
	} else {
		printf("__system_property_find: >%s< not handled yet\n", prop_info->name);
		strncpy(value, "", PROP_VALUE_MAX);
	}

	return strlen(value);
}

// actually misc stuff

void android_set_abort_message(const char *msg)
{
	printf("android_set_abort_message called: '%s'\n", msg);
	//	exit(1);
}

// setlocale is a bit special on bionic, try to mimic the behavior
char *bionic_setlocale(int category, const char *locale)
{
	if(!locale)
		return setlocale(category, NULL);

	// list of allowed locales from bionic
	if (!strcmp(locale, "") ||
	    !strcmp(locale, "C.UTF-8") ||
	    !strcmp(locale, "en_US.UTF-8")) {
		return setlocale(category, "C.UTF-8");
	} else if (!strcmp(locale, "C") ||
		   !strcmp(locale, "POSIX")) {
		return setlocale(category, "C");
	} else {
		errno = ENOENT;
		return NULL;
	}
}

char *bionic_getenv(const char *name) {
	if(!name) {
		printf("!!! NULL passed to %s\n", __func__);
	}

	if(!strcmp(name, "HOME"))
		return NULL;

	/* no point returning anything else, apps mix this with using hardcoded paths */
	if(!strcmp(name, "ANDROID_ROOT"))
		return "/system";

	return getenv(name);
}

/* __gnu_strerror_r emulates glibc's strerror_r.
 * to make sure we get that, we define _GNU_SOURCE.
 * musl doesn't have any way to get the glibc behavior. */

#ifndef _GNU_SOURCE
#define _GNU_SOURCE
#endif

char* bionic___gnu_strerror_r(int error_number, char* buf, size_t buf_len) {
#ifdef __GLIBC__
	return strerror_r(error_number, buf, buf_len);
#else // musl, or something else POSIX compliant
	int saved_errno = errno;
	strerror_r(error_number, buf, buf_len);
	errno = saved_errno;
	return buf;
#endif
}

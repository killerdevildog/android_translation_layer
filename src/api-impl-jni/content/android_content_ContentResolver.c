#include <gio/gio.h>
#include <string.h>

#include "../defines.h"
#include "../generated_headers/android_content_ContentResolver.h"

JNIEXPORT void JNICALL Java_android_content_ContentResolver_native_1query_1file_1info(JNIEnv *env, jclass clazz, jstring path_jstr, jobjectArray attributes, jobjectArray results)
{
	const char *path = (*env)->GetStringUTFChars(env, path_jstr, NULL);
	GFile *file = g_file_new_for_path(path);
	GString *attrs = g_string_new("");
	for (int i = 0; i < (*env)->GetArrayLength(env, attributes); i++, g_string_append(attrs, ",")) {
		const char *attr = (*env)->GetStringUTFChars(env, (*env)->GetObjectArrayElement(env, attributes, i), NULL);
		if (!strcmp(attr, "_display_name")) {
			g_string_append(attrs, G_FILE_ATTRIBUTE_STANDARD_DISPLAY_NAME);
		} else if (!strcmp(attr, "mime_type")) {
			g_string_append(attrs, G_FILE_ATTRIBUTE_STANDARD_CONTENT_TYPE);
		}
		(*env)->ReleaseStringUTFChars(env, (*env)->GetObjectArrayElement(env, attributes, i), attr);
	}
	GError *error = NULL;
	GFileInfo *file_info = g_file_query_info(file, attrs->str, G_FILE_QUERY_INFO_NONE, NULL, &error);
	g_string_free(attrs, TRUE);
	if (error) {
		g_error_free(error);
		(*env)->ReleaseStringUTFChars(env, path_jstr, path);
		return;
	}
	for (int i = 0; i < (*env)->GetArrayLength(env, attributes); i++) {
		const char *attr = (*env)->GetStringUTFChars(env, (*env)->GetObjectArrayElement(env, attributes, i), NULL);
		if (!strcmp(attr, "_display_name")) {
			jstring name = _JSTRING(g_file_info_get_attribute_string(file_info, G_FILE_ATTRIBUTE_STANDARD_DISPLAY_NAME));
			(*env)->SetObjectArrayElement(env, results, i, name);
		} else if (!strcmp(attr, "mime_type")) {
			jstring mime_type = _JSTRING(g_content_type_get_mime_type(g_file_info_get_content_type(file_info)));
			(*env)->SetObjectArrayElement(env, results, i, mime_type);
		}
		(*env)->ReleaseStringUTFChars(env, (*env)->GetObjectArrayElement(env, attributes, i), attr);
	}
	g_object_unref(file_info);
	(*env)->ReleaseStringUTFChars(env, path_jstr, path);
}

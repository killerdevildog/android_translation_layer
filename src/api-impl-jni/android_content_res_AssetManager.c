#include <errno.h>
#include <fcntl.h>
#include <stddef.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>

#include <androidfw/androidfw_c_api.h>

#include "defines.h"
#include "util.h"
#include "generated_headers/android_content_res_AssetManager.h"

#include <dirent.h>
#include <glib.h>

#define JAVA_ENUM_CLASS android_content_res_AssetManager
enum {
	JAVA_ENUM(STYLE_TYPE),
	JAVA_ENUM(STYLE_DATA),
	JAVA_ENUM(STYLE_ASSET_COOKIE),
	JAVA_ENUM(STYLE_RESOURCE_ID),
	JAVA_ENUM(STYLE_CHANGING_CONFIGURATIONS),
	JAVA_ENUM(STYLE_DENSITY),
	JAVA_ENUM(STYLE_NUM_ENTRIES),
};
#undef JAVA_ENUM_CLASS

#define JAVA_COOKIE(cookie)   (cookie != -1 ? (jint)(cookie + 1) : -1)
#define NATIVE_COOKIE(cookie) (cookie != -1 ? (ApkAssetsCookie)(cookie - 1) : -1)

void _AssetManager_unlock(struct AssetManager **asset_manager)
{
	AssetManager_unlock(*asset_manager);
}

#define AM_SCOPEDLOCK(asset_manager)      \
	AssetManager_lock(asset_manager); \
	__attribute__((__cleanup__(_AssetManager_unlock))) struct AssetManager *_RESERVED_am = asset_manager;

JNIEXPORT jlong JNICALL Java_android_content_res_AssetManager_openAsset(JNIEnv *env, jobject this, jstring _file_name, jint mode)
{
	const char *file_name = _CSTRING(_file_name);

	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	struct Asset *asset = AssetManager_openNonAsset(asset_manager, file_name, mode);
	android_log_printf(ANDROID_LOG_VERBOSE, "[" __FILE__ "]", "AssetManager_openAsset(%p, %s, %d) returns %p\n", asset_manager, file_name, mode, asset);

	return _INTPTR(asset);
}

JNIEXPORT jint JNICALL Java_android_content_res_AssetManager_openAssetFd(JNIEnv *env, jobject this, jstring _file_name, jint mode, jlongArray _offset, jlongArray _size)
{
	int fd;
	off_t offset;
	off_t size;

	const char *file_name = _CSTRING(_file_name);

	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	struct Asset *asset = AssetManager_openNonAsset(asset_manager, file_name, mode);
	android_log_printf(ANDROID_LOG_VERBOSE, "[" __FILE__ "]", "AssetManager_openAssetFd(%p, %s, %d, ...)\n", asset_manager, file_name, mode);

	fd = Asset_openFileDescriptor(asset, &offset, &size);

	(*env)->SetLongArrayRegion(env, _offset, 0, 1, (jlong[]){offset});
	(*env)->SetLongArrayRegion(env, _size, 0, 1, (jlong[]){size});

	return fd;
}

JNIEXPORT jlong JNICALL Java_android_content_res_AssetManager_getAssetLength(JNIEnv *env, jobject this, jlong _asset)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)

	struct Asset *asset = _PTR(_asset);
	return Asset_getLength(asset);
}

JNIEXPORT jlong JNICALL Java_android_content_res_AssetManager_getAssetRemainingLength(JNIEnv *env, jobject this, jlong _asset)
{
	struct Asset *asset = _PTR(_asset);
	return Asset_getRemainingLength(asset);
}

JNIEXPORT jint JNICALL Java_android_content_res_AssetManager_readAsset(JNIEnv *env, jobject this, jlong _asset, jbyteArray b, jlong offset, jlong length)
{
	int ret;
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)

	struct Asset *asset = _PTR(_asset);
	jbyte *array = _GET_BYTE_ARRAY_ELEMENTS(b);
	ret = Asset_read(asset, &array[offset], length);
	_RELEASE_BYTE_ARRAY_ELEMENTS(b, array);

	return ret;
}

JNIEXPORT jint JNICALL Java_android_content_res_AssetManager_readAssetChar(JNIEnv *env, jobject this, jlong _asset)
{
	int ret;
	uint8_t byte;
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)

	struct Asset *asset = _PTR(_asset);
	ret = Asset_read(asset, &byte, 1);
	return (ret == 1) ? byte : -1;
}

JNIEXPORT jlong JNICALL Java_android_content_res_AssetManager_seekAsset(JNIEnv *env, jobject this, jlong _asset, jlong offset, jint whence)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)

	struct Asset *asset = _PTR(_asset);
	return Asset_seek(asset, offset, whence);
}

JNIEXPORT void JNICALL Java_android_content_res_AssetManager_destroyAsset(JNIEnv *env, jobject this, jlong _asset)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)

	struct Asset *asset = _PTR(_asset);
	Asset_delete(asset);
}

JNIEXPORT void JNICALL Java_android_content_res_AssetManager_init(JNIEnv *env, jobject this, jint sdk_version)
{
	struct AssetManager *asset_manager = AssetManager_new();
	const struct ResTable_config config = {
		.density = /*ACONFIGURATION_DENSITY_MEDIUM*/ 160,
		.sdkVersion = sdk_version,
	};
	AssetManager_setConfiguration(asset_manager, &config);
	_SET_LONG_FIELD(this, "mObject", _INTPTR(asset_manager));
}

JNIEXPORT void JNICALL Java_android_content_res_AssetManager_native_1setApkAssets(JNIEnv *env, jobject this, jobjectArray paths, int num_assets)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	const struct ApkAssets *apk_assets[num_assets];
	for (int i = 0; i < num_assets; i++) {
		jstring path_jstr = (jstring)((*env)->GetObjectArrayElement(env, paths, i));
		const char *path = (*env)->GetStringUTFChars(env, path_jstr, NULL);
		if (path[strlen(path) - 1] == '/')
			apk_assets[i] = ApkAssets_loadDir(strdup(path));
		else
			apk_assets[i] = ApkAssets_load(strdup(path), false);
		(*env)->ReleaseStringUTFChars(env, path_jstr, path);
	}
	AssetManager_setApkAssets(asset_manager, apk_assets, num_assets, true, true);
}

JNIEXPORT jint JNICALL Java_android_content_res_AssetManager_loadResourceValue(JNIEnv *env, jobject this, jint ident, jshort density, jobject outValue, jboolean resolve)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	uint32_t resId = ident;
	struct Res_value value;
	uint32_t outSpecFlags;
	struct ResTable_config outConfig;
	uint32_t ref;
	ApkAssetsCookie cookie = AssetManager_getResource(asset_manager, resId, false, density,
	                                                  &value, &outConfig, &outSpecFlags);
	if (resolve) {
		cookie = AssetManager_resolveReference(asset_manager, cookie,
		                                       &value, &outConfig,
		                                       &outSpecFlags, &ref);
	}
	if (cookie >= 0) {
		_SET_INT_FIELD(outValue, "type", value.dataType);
		_SET_INT_FIELD(outValue, "data", value.data);
		_SET_INT_FIELD(outValue, "resourceId", resId);
		_SET_INT_FIELD(outValue, "assetCookie", JAVA_COOKIE(cookie));
		if (value.dataType == TYPE_STRING) {
			const struct ResStringPool *string_pool = AssetManager_getStringPoolForCookie(asset_manager, cookie);
			size_t len;
			const char16_t *string = ResStringPool_stringAt(string_pool, value.data, &len);
			_SET_OBJ_FIELD(outValue, "string", "Ljava/lang/CharSequence;", (*env)->NewString(env, string, len));
		} else {
			_SET_OBJ_FIELD(outValue, "string", "Ljava/lang/CharSequence;", NULL);
		}
	}
	return JAVA_COOKIE(cookie);
}

JNIEXPORT jobjectArray JNICALL Java_android_content_res_AssetManager_getArrayStringResource(JNIEnv *env, jobject this, jint ident)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	const struct ResolvedBag *bag = AssetManager_getBag(asset_manager, ident);
	jobjectArray array = (*env)->NewObjectArray(env, bag->entry_count, (*env)->FindClass(env, "java/lang/String"), NULL);
	for (int i = 0; i < bag->entry_count; i++) {
		const struct ResolvedBag_Entry entry = bag->entries[i];
		struct Res_value value = entry.value;
		struct ResTable_config outConfig;
		uint32_t outSpecFlags;
		uint32_t ref;
		ApkAssetsCookie cookie = AssetManager_resolveReference(asset_manager, entry.cookie,
		                                                       &value, &outConfig,
		                                                       &outSpecFlags, &ref);
		if (value.dataType == TYPE_STRING) {
			const struct ResStringPool *string_pool = AssetManager_getStringPoolForCookie(asset_manager, cookie);
			if (string_pool == NULL)
				continue;
			size_t len;
			const char16_t *string = ResStringPool_stringAt(string_pool, value.data, &len);
			(*env)->SetObjectArrayElement(env, array, i, (*env)->NewString(env, string, len));
		}
	}

	return array;
}

JNIEXPORT jint JNICALL Java_android_content_res_AssetManager_getResourceIdentifier(JNIEnv *env, jobject this, jstring name_jstr, jstring type_jstr, jstring package_jstr)
{
	const char *name = "";
	const char *type = "";
	const char *package = "";
	uint32_t ret;

	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	if (name_jstr)
		name = (*env)->GetStringUTFChars(env, name_jstr, NULL);
	if (type_jstr)
		type = (*env)->GetStringUTFChars(env, type_jstr, NULL);
	if (package_jstr)
		package = (*env)->GetStringUTFChars(env, package_jstr, NULL);

	ret = AssetManager_getResourceId(asset_manager, name, type, package);
	if (name_jstr)
		(*env)->ReleaseStringUTFChars(env, name_jstr, name);
	if (type_jstr)
		(*env)->ReleaseStringUTFChars(env, type_jstr, type);
	if (package_jstr)
		(*env)->ReleaseStringUTFChars(env, package_jstr, package);
	return ret;
}

JNIEXPORT jobject JNICALL Java_android_content_res_AssetManager_getPooledString(JNIEnv *env, jobject this, jint cookie, jint index)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	const struct ResStringPool *string_pool = AssetManager_getStringPoolForCookie(asset_manager, NATIVE_COOKIE(cookie));
	size_t len;
	const char16_t *string = ResStringPool_stringAt(string_pool, index, &len);
	return (*env)->NewString(env, string, len);
}

JNIEXPORT jlong JNICALL Java_android_content_res_AssetManager_newTheme(JNIEnv *env, jobject this)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	struct Theme *theme = AssetManager_newTheme(asset_manager);
	return _INTPTR(theme);
}

JNIEXPORT void JNICALL Java_android_content_res_AssetManager_deleteTheme(JNIEnv *env, jobject this, jlong theme_ptr)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)

	Theme_delete(_PTR(theme_ptr));
}

JNIEXPORT void JNICALL Java_android_content_res_AssetManager_applyThemeStyle(JNIEnv *env, jobject this, jlong theme_ptr, jint styleRes, jboolean force)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)

	Theme_applyStyle(_PTR(theme_ptr), styleRes, force);
}

JNIEXPORT jint JNICALL Java_android_content_res_AssetManager_loadThemeAttributeValue(JNIEnv *env, jobject this, jlong theme_ptr, jint ident, jobject outValue, jboolean resolve)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	struct Theme *theme = _PTR(theme_ptr);
	struct Res_value value;
	uint32_t outSpecFlags;
	struct ResTable_config outConfig;
	uint32_t ref;
	int cookie = Theme_getAttribute(theme, ident, &value, &outSpecFlags);
	if (resolve) {
		cookie = AssetManager_resolveReference(asset_manager, cookie,
		                                       &value, &outConfig,
		                                       &outSpecFlags, &ref);
	}
	if (cookie >= 0) {
		_SET_INT_FIELD(outValue, "type", value.dataType);
		_SET_INT_FIELD(outValue, "data", value.data);
		_SET_INT_FIELD(outValue, "resourceId", ref);
		_SET_INT_FIELD(outValue, "assetCookie", JAVA_COOKIE(cookie));
		if (value.dataType == TYPE_STRING) {
			const struct ResStringPool *string_pool = AssetManager_getStringPoolForCookie(asset_manager, cookie);
			size_t len;
			const char16_t *string = ResStringPool_stringAt(string_pool, value.data, &len);
			_SET_OBJ_FIELD(outValue, "string", "Ljava/lang/CharSequence;", (*env)->NewString(env, string, len));
		} else {
			_SET_OBJ_FIELD(outValue, "string", "Ljava/lang/CharSequence;", NULL);
		}
	}
	return JAVA_COOKIE(cookie);
}

/* function ported from AOSP - Copyright 2006, The Android Open Source Project */
JNIEXPORT jboolean JNICALL Java_android_content_res_AssetManager_resolveAttrs(JNIEnv *env, jobject this,
                                                                              jlong theme_ptr, jint def_style_attr,
                                                                              jint def_style_res, jintArray java_values,
                                                                              jintArray java_attrs, jintArray out_java_values,
                                                                              jintArray out_java_indices)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)

	struct Theme *theme = _PTR(theme_ptr);
	const jsize attrs_len = (*env)->GetArrayLength(env, java_attrs);
	const jsize out_values_len = (*env)->GetArrayLength(env, out_java_values);
	if (out_values_len < (attrs_len * STYLE_NUM_ENTRIES)) {
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/IndexOutOfBoundsException"), "outValues too small");
		return false;
	}

	jint *attrs = (jint *)(*env)->GetPrimitiveArrayCritical(env, java_attrs, NULL);
	if (attrs == NULL) {
		return true;
	}

	jint *values = NULL;
	jsize values_len = 0;
	if (java_values != NULL) {
		values_len = (*env)->GetArrayLength(env, java_values);
		values = (jint *)(*env)->GetPrimitiveArrayCritical(env, java_values, NULL);
		if (values == NULL) {
			(*env)->ReleasePrimitiveArrayCritical(env, java_attrs, attrs, JNI_ABORT);
			return false;
		}
	}

	jint *out_values = (jint *)(*env)->GetPrimitiveArrayCritical(env, out_java_values, NULL);
	if (!out_values) {
		(*env)->ReleasePrimitiveArrayCritical(env, java_attrs, attrs, JNI_ABORT);
		if (values) {
			(*env)->ReleasePrimitiveArrayCritical(env, java_values, values, JNI_ABORT);
		}
		return false;
	}

	jint *out_indices = NULL;
	if (out_java_indices) {
		jsize out_indices_len = (*env)->GetArrayLength(env, out_java_indices);
		if (out_indices_len > attrs_len) {
			out_indices = (jint *)(*env)->GetPrimitiveArrayCritical(env, out_java_indices, NULL);
			if (!out_indices) {
				(*env)->ReleasePrimitiveArrayCritical(env, java_attrs, attrs, JNI_ABORT);
				if (values)
					(*env)->ReleasePrimitiveArrayCritical(env, java_values, values, JNI_ABORT);
				(*env)->ReleasePrimitiveArrayCritical(env, out_java_values, out_values, JNI_ABORT);
				return false;
			}
		}
	}

	bool ret = ResolveAttrs(theme, def_style_attr, def_style_res,
	                        (uint32_t *)values, values_len,
	                        (uint32_t *)attrs, attrs_len,
	                        (uint32_t *)out_values, (uint32_t *)out_indices);
	if (out_indices)
		(*env)->ReleasePrimitiveArrayCritical(env, out_java_indices, out_indices, 0);

	(*env)->ReleasePrimitiveArrayCritical(env, out_java_values, out_values, 0);

	if (values)
		(*env)->ReleasePrimitiveArrayCritical(env, java_values, values, JNI_ABORT);

	(*env)->ReleasePrimitiveArrayCritical(env, java_attrs, attrs, JNI_ABORT);
	return ret;
}

JNIEXPORT jboolean JNICALL Java_android_content_res_AssetManager_retrieveAttributes(JNIEnv *env, jobject this,
                                                                                    jlong parser_ptr,
                                                                                    jintArray java_attrs, jint attrs_len,
                                                                                    jlong out_values, jlong out_indices)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	struct ResXMLParser *parser = (struct ResXMLParser *)_PTR(parser_ptr);

	jint *attrs = (*env)->GetIntArrayElements(env, java_attrs, 0);

	jboolean ret = RetrieveAttributes(asset_manager, parser, (uint32_t *)attrs, attrs_len, (uint32_t *)_PTR(out_values), (uint32_t *)_PTR(out_indices));

	(*env)->ReleaseIntArrayElements(env, java_attrs, attrs, JNI_ABORT);

	return ret;
}

JNIEXPORT void JNICALL Java_android_content_res_AssetManager_applyStyle(JNIEnv *env, jobject this,
                                                                        jlong theme_ptr, jlong parser_ptr,
                                                                        jint def_style_attr, jint def_style_res,
                                                                        jintArray java_attrs, jint attrs_len,
                                                                        jlong out_values, jlong out_indices)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)

	struct Theme *theme = _PTR(theme_ptr);
	struct ResXMLParser *parser = (struct ResXMLParser *)_PTR(parser_ptr);

	jint *attrs = (*env)->GetIntArrayElements(env, java_attrs, 0);

	ApplyStyle(theme, parser, def_style_attr, def_style_res, (uint32_t *)attrs, attrs_len, (uint32_t *)_PTR(out_values), (uint32_t *)_PTR(out_indices));

	(*env)->ReleaseIntArrayElements(env, java_attrs, attrs, JNI_ABORT);
}

JNIEXPORT jint JNICALL Java_android_content_res_AssetManager_getArraySize(JNIEnv *env, jobject this, jint ident)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	const struct ResolvedBag *bag = AssetManager_getBag(asset_manager, ident);
	return bag->entry_count;
}

JNIEXPORT jint JNICALL Java_android_content_res_AssetManager_retrieveArray(JNIEnv *env, jobject this, jint ident, jintArray outArray)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)

	jint *array = (*env)->GetIntArrayElements(env, outArray, NULL);
	const struct ResolvedBag *bag = AssetManager_getBag(asset_manager, ident);
	for (int i = 0; i < bag->entry_count; i++) {
		const struct ResolvedBag_Entry entry = bag->entries[i];
		struct Res_value value = entry.value;
		struct ResTable_config outConfig;
		uint32_t outSpecFlags;
		uint32_t ref;
		ApkAssetsCookie cookie = AssetManager_resolveReference(asset_manager, entry.cookie,
		                                                       &value, &outConfig,
		                                                       &outSpecFlags, &ref);

		array[i * STYLE_NUM_ENTRIES + STYLE_TYPE] = value.dataType;
		array[i * STYLE_NUM_ENTRIES + STYLE_DATA] = value.data;
		array[i * STYLE_NUM_ENTRIES + STYLE_ASSET_COOKIE] = JAVA_COOKIE(cookie);
		array[i * STYLE_NUM_ENTRIES + STYLE_RESOURCE_ID] = ref;
	}
	(*env)->ReleaseIntArrayElements(env, outArray, array, 0);
	return bag->entry_count;
}

JNIEXPORT jstring JNICALL Java_android_content_res_AssetManager_getResourceName(JNIEnv *env, jobject this, jint ident)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	struct resource_name res_name;
	bool ret = AssetManager_getResourceName(asset_manager, ident, &res_name);
	if (!ret)
		return NULL;

	const gchar *type = res_name.type ?: g_utf16_to_utf8(res_name.type16, res_name.type_len, NULL, NULL, NULL);
	const gchar *entry = res_name.entry ?: g_utf16_to_utf8(res_name.entry16, res_name.entry_len, NULL, NULL, NULL);

	gchar *result = g_strdup_printf("%.*s:%.*s/%.*s",
	                                res_name.package ? (int)res_name.package_len : 0,
	                                res_name.package ?: "",
	                                type ? (int)res_name.type_len : 0,
	                                type ?: "",
	                                entry ? (int)res_name.entry_len : 0,
	                                entry ?: "");
	jstring result_jstr = _JSTRING(result);
	free(result);
	return result_jstr;
}

JNIEXPORT jstring JNICALL Java_android_content_res_AssetManager_getResourcePackageName(JNIEnv *env, jobject this, jint ident)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	struct resource_name res_name;
	bool ret = AssetManager_getResourceName(asset_manager, ident, &res_name);
	return (ret && res_name.package) ? (*env)->NewStringUTF(env, res_name.package) : NULL;
}

JNIEXPORT jstring JNICALL Java_android_content_res_AssetManager_getResourceTypeName(JNIEnv *env, jobject this, jint ident)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	struct resource_name res_name;
	bool ret = AssetManager_getResourceName(asset_manager, ident, &res_name);
	if (!ret)
		return NULL;

	if (res_name.type)
		return (*env)->NewStringUTF(env, res_name.type);
	else if (res_name.type16)
		return (*env)->NewString(env, res_name.type16, res_name.type_len);
	else
		return NULL;
}

JNIEXPORT jstring JNICALL Java_android_content_res_AssetManager_getResourceEntryName(JNIEnv *env, jobject this, jint ident)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	struct resource_name res_name;
	bool ret = AssetManager_getResourceName(asset_manager, ident, &res_name);
	if (!ret)
		return NULL;

	if (res_name.entry)
		return (*env)->NewStringUTF(env, res_name.entry);
	else if (res_name.entry16)
		return (*env)->NewString(env, res_name.entry16, res_name.entry_len);
	else
		return NULL;
}

JNIEXPORT jint JNICALL Java_android_content_res_AssetManager_loadResourceBagValue(JNIEnv *env, jobject this, jint ident, jint bagEntryId, jobject outValue, jboolean resolve)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	ApkAssetsCookie cookie = -1;

	const struct ResolvedBag *bag = AssetManager_getBag(asset_manager, ident);
	if (!bag)
		return -1;
	for (int i = 0; i < bag->entry_count; i++) {
		const struct ResolvedBag_Entry entry = bag->entries[i];
		if (entry.key == bagEntryId) {
			struct Res_value value = entry.value;
			struct ResTable_config outConfig;
			uint32_t outSpecFlags;
			uint32_t ref;
			cookie = AssetManager_resolveReference(asset_manager, entry.cookie,
			                                       &value, &outConfig,
			                                       &outSpecFlags, &ref);

			_SET_INT_FIELD(outValue, "type", value.dataType);
			_SET_INT_FIELD(outValue, "data", value.data);
			_SET_INT_FIELD(outValue, "resourceId", ref);
			_SET_INT_FIELD(outValue, "assetCookie", JAVA_COOKIE(cookie));
			if (value.dataType == TYPE_STRING) {
				const struct ResStringPool *string_pool = AssetManager_getStringPoolForCookie(asset_manager, cookie);
				size_t len;
				const char16_t *string = ResStringPool_stringAt(string_pool, value.data, &len);
				_SET_OBJ_FIELD(outValue, "string", "Ljava/lang/CharSequence;", (*env)->NewString(env, string, len));
			} else {
				_SET_OBJ_FIELD(outValue, "string", "Ljava/lang/CharSequence;", NULL);
			}
			break;
		}
	}

	return JAVA_COOKIE(cookie);
}

JNIEXPORT void JNICALL Java_android_content_res_AssetManager_copyTheme(JNIEnv *env, jobject this, jlong dest, jlong src)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)

	Theme_setTo(_PTR(dest), _PTR(src));
}

JNIEXPORT void JNICALL Java_android_content_res_AssetManager_setConfiguration(
    JNIEnv *env, jobject this, jint mcc, jint mnc, jstring locale,
    jint orientation, jint touchscreen, jint density, jint keyboard,
    jint keyboardHidden, jint navigation, jint screenWidth, jint screenHeight,
    jint smallestScreenWidthDp, jint screenWidthDp, jint screenHeightDp,
    jint screenLayout, jint uiMode, jint majorVersion)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	const struct ResTable_config config = {
		.mcc = mcc,
		.mnc = mnc,
		.orientation = orientation,
		.touchscreen = touchscreen,
		.density = density,
		.keyboard = keyboard,
		.navigation = navigation,
		.screenWidth = screenWidth,
		.screenHeight = screenHeight,
		.smallestScreenWidthDp = smallestScreenWidthDp,
		.screenWidthDp = screenWidthDp,
		.screenHeightDp = screenHeightDp,
		.screenLayout = screenLayout,
		.uiMode = uiMode,
		.sdkVersion = majorVersion
	};
	AssetManager_setConfiguration(asset_manager, &config);
}

JNIEXPORT jobjectArray JNICALL Java_android_content_res_AssetManager_list(JNIEnv *env, jobject this, jstring path_jstr)
{
	const char *path = "";

	path = (*env)->GetStringUTFChars(env, path_jstr, NULL);
	if (!path_jstr)
		return NULL;

	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)

	struct AssetDir *asset_dir = AssetManager_openDir(asset_manager, path);
	(*env)->ReleaseStringUTFChars(env, path_jstr, path);
	if (!asset_dir) {
		(*env)->ThrowNew(env, (*env)->FindClass(env, "java/io/FileNotFoundException"), path);
		return NULL;
	}

	const size_t file_count = AssetDir_getFileCount(asset_dir);

	jobjectArray array = (*env)->NewObjectArray(env, file_count, (*env)->FindClass(env, "java/lang/String"), NULL);
	if (!array)
		return NULL;

	for (size_t i = 0; i < file_count; i++) {
		const char *asset = AssetDir_getFileName(asset_dir, i);
		jstring asset_jstr = (*env)->NewStringUTF(env, asset);
		(*env)->SetObjectArrayElement(env, array, i, (*env)->NewStringUTF(env, asset));
		(*env)->DeleteLocalRef(env, asset_jstr);
	}

	return array;
}

JNIEXPORT jlong JNICALL Java_android_content_res_AssetManager_openXmlAssetNative(JNIEnv *env, jobject this, jint cookie, jstring _file_name)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	const char *file_name = (*env)->GetStringUTFChars(env, _file_name, NULL);
	struct Asset *asset = AssetManager_openNonAsset(asset_manager, file_name, ACCESS_BUFFER);
	(*env)->ReleaseStringUTFChars(env, _file_name, file_name);

	struct ResXMLTree *res_xml = ResXMLTree_new();
	ResXMLTree_setTo(res_xml, Asset_getBuffer(asset, true), Asset_getLength(asset), true);
	Asset_delete(asset);
	return _INTPTR(res_xml);
}

JNIEXPORT jobjectArray JNICALL Java_android_content_res_AssetManager_getLocales(JNIEnv *env, jobject this)
{
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(this, "mObject"));
	AM_SCOPEDLOCK(asset_manager)
	char **locales = AssetManager_getLocales(asset_manager, false, true);
	int i = 0;
	while (locales[i] != NULL)
		i++;
	jobjectArray array = (*env)->NewObjectArray(env, i, (*env)->FindClass(env, "java/lang/String"), NULL);
	for (i = 0; locales[i] != NULL; i++) {
		(*env)->SetObjectArrayElement(env, array, i, (*env)->NewStringUTF(env, locales[i]));
		free(locales[i]);
	}
	free(locales);
	return array;
}

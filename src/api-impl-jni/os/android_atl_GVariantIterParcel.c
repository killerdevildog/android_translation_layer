#include <glib.h>

#include "../defines.h"

#include "../generated_headers/android_atl_GVariantIterParcel.h"

JNIEXPORT jbyte JNICALL Java_android_atl_GVariantIterParcel_native_1readByte(JNIEnv *env, jclass clazz, jlong iter_ptr)
{
	GVariantIter *iter = (GVariantIter *)iter_ptr;
	jbyte b = 0;
	if (iter)
		g_variant_iter_next(iter, "y", &b);
	return b;
}

JNIEXPORT jint JNICALL Java_android_atl_GVariantIterParcel_native_1readInt(JNIEnv *env, jclass clazz, jlong iter_ptr)
{
	GVariantIter *iter = (GVariantIter *)iter_ptr;
	jint i = 0;
	if (iter)
		g_variant_iter_next(iter, "i", &i);
	return i;
}

JNIEXPORT jstring JNICALL Java_android_atl_GVariantIterParcel_native_1readString(JNIEnv *env, jclass clazz, jlong iter_ptr)
{
	GVariantIter *iter = (GVariantIter *)iter_ptr;
	const char *s = NULL;
	if (iter)
		g_variant_iter_next(iter, "ms", &s);
	return s ? _JSTRING(s) : NULL;
}

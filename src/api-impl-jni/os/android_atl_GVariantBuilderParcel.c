#include <glib.h>

#include "../generated_headers/android_atl_GVariantBuilderParcel.h"

JNIEXPORT void JNICALL Java_android_atl_GVariantBuilderParcel_native_1writeByte(JNIEnv *env, jclass clazz, jlong builder_ptr, jbyte value)
{
	GVariantBuilder *builder = (GVariantBuilder *)builder_ptr;
	if (builder)
		g_variant_builder_add(builder, "y", value);
}

JNIEXPORT void JNICALL Java_android_atl_GVariantBuilderParcel_native_1writeInt(JNIEnv *env, jclass clazz, jlong builder_ptr, jint value)
{
	GVariantBuilder *builder = (GVariantBuilder *)builder_ptr;
	if (builder)
		g_variant_builder_add(builder, "i", value);
}

JNIEXPORT void JNICALL Java_android_atl_GVariantBuilderParcel_native_1writeString(JNIEnv *env, jclass clazz, jlong builder_ptr, jstring value_jstr)
{
	GVariantBuilder *builder = (GVariantBuilder *)builder_ptr;
	if (builder) {
		const char *value = value_jstr ? (*env)->GetStringUTFChars(env, value_jstr, NULL) : NULL;
		g_variant_builder_add(builder, "ms", value);
		if (value_jstr)
			(*env)->ReleaseStringUTFChars(env, value_jstr, value);
	}
}

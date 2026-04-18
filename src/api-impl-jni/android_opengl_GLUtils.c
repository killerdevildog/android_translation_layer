#include <GLES3/gl3.h>
#include <jni.h>

#include "../api-impl-jni/defines.h"
#include "generated_headers/android_opengl_GLUtils.h"
#include "../libandroid/bitmap.h"

int get_internal_format(int32_t bitmapformat)
{
	switch (bitmapformat) {
		case ANDROID_BITMAP_FORMAT_A_8:
			return GL_ALPHA;
		case ANDROID_BITMAP_FORMAT_RGBA_4444:
			return GL_RGBA;
		case ANDROID_BITMAP_FORMAT_RGBA_8888:
			return GL_RGBA;
		case ANDROID_BITMAP_FORMAT_RGB_565:
			return GL_RGB;
		case ANDROID_BITMAP_FORMAT_RGBA_F16:
			return GL_RGBA16F;
		case ANDROID_BITMAP_FORMAT_RGBA_1010102:
			return GL_RGB10_A2;
		default:
			return -1;
	}
}

int get_type(int32_t bitmapformat)
{
	switch (bitmapformat) {
		case ANDROID_BITMAP_FORMAT_A_8:
			return GL_UNSIGNED_BYTE;
		case ANDROID_BITMAP_FORMAT_RGBA_4444:
			return GL_UNSIGNED_SHORT_4_4_4_4;
		case ANDROID_BITMAP_FORMAT_RGBA_8888:
			return GL_UNSIGNED_BYTE;
		case ANDROID_BITMAP_FORMAT_RGB_565:
			return GL_UNSIGNED_SHORT_5_6_5;
		case ANDROID_BITMAP_FORMAT_RGBA_F16:
			return GL_HALF_FLOAT;
		case ANDROID_BITMAP_FORMAT_RGBA_1010102:
			return GL_UNSIGNED_INT_2_10_10_10_REV;
		default:
			return -1;
	}
}

int get_pixel_format_from_internal_format(uint32_t internalformat)
{
	switch (internalformat) {
		case GL_RGBA16F:
		case GL_SRGB8_ALPHA8:
			return GL_RGBA;
		default:
			return internalformat;
	}
}

JNIEXPORT jint JNICALL Java_android_opengl_GLUtils_native_1texImage2D(
    JNIEnv *env, jclass this, jint target, jint level, jint internalformat,
    jobject bitmap, jint type, jint border)
{
	struct AndroidBitmapInfo bitmap_info;
	AndroidBitmap_getInfo(env, bitmap, &bitmap_info);
	if (internalformat < 0) {
		internalformat = get_internal_format(bitmap_info.format);
	}
	if (type < 0) {
		type = get_type(bitmap_info.format);
	}

	GLsizei width = bitmap_info.width;
	GLsizei height = bitmap_info.height;
	GLenum format = get_pixel_format_from_internal_format(internalformat);
	glTexImage2D(target, level, internalformat, width, height, border, format,
	             type, _PTR(_GET_LONG_FIELD(bitmap, "bytes")));
	return 0;
}

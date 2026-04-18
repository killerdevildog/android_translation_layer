#ifndef BITMAP_H
#define BITMAP_H

#include <jni.h>
#include <stdint.h>

enum {
	ANDROID_BITMAP_FORMAT_A_8,
	ANDROID_BITMAP_FORMAT_RGBA_4444,
	ANDROID_BITMAP_FORMAT_RGBA_8888,
	ANDROID_BITMAP_FORMAT_RGB_565,
	ANDROID_BITMAP_FORMAT_RGBA_F16,
	ANDROID_BITMAP_FORMAT_RGBA_1010102,
};

struct AndroidBitmapInfo {
	uint32_t width;
	uint32_t height;
	uint32_t stride;
	int32_t format;
	uint32_t flags;
};

int AndroidBitmap_getInfo(JNIEnv *env, jobject bitmap,
                          struct AndroidBitmapInfo *info);

#endif

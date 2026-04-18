#include <glib.h>
#include <gsk/gsk.h>
#include <gtk/gtk.h>

#include "../defines.h"
#include "AndroidPaint.h"
#include "pango/pango-font.h"

#include "../generated_headers/android_graphics_Paint.h"

JNIEXPORT jlong JNICALL Java_android_graphics_Paint_native_1create(JNIEnv *env, jclass clazz)
{
	struct AndroidPaint *paint = g_new0(struct AndroidPaint, 1);
	paint->color.alpha = 1.f;
	paint->gsk_stroke = gsk_stroke_new(1);
	paint->font = pango_font_description_new();
	paint->is_fill = true;
	return _INTPTR(paint);
}

JNIEXPORT jlong JNICALL Java_android_graphics_Paint_native_1clone(JNIEnv *env, jclass clazz, jlong paint_ptr)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	struct AndroidPaint *clone = g_memdup2(paint, sizeof(struct AndroidPaint));
	clone->gsk_stroke = gsk_stroke_copy(paint->gsk_stroke);
	clone->font = pango_font_description_copy(paint->font);
	return _INTPTR(clone);
}

JNIEXPORT void JNICALL Java_android_graphics_Paint_native_1recycle(JNIEnv *env, jclass clazz, jlong paint_ptr)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	gsk_stroke_free(paint->gsk_stroke);
	pango_font_description_free(paint->font);
	g_free(paint);
}

JNIEXPORT void JNICALL Java_android_graphics_Paint_native_1set_1color(JNIEnv *env, jclass clazz, jlong paint_ptr, jint color)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	paint->color.red = ((color >> 16) & 0xFF) / 255.f;
	paint->color.green = ((color >> 8) & 0xFF) / 255.f;
	paint->color.blue = ((color >> 0) & 0xFF) / 255.f;
	paint->color.alpha = ((color >> 24) & 0xFF) / 255.f;
}

JNIEXPORT jint JNICALL Java_android_graphics_Paint_native_1get_1color(JNIEnv *env, jclass clazz, jlong paint_ptr)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	return ((int)(paint->color.red * 0xFF) << 16) + ((int)(paint->color.green * 0xFF) << 8) + ((int)(paint->color.blue * 0xFF) << 0) + ((int)(paint->color.alpha * 0xFF) << 24);
}

JNIEXPORT void JNICALL Java_android_graphics_Paint_native_1set_1alpha(JNIEnv *env, jclass clazz, jlong paint_ptr, jint alpha)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	paint->color.alpha = (alpha & 0xFF) / 255.f;
}

JNIEXPORT int JNICALL Java_android_graphics_Paint_native_1get_1alpha(JNIEnv *env, jclass clazz, jlong paint_ptr)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	return (int)(paint->color.alpha * 0xFF);
}

#define STYLE_FILL            0
#define STYLE_STROKE          1
#define STYLE_FILL_AND_STROKE 2

JNIEXPORT void JNICALL Java_android_graphics_Paint_native_1set_1style(JNIEnv *env, jclass clazz, jlong paint_ptr, jint style)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	paint->is_fill = style == STYLE_FILL || style == STYLE_FILL_AND_STROKE;
	paint->is_stroke = style == STYLE_STROKE || style == STYLE_FILL_AND_STROKE;
}

JNIEXPORT jint JNICALL Java_android_graphics_Paint_native_1get_1style(JNIEnv *env, jclass clazz, jlong paint_ptr)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	if (paint->is_fill && paint->is_stroke)
		return STYLE_FILL_AND_STROKE;
	else if (paint->is_fill)
		return STYLE_FILL;
	else
		return STYLE_STROKE;
}

JNIEXPORT void JNICALL Java_android_graphics_Paint_native_1set_1stroke_1width(JNIEnv *env, jclass clazz, jlong paint_ptr, jfloat width)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	/* TODO: width of 0 means "single pixel width, no matter what",
	 * meanwile width of 1 should care about scaling;
	 * The problem is, 0 is not a valid value for gsk_stroke_set_line_width,
	 * so at least change it to 1 for now */
	if (width == 0)
		width = 1;
	gsk_stroke_set_line_width(paint->gsk_stroke, width);
}

JNIEXPORT jfloat JNICALL Java_android_graphics_Paint_native_1get_1stroke_1width(JNIEnv *env, jclass clazz, jlong paint_ptr)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	return gsk_stroke_get_line_width(paint->gsk_stroke);
}

JNIEXPORT void JNICALL Java_android_graphics_Paint_native_1set_1stroke_1cap(JNIEnv *env, jclass clazz, jlong paint_ptr, jint cap)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	gsk_stroke_set_line_cap(paint->gsk_stroke, cap);
}

JNIEXPORT jint JNICALL Java_android_graphics_Paint_native_1get_1stroke_1cap(JNIEnv *env, jclass clazz, jlong paint_ptr)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	return gsk_stroke_get_line_cap(paint->gsk_stroke);
}

JNIEXPORT void JNICALL Java_android_graphics_Paint_native_1set_1stroke_1join(JNIEnv *env, jclass clazz, jlong paint_ptr, jint join)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	gsk_stroke_set_line_join(paint->gsk_stroke, join);
}

JNIEXPORT jint JNICALL Java_android_graphics_Paint_native_1get_1stroke_1join(JNIEnv *env, jclass clazz, jlong paint_ptr)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	return gsk_stroke_get_line_join(paint->gsk_stroke);
}

JNIEXPORT void JNICALL Java_android_graphics_Paint_native_1set_1text_1size(JNIEnv *env, jclass clazz, jlong paint_ptr, jfloat size)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	pango_font_description_set_absolute_size(paint->font, roundf(size * PANGO_SCALE));
}

JNIEXPORT jfloat JNICALL Java_android_graphics_Paint_native_1get_1text_1size(JNIEnv *env, jclass clazz, jlong paint_ptr)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	return (jfloat)pango_font_description_get_size(paint->font) / PANGO_SCALE;
}

JNIEXPORT void JNICALL Java_android_graphics_Paint_native_1set_1color_1filter(JNIEnv *env, jclass clazz, jlong paint_ptr, jint mode, jint color)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	/* clang-format off */
	graphene_matrix_init_from_float(&paint->color_matrix, (float[]){
		0, 0, 0, 0,
		0, 0, 0, 0,
		0, 0, 0, 0,
		0, 0, 0, ((color >> 24) & 0xFF) / 255.f,
	});
	/* clang-format on */
	graphene_vec4_init(&paint->color_offset, ((color >> 16) & 0xFF) / 255.f, ((color >> 8) & 0xFF) / 255.f, ((color >> 0) & 0xFF) / 255.f, 0);
	paint->use_color_filter = mode != -1;
}

extern GtkWidget *window;

JNIEXPORT void JNICALL Java_android_graphics_Paint_native_1get_1text_1bounds(JNIEnv *env, jclass clazz, jlong paint_ptr, jstring text_ptr, jobject bounds)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	PangoLayout *layout = pango_layout_new(gtk_widget_get_pango_context(window));
	pango_layout_set_font_description(layout, paint->font);
	const char *str = (*env)->GetStringUTFChars(env, text_ptr, NULL);
	pango_layout_set_text(layout, str, -1);
	(*env)->ReleaseStringUTFChars(env, text_ptr, str);
	PangoRectangle rect;
	pango_layout_get_pixel_extents(layout, NULL, &rect);
	rect.y -= (float)pango_layout_get_baseline(layout) / PANGO_SCALE;
	if (paint->alignment == PANGO_ALIGN_CENTER)
		rect.x -= rect.width / 2.f;
	else if (paint->alignment == PANGO_ALIGN_RIGHT)
		rect.x -= rect.width;
	_SET_INT_FIELD(bounds, "left", rect.x);
	_SET_INT_FIELD(bounds, "top", rect.y);
	_SET_INT_FIELD(bounds, "right", rect.x + rect.width);
	_SET_INT_FIELD(bounds, "bottom", rect.y + rect.height);
	g_object_unref(layout);
}

JNIEXPORT void JNICALL Java_android_graphics_Paint_native_1set_1text_1align(JNIEnv *env, jclass clazz, jlong paint_ptr, jint align)
{
	struct AndroidPaint *paint = _PTR(paint_ptr);
	paint->alignment = align;
}

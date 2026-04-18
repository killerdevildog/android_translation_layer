#include <gtk/gtk.h>

#include "../defines.h"
#include "../util.h"

#include "WrapperWidget.h"

#include "../generated_headers/android_widget_ImageView.h"

#define SCALE_TYPE_MATRIX        0
#define SCALE_TYPE_FIT_XY        1
#define SCALE_TYPE_FIT_START     2
#define SCALE_TYPE_FIT_CENTER    3
#define SCALE_TYPE_FIT_END       4
#define SCALE_TYPE_CENTER        5
#define SCALE_TYPE_CENTER_CROP   6
#define SCALE_TYPE_CENTER_INSIDE 7

JNIEXPORT jlong JNICALL Java_android_widget_ImageView_native_1constructor(JNIEnv *env, jobject this, jobject context, jobject attrs)
{
	GtkWidget *wrapper = g_object_ref(wrapper_widget_new());
	GtkWidget *image = gtk_picture_new();
	wrapper_widget_set_child(WRAPPER_WIDGET(wrapper), image);
	wrapper_widget_set_jobject(WRAPPER_WIDGET(wrapper), env, this);
	return _INTPTR(image);
}

JNIEXPORT void JNICALL Java_android_widget_ImageView_native_1setDrawable(JNIEnv *env, jobject this, jlong widget_ptr, jlong paintable_ptr)
{
	GtkPicture *picture = _PTR(widget_ptr);
	GdkPaintable *paintable = _PTR(paintable_ptr);
	gtk_picture_set_paintable(picture, paintable);
}

JNIEXPORT void JNICALL Java_android_widget_ImageView_native_1setScaleType(JNIEnv *env, jobject this, jlong widget_ptr, jint scale_type)
{
	GtkPicture *picture = _PTR(widget_ptr);
	/* TODO: somehow handle all the types */
	switch (scale_type) {
		case SCALE_TYPE_FIT_XY:
			gtk_picture_set_content_fit(picture, GTK_CONTENT_FIT_FILL);
			break;
		case SCALE_TYPE_CENTER:
			/* should probably let it overflow instead */
			gtk_picture_set_content_fit(picture, GTK_CONTENT_FIT_SCALE_DOWN);
			break;
		case SCALE_TYPE_CENTER_CROP:
			gtk_picture_set_content_fit(picture, GTK_CONTENT_FIT_COVER);
			break;
		case SCALE_TYPE_CENTER_INSIDE:
			gtk_picture_set_content_fit(picture, GTK_CONTENT_FIT_CONTAIN);
			break;
	}
}

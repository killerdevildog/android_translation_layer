#include <gtk/gtk.h>

#include "../defines.h"
#include "../util.h"

#include "WrapperWidget.h"

#include "../generated_headers/android_widget_ImageButton.h"

JNIEXPORT jlong JNICALL Java_android_widget_ImageButton_native_1constructor(JNIEnv *env, jobject this, jobject context, jobject attrs)
{
	GtkWidget *wrapper = g_object_ref(wrapper_widget_new());
	GtkWidget *button = gtk_button_new();
	GtkWidget *image = gtk_picture_new_for_resource("/org/gtk/libgtk/icons/16x16/status/image-missing.png"); // show "broken image" icon
	gtk_button_set_child(GTK_BUTTON(button), image);
	wrapper_widget_set_child(WRAPPER_WIDGET(wrapper), button);
	wrapper_widget_consume_touch_events(WRAPPER_WIDGET(wrapper)); // Android button consumes touch events
	wrapper_widget_set_jobject(WRAPPER_WIDGET(wrapper), env, this);

	return _INTPTR(button);
}

static void clicked_cb(GtkWidget *button, gpointer user_data)
{
	printf("clicked_cb\n");
	JNIEnv *env = get_jni_env();
	WrapperWidget *wrapper = WRAPPER_WIDGET(gtk_widget_get_parent(button));

	(*env)->CallBooleanMethod(env, wrapper->jobj, handle_cache.view.performClick);

	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
}

JNIEXPORT void JNICALL Java_android_widget_ImageButton_nativeSetOnClickListener(JNIEnv *env, jobject this, jlong widget_ptr)
{
	GtkWidget *button = GTK_WIDGET(_PTR(widget_ptr));
	g_signal_handlers_disconnect_matched(button, G_SIGNAL_MATCH_FUNC, 0, 0, NULL, clicked_cb, NULL);

	g_signal_connect(button, "clicked", G_CALLBACK(clicked_cb), NULL);
}

JNIEXPORT void JNICALL Java_android_widget_ImageButton_native_1setDrawable(JNIEnv *env, jobject this, jlong widget_ptr, jlong paintable_ptr)
{
	GtkButton *button = _PTR(widget_ptr);
	GtkPicture *picture = GTK_PICTURE(gtk_button_get_child(GTK_BUTTON(button)));
	GdkPaintable *paintable = _PTR(paintable_ptr);
	gtk_picture_set_paintable(picture, paintable);

	gtk_widget_add_css_class(GTK_WIDGET(button), "ATL-no-border");
}

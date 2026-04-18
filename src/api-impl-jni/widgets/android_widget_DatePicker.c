#include <gtk/gtk.h>

#include "../defines.h"
#include "../util.h"

#include "WrapperWidget.h"

#include "../generated_headers/android_widget_Button.h"

JNIEXPORT jlong JNICALL Java_android_widget_DatePicker_native_1constructor(JNIEnv *env, jobject this, jobject context, jobject attrs)
{
	GtkWidget *wrapper = g_object_ref(wrapper_widget_new());
	GtkWidget *calendar = gtk_calendar_new();

	wrapper_widget_set_child(WRAPPER_WIDGET(wrapper), calendar);
	wrapper_widget_set_jobject(WRAPPER_WIDGET(wrapper), env, this);

	return _INTPTR(calendar);
}

static void day_selected_cb(GtkWidget *calendar, gpointer user_data)
{
	JNIEnv *env = get_jni_env();
	WrapperWidget *wrapper = WRAPPER_WIDGET(gtk_widget_get_parent(calendar));

	(*env)->CallVoidMethod(env, wrapper->jobj, handle_cache.date_picker.onDateChange);

	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
}

JNIEXPORT void JNICALL Java_android_widget_DatePicker_nativeSetOnDateChangedListener(JNIEnv *env, jobject this, jlong widget_ptr)
{
	GtkWidget *calendar = GTK_WIDGET(_PTR(widget_ptr));
	g_signal_handlers_disconnect_matched(calendar, G_SIGNAL_MATCH_FUNC, 0, 0, NULL, day_selected_cb, NULL);

	g_signal_connect(calendar, "day-selected", G_CALLBACK(day_selected_cb), NULL);
}

JNIEXPORT jint JNICALL Java_android_widget_DatePicker_nativeGetYear(JNIEnv *env, jobject this, jlong widget_ptr)
{
	GtkCalendar *calendar = _PTR(widget_ptr);
	return gtk_calendar_get_year(calendar);
}

JNIEXPORT jint JNICALL Java_android_widget_DatePicker_nativeGetMonth(JNIEnv *env, jobject this, jlong widget_ptr)
{
	GtkCalendar *calendar = _PTR(widget_ptr);
	return gtk_calendar_get_month(calendar);
}

JNIEXPORT jint JNICALL Java_android_widget_DatePicker_nativeGetDay(JNIEnv *env, jobject this, jlong widget_ptr)
{
	GtkCalendar *calendar = _PTR(widget_ptr);
	return gtk_calendar_get_day(calendar);
}

JNIEXPORT void JNICALL Java_android_widget_DatePicker_nativeUpdateDate(JNIEnv *env, jobject this, jlong widget_ptr, jint year, jint month, jint day)
{
	GtkCalendar *calendar = _PTR(widget_ptr);
	gtk_calendar_set_year(calendar, year);
	gtk_calendar_set_month(calendar, month);
	gtk_calendar_set_day(calendar, day);
}

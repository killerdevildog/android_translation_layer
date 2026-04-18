#include <gtk/gtk.h>

#include "../defines.h"
#include "../util.h"

#include "WrapperWidget.h"

#include "../generated_headers/android_widget_Button.h"

static gboolean transform_spin_txt_cb(GtkSpinButton *widget, gpointer user_data G_GNUC_UNUSED)
{
	char *text = g_strdup_printf("%02d", gtk_spin_button_get_value_as_int(widget));
	gtk_editable_set_text(GTK_EDITABLE(widget), text);
	g_free(text);
	return TRUE;
}

JNIEXPORT jlong JNICALL Java_android_widget_TimePicker_native_1constructor(JNIEnv *env, jobject this, jobject context, jobject attrs)
{
	GtkWidget *wrapper = g_object_ref(wrapper_widget_new());
	GtkWidget *vbox = gtk_box_new(GTK_ORIENTATION_VERTICAL, 10);
	gtk_widget_set_margin_start(vbox, 10);
	gtk_widget_set_margin_top(vbox, 10);

	GtkWidget *time_select_hbox = gtk_box_new(GTK_ORIENTATION_HORIZONTAL, 10);

	// add hour input
	GtkWidget *hour_vbox = gtk_box_new(GTK_ORIENTATION_VERTICAL, 0);
	GtkWidget *hours_spin_btn = gtk_spin_button_new_with_range(0.0, 24.0, 1.0);
	gtk_orientable_set_orientation(GTK_ORIENTABLE(hours_spin_btn), GTK_ORIENTATION_VERTICAL);
	gtk_widget_set_size_request(hour_vbox, 60, -1);
	gtk_widget_set_tooltip_text(hours_spin_btn, "Hours");
	gtk_box_append(GTK_BOX(hour_vbox), hours_spin_btn);
	gtk_box_append(GTK_BOX(time_select_hbox), hour_vbox);
	g_signal_connect(GTK_SPIN_BUTTON(hours_spin_btn), "output", G_CALLBACK(transform_spin_txt_cb), NULL);

	// : label
	GtkWidget *spacer_label = gtk_label_new(":");
	gtk_box_append(GTK_BOX(time_select_hbox), spacer_label);

	// minute input
	GtkWidget *minute_vbox = gtk_box_new(GTK_ORIENTATION_VERTICAL, 0);
	GtkWidget *minutes_spin_btn = gtk_spin_button_new_with_range(0.0, 60.0, 1.0);
	gtk_orientable_set_orientation(GTK_ORIENTABLE(minutes_spin_btn), GTK_ORIENTATION_VERTICAL);
	gtk_widget_set_size_request(minute_vbox, 59, -1);
	gtk_widget_set_tooltip_text(minutes_spin_btn, "Minutes");
	gtk_box_append(GTK_BOX(minute_vbox), minutes_spin_btn);
	gtk_box_append(GTK_BOX(time_select_hbox), minute_vbox);
	g_signal_connect(GTK_SPIN_BUTTON(minutes_spin_btn), "output", G_CALLBACK(transform_spin_txt_cb), NULL);
	gtk_box_append(GTK_BOX(vbox), time_select_hbox);

	// set time button
	GtkWidget *button_hbox = gtk_box_new(GTK_ORIENTATION_HORIZONTAL, 10);
	GtkWidget *button = gtk_button_new();
	GtkWidget *box = gtk_box_new(GTK_ORIENTATION_HORIZONTAL, 0);
	gtk_button_set_child(GTK_BUTTON(button), box);
	gtk_box_append(GTK_BOX(box), gtk_label_new("Set time"));

	gtk_box_append(GTK_BOX(button_hbox), button);
	gtk_box_append(GTK_BOX(vbox), button_hbox);

	wrapper_widget_set_child(WRAPPER_WIDGET(wrapper), vbox);
	wrapper_widget_set_jobject(WRAPPER_WIDGET(wrapper), env, this);

	_SET_LONG_FIELD(this, "hour_spin_widget", _INTPTR(hours_spin_btn));
	_SET_LONG_FIELD(this, "minute_spin_widget", _INTPTR(minutes_spin_btn));
	_SET_LONG_FIELD(this, "btn_widget", _INTPTR(button));

	return _INTPTR(vbox);
}

JNIEXPORT void JNICALL Java_android_widget_TimePicker_nativeSetSpinBtnValue(JNIEnv *env, jobject this, jlong widget_ptr, jint value)
{
	GtkSpinButton *widget = _PTR(widget_ptr);
	gtk_spin_button_set_value(widget, value);
}

JNIEXPORT jint JNICALL Java_android_widget_TimePicker_nativeGetSpinBtnValue(JNIEnv *env, jobject this, jlong widget_ptr)
{
	GtkSpinButton *widget = _PTR(widget_ptr);
	return gtk_spin_button_get_value_as_int(widget);
}

static void clicked_cb(GtkWidget *button, gpointer user_data)
{
	JNIEnv *env = get_jni_env();
	WrapperWidget *wrapper = WRAPPER_WIDGET(gtk_widget_get_parent(gtk_widget_get_parent(gtk_widget_get_parent(button))));

	(*env)->CallVoidMethod(env, wrapper->jobj, handle_cache.time_picker.onTimeChange);

	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
}

JNIEXPORT void JNICALL Java_android_widget_TimePicker_nativeSetOnTimeChangedListener(JNIEnv *env, jobject this, jlong widget_ptr)
{
	GtkWidget *button = GTK_WIDGET(_PTR(widget_ptr));
	g_signal_handlers_disconnect_matched(button, G_SIGNAL_MATCH_FUNC, 0, 0, NULL, clicked_cb, NULL);

	g_signal_connect(button, "clicked", G_CALLBACK(clicked_cb), NULL);
}

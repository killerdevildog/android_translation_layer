#include <gtk/gtk.h>

#include "../util.h"
#include "WrapperWidget.h"

#include "../generated_headers/android_widget_CheckBox.h"
#include "jni.h"

JNIEXPORT jlong JNICALL Java_android_widget_CheckBox_native_1constructor(JNIEnv *env, jobject this, jobject context, jobject attrs)
{
	GtkWidget *wrapper = g_object_ref(wrapper_widget_new());
	GtkWidget *button = gtk_check_button_new();
	wrapper_widget_set_child(WRAPPER_WIDGET(wrapper), button);
	wrapper_widget_set_jobject(WRAPPER_WIDGET(wrapper), env, this);
	return _INTPTR(button);
}

JNIEXPORT void JNICALL Java_android_widget_CheckBox_setChecked(JNIEnv *env, jobject this, jboolean checked)
{
	gtk_check_button_set_active(GTK_CHECK_BUTTON(_PTR(_GET_LONG_FIELD(this, "widget"))), checked);
}

JNIEXPORT jboolean JNICALL Java_android_widget_CheckBox_isChecked(JNIEnv *env, jobject this)
{
	return gtk_check_button_get_active(GTK_CHECK_BUTTON(_PTR(_GET_LONG_FIELD(this, "widget"))));
}

static gboolean on_toggled(GtkCheckButton *self, jobject listener)
{
	JNIEnv *env = get_jni_env();
	WrapperWidget *wrapper = WRAPPER_WIDGET(gtk_widget_get_parent(GTK_WIDGET(self)));
	jmethodID on_check_changed = _METHOD(_CLASS(listener), "onCheckedChanged", "(Landroid/widget/CompoundButton;Z)V");
	gboolean state = gtk_check_button_get_active(self);
	(*env)->CallVoidMethod(env, listener, on_check_changed, wrapper->jobj, state);
	return FALSE;
}

JNIEXPORT void JNICALL Java_android_widget_CheckBox_setOnCheckedChangeListener(JNIEnv *env, jobject this, jobject listener)
{
	GtkCheckButton *button = GTK_CHECK_BUTTON(_PTR(_GET_LONG_FIELD(this, "widget")));

	g_signal_handlers_block_matched(button, G_SIGNAL_MATCH_FUNC, 0, 0, NULL, on_toggled, NULL);

	if (listener) {
		g_signal_connect(button, "toggled", G_CALLBACK(on_toggled), _REF(listener));
	}
}

JNIEXPORT void JNICALL Java_android_widget_CheckBox_native_1setText(JNIEnv *env, jobject this, jlong widget_ptr, jstring text)
{
	GtkCheckButton *button = GTK_CHECK_BUTTON(_PTR(widget_ptr));
	if (text == NULL) {
		gtk_check_button_set_label(button, NULL);
		return;
	}
	const char *text_str = (*env)->GetStringUTFChars(env, text, NULL);
	gtk_check_button_set_label(button, text_str);
	(*env)->ReleaseStringUTFChars(env, text, text_str);
}

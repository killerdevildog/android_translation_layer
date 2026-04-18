#include <gtk/gtk.h>

#include "../util.h"
#include "WrapperWidget.h"

#include "../generated_headers/android_widget_CompoundButton.h"
#include "jni.h"

JNIEXPORT jlong JNICALL Java_android_widget_CompoundButton_native_1constructor(JNIEnv *env, jobject this, jobject context, jobject attrs)
{
	GtkWidget *wrapper = g_object_ref(wrapper_widget_new());
	GtkWidget *box = gtk_box_new(GTK_ORIENTATION_HORIZONTAL, 0);
	GtkWidget *label = gtk_label_new("");
	gtk_widget_set_hexpand(label, TRUE);
	gtk_widget_set_halign(label, GTK_ALIGN_START);
	gtk_box_append(GTK_BOX(box), label);
	gtk_box_append(GTK_BOX(box), gtk_switch_new());
	wrapper_widget_set_child(WRAPPER_WIDGET(wrapper), box);
	wrapper_widget_set_jobject(WRAPPER_WIDGET(wrapper), env, this);
	return _INTPTR(box);
}

JNIEXPORT void JNICALL Java_android_widget_CompoundButton_setChecked(JNIEnv *env, jobject this, jboolean checked)
{
	gtk_switch_set_active(GTK_SWITCH(gtk_widget_get_last_child(_PTR(_GET_LONG_FIELD(this, "widget")))), checked);
}

JNIEXPORT jboolean JNICALL Java_android_widget_CompoundButton_isChecked(JNIEnv *env, jobject this)
{
	return gtk_switch_get_active(GTK_SWITCH(gtk_widget_get_last_child(_PTR(_GET_LONG_FIELD(this, "widget")))));
}

static gboolean on_state_set(GtkSwitch *self, gboolean state, jobject listener)
{
	JNIEnv *env = get_jni_env();
	WrapperWidget *wrapper = WRAPPER_WIDGET(gtk_widget_get_parent(gtk_widget_get_parent(GTK_WIDGET(self))));
	jmethodID on_check_changed = _METHOD(_CLASS(listener), "onCheckedChanged", "(Landroid/widget/CompoundButton;Z)V");
	(*env)->CallVoidMethod(env, listener, on_check_changed, wrapper->jobj, state);
	return FALSE;
}

JNIEXPORT void JNICALL Java_android_widget_CompoundButton_setOnCheckedChangeListener(JNIEnv *env, jobject this, jobject listener)
{
	GtkSwitch *switcher = GTK_SWITCH(gtk_widget_get_last_child(_PTR(_GET_LONG_FIELD(this, "widget"))));

	g_signal_handlers_block_matched(switcher, G_SIGNAL_MATCH_FUNC, 0, 0, NULL, on_state_set, NULL);

	if (listener) {
		g_signal_connect(switcher, "state-set", G_CALLBACK(on_state_set), _REF(listener));
	}
}

JNIEXPORT void JNICALL Java_android_widget_CompoundButton_native_1setText(JNIEnv *env, jobject this, jlong widget_ptr, jstring text)
{
	GtkLabel *label = GTK_LABEL(gtk_widget_get_first_child(_PTR(widget_ptr)));
	const char *nativeText = (*env)->GetStringUTFChars(env, text, NULL);
	gtk_label_set_text(label, nativeText);
	(*env)->ReleaseStringUTFChars(env, text, nativeText);
}

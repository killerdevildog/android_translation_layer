#include <gtk/gtk.h>
#include <jni.h>

#include "../defines.h"
#include "../util.h"
#include "../generated_headers/android_app_Dialog.h"

/* main app window */
extern GtkWindow *window;

static gboolean on_close_request(GtkWidget *dialog, jobject jobj)
{
	printf("on_close_request\n");
	JNIEnv *env = get_jni_env();
	jmethodID dismiss = _METHOD(_CLASS(jobj), "dismiss", "()V");
	(*env)->CallVoidMethod(env, jobj, dismiss);
	return FALSE;
}

JNIEXPORT jlong JNICALL Java_android_app_Dialog_nativeInit(JNIEnv *env, jobject this)
{
	GtkWidget *dialog = gtk_window_new();
	gtk_window_set_transient_for(GTK_WINDOW(dialog), window);
	gtk_window_set_child(GTK_WINDOW(dialog), gtk_box_new(GTK_ORIENTATION_VERTICAL, 1));
	g_signal_connect_swapped(dialog, "response", G_CALLBACK(gtk_window_destroy), dialog);
	g_signal_connect(GTK_WINDOW(dialog), "close-request", G_CALLBACK(on_close_request), _REF(this));
	return _INTPTR(g_object_ref(dialog));
}

JNIEXPORT void JNICALL Java_android_app_Dialog_nativeSetTitle(JNIEnv *env, jobject this, jlong ptr, jstring title)
{
	GtkWindow *dialog = GTK_WINDOW(_PTR(ptr));
	const char *nativeTitle = (*env)->GetStringUTFChars(env, title, NULL);
	gtk_window_set_title(dialog, nativeTitle);
	(*env)->ReleaseStringUTFChars(env, title, nativeTitle);
}

JNIEXPORT void JNICALL Java_android_app_Dialog_nativeSetContentView(JNIEnv *env, jobject this, jlong ptr, jlong widget_ptr)
{
	GtkWindow *dialog = GTK_WINDOW(_PTR(ptr));
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));

	gtk_window_set_child(dialog, gtk_widget_get_parent(widget));
}

JNIEXPORT void JNICALL Java_android_app_Dialog_nativeShow(JNIEnv *env, jobject this, jlong ptr)
{
	GtkWindow *dialog = GTK_WINDOW(_PTR(ptr));
	gtk_window_present(dialog);
}

JNIEXPORT void JNICALL Java_android_app_Dialog_nativeClose(JNIEnv *env, jobject this, jlong ptr)
{
	GtkWindow *dialog = GTK_WINDOW(_PTR(ptr));
	gtk_window_close(dialog);
}

JNIEXPORT jboolean JNICALL Java_android_app_Dialog_nativeIsShowing(JNIEnv *env, jobject this, jlong ptr)
{
	GtkWindow *dialog = GTK_WINDOW(_PTR(ptr));
	return gtk_widget_is_visible(GTK_WIDGET(dialog));
}

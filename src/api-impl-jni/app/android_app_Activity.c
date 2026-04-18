#include <gtk/gtk.h>
#include <libportal/portal.h>

#include <jni.h>
#include <string.h>

#include "../defines.h"
#include "../util.h"
#include "../generated_headers/android_app_Activity.h"
#include "../../main-executable/back_button.h"
#include "android_app_Activity.h"

static GList *activity_backlog = NULL;
static jobject activity_current = NULL;

static void activity_close(JNIEnv *env, jobject activity)
{
	// in case some exception was left unhandled in native code, print it here so we don't confuse it with an exception thrown by onDestroy
	if ((*env)->ExceptionCheck(env)) {
		fprintf(stderr, "activity.onDestroy: seems there was a pending exception... :");
		(*env)->ExceptionDescribe(env);
	}

	/* -- run the activity's onDestroy -- */
	(*env)->CallVoidMethod(env, activity, handle_cache.activity.onDestroy);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
}

static void activity_unfocus(JNIEnv *env, jobject activity)
{
	if (!_GET_BOOL_FIELD(activity, "paused")) {
		(*env)->CallVoidMethod(env, activity, handle_cache.activity.onPause);
		if ((*env)->ExceptionCheck(env))
			(*env)->ExceptionDescribe(env);
	}

	(*env)->CallVoidMethod(env, activity, handle_cache.activity.onStop);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);

	(*env)->CallVoidMethod(env, activity, handle_cache.activity.onWindowFocusChanged, false);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
}

static void activity_focus(JNIEnv *env, jobject activity)
{
	if (_GET_BOOL_FIELD(activity, "finishing"))
		return;

	(*env)->CallVoidMethod(env, activity, handle_cache.activity.onStart);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
	if (_GET_BOOL_FIELD(activity, "finishing"))
		return;

	(*env)->CallVoidMethod(env, activity, handle_cache.activity.onResume);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
	if (_GET_BOOL_FIELD(activity, "finishing"))
		return;

	(*env)->CallVoidMethod(env, activity, handle_cache.activity.onPostResume);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
	if (_GET_BOOL_FIELD(activity, "finishing"))
		return;

	(*env)->CallVoidMethod(env, activity, handle_cache.activity.onWindowFocusChanged, true);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
}

static void activity_update_current(JNIEnv *env)
{
	jobject activity_new = activity_backlog ? g_list_first(activity_backlog)->data : NULL;

	if (activity_current != activity_new) {
		if (activity_current)
			activity_unfocus(env, activity_current);
		activity_current = NULL;

		if (activity_new)
			activity_focus(env, activity_new);
		if (activity_new && _GET_BOOL_FIELD(activity_new, "finishing"))
			return;

		activity_current = activity_new;
	}

	if (activity_current != NULL) {
		jclass current_activity_class = (*env)->GetObjectClass(env, activity_current);
		jmethodID current_activity_on_back_pressed_method_id = (*env)->GetMethodID(env, current_activity_class, "onBackPressed", "()V");
		if ((*env)->ExceptionCheck(env))
			(*env)->ExceptionDescribe(env);

		if (g_list_length(activity_backlog) > 1 || handle_cache.activity.onBackPressed != current_activity_on_back_pressed_method_id) {
			back_button_set_sensitive(true);
		} else {
			back_button_set_sensitive(false);
		}
	}
}

void activity_window_ready(void)
{
	JNIEnv *env = get_jni_env();

	for (GList *l = activity_backlog; l != NULL; l = l->next) {
		(*env)->CallVoidMethod(env, l->data, handle_cache.activity.onWindowFocusChanged, true);
		if ((*env)->ExceptionCheck(env))
			(*env)->ExceptionDescribe(env);
	}
}

void current_activity_back_pressed(void)
{
	JNIEnv *env = get_jni_env();

	jclass current_activity_class = (*env)->GetObjectClass(env, activity_current);
	jmethodID current_activity_on_back_pressed_method_id = (*env)->GetMethodID(env, current_activity_class, "onBackPressed", "()V");
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);

	// Either a new activity was added to the backlog or the current activity's onBackPressed method was changed
	if (g_list_length(activity_backlog) > 1 || handle_cache.activity.onBackPressed != current_activity_on_back_pressed_method_id) {
		(*env)->CallVoidMethod(env, activity_current, handle_cache.activity.onBackPressed);
		if ((*env)->ExceptionCheck(env))
			(*env)->ExceptionDescribe(env);
	} else {
		back_button_set_sensitive(false);
	}
}

void activity_close_all(void)
{
	GList *activities, *l;
	JNIEnv *env = get_jni_env();
	// local backup of the backlog
	activities = activity_backlog;
	// deactivate all activities
	activity_backlog = NULL;
	activity_update_current(env);
	// destroy all activities
	for (l = activities; l != NULL; l = l->next) {
		activity_close(env, l->data);
		_UNREF(l->data);
	}
	g_list_free(activities);
}

void activity_start(JNIEnv *env, jobject activity_object)
{
	if (activity_current)
		activity_unfocus(env, activity_current);
	activity_current = NULL;
	/* -- run the activity's onCreate -- */
	(*env)->CallVoidMethod(env, activity_object, handle_cache.activity.onCreate, NULL);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);

	if (_GET_BOOL_FIELD(activity_object, "finishing")) { // finish() was called before the activity was created
		return;
	}

	(*env)->CallVoidMethod(env, activity_object, handle_cache.activity.onPostCreate, NULL);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);

	activity_backlog = g_list_prepend(activity_backlog, _REF(activity_object));

	activity_update_current(env);
}

JNIEXPORT void JNICALL Java_android_app_Activity_nativeFinish(JNIEnv *env, jobject this, jlong window)
{
	GList *l;
	jobject removed_activity = NULL;
	for (l = activity_backlog; l != NULL; l = l->next) {
		if ((*env)->IsSameObject(env, this, l->data)) {
			removed_activity = l->data;
			activity_backlog = g_list_delete_link(activity_backlog, l);
			break;
		}
	}
	activity_update_current(env);
	if (removed_activity) {
		activity_close(env, removed_activity);
		_UNREF(removed_activity);
	}
	if (activity_backlog == NULL && window)
		gtk_window_close(GTK_WINDOW(_PTR(window)));
}

JNIEXPORT void JNICALL Java_android_app_Activity_nativeStartActivity(JNIEnv *env, jclass class, jobject activity)
{
	activity_start(env, activity);
}

JNIEXPORT jboolean JNICALL Java_android_app_Activity_nativeResumeActivity(JNIEnv *env, jclass class, jclass activity_class, jobject intent)
{
	GList *l;
	GList *activities_to_close = NULL;
	jboolean found = JNI_FALSE;
	for (l = activity_backlog; l != NULL; l = l->next) {
		if ((*env)->IsSameObject(env, activity_class, _CLASS(l->data))) {
			if (l != activity_backlog) {
				activities_to_close = activity_backlog;
				activity_backlog = l;
				l->prev->next = NULL;
				l->prev = NULL;
			}

			/* -- run the activity's onNewIntent -- */
			(*env)->CallVoidMethod(env, l->data, handle_cache.activity.onNewIntent, intent);
			if ((*env)->ExceptionCheck(env))
				(*env)->ExceptionDescribe(env);
			found = JNI_TRUE;
			break;
		}
	}
	activity_update_current(env);

	for (l = activities_to_close; l != NULL; l = l->next) {
		activity_close(env, l->data);
		_UNREF(l->data);
	}
	g_list_free(activities_to_close);

	return found;
}

JNIEXPORT void JNICALL Java_android_app_Activity_nativeOpenURI(JNIEnv *env, jclass class, jstring uriString)
{
	static XdpPortal *portal = NULL;
	if (!portal) {
		portal = xdp_portal_new();
	}

	const char *uri = (*env)->GetStringUTFChars(env, uriString, NULL);
	xdp_portal_open_uri(portal, NULL, uri, XDP_OPEN_URI_FLAG_NONE, NULL, NULL, NULL);
	(*env)->ReleaseStringUTFChars(env, uriString, uri);
}

extern GtkWindow *window; // TODO: get this in a better way

struct filechooser_callback_data {
	jobject activity;
	jint request_code;
	jint action;
};

#define RESULT_OK       -1
#define RESULT_CANCELED 0

static void file_dialog_callback(GObject *source_object, GAsyncResult *res, gpointer data)
{
	struct filechooser_callback_data *d = data;
	GtkFileDialog *dialog = GTK_FILE_DIALOG(source_object);
	GFile *(*const finish_functions[])(GtkFileDialog *, GAsyncResult *, GError **) = {
		gtk_file_dialog_open_finish,
		gtk_file_dialog_save_finish,
		gtk_file_dialog_select_folder_finish,
	};

	GFile *file = finish_functions[d->action](dialog, res, NULL);
	JNIEnv *env = get_jni_env();
	jmethodID fileChooserResultCallback = _METHOD(handle_cache.activity.class, "fileChooserResultCallback", "(IIILjava/lang/String;)V");

	if (file) {
		char *uri = g_file_get_uri(file);

		(*env)->CallVoidMethod(env, d->activity, fileChooserResultCallback, d->request_code, RESULT_OK, d->action, _JSTRING(uri));
		if ((*env)->ExceptionCheck(env))
			(*env)->ExceptionDescribe(env);

		g_free(uri);
		g_object_unref(file);
	} else {
		(*env)->CallVoidMethod(env, d->activity, fileChooserResultCallback, d->request_code, RESULT_CANCELED, d->action, NULL);
	}
	free(d);
}

JNIEXPORT void JNICALL Java_android_app_Activity_nativeFileChooser(JNIEnv *env, jobject this, jint action, jstring type_jstring, jstring filename_jstring, jint request_code)
{
	const char *chooser_title = ((char *[]){"Open File", "Save File", "Select Folder"})[action];
	GtkFileDialog *dialog = gtk_file_dialog_new();
	gtk_file_dialog_set_title(GTK_FILE_DIALOG(dialog), chooser_title);

	const char *type = type_jstring ? (*env)->GetStringUTFChars(env, type_jstring, NULL) : NULL;
	if (type && !strchr(type, '*')) {
		GtkFileFilter *filter = gtk_file_filter_new();
		gtk_file_filter_add_mime_type(filter, type);
		gtk_file_filter_set_name(filter, type);
		gtk_file_dialog_set_default_filter(GTK_FILE_DIALOG(dialog), filter);
		(*env)->ReleaseStringUTFChars(env, type_jstring, type);
	}
	const char *filename = filename_jstring ? (*env)->GetStringUTFChars(env, filename_jstring, NULL) : NULL;
	if (filename) {
		gtk_file_dialog_set_initial_name(GTK_FILE_DIALOG(dialog), filename);
		(*env)->ReleaseStringUTFChars(env, filename_jstring, filename);
	}

	struct filechooser_callback_data *callback_data = malloc(sizeof(struct filechooser_callback_data));
	callback_data->activity = _REF(this);
	callback_data->request_code = request_code;
	callback_data->action = action;
	void (*const file_dialog_functions[])(GtkFileDialog *, GtkWindow *, GCancellable *, GAsyncReadyCallback, gpointer) = {
		gtk_file_dialog_open,
		gtk_file_dialog_save,
		gtk_file_dialog_select_folder,
	};
	file_dialog_functions[action](dialog, window, NULL, file_dialog_callback, callback_data);
}

JNIEXPORT jboolean JNICALL Java_android_app_Activity_isInMultiWindowMode(JNIEnv *env, jobject this)
{
	return !gtk_window_is_maximized(window);
}

JNIEXPORT jboolean JNICALL Java_android_app_Activity_isTaskRoot(JNIEnv *env, jobject this)
{
	jobject root_activity = activity_backlog ? g_list_last(activity_backlog)->data : NULL;
	// NULL means that we are currently creating the root activity, so no other activity can exist yet
	return root_activity == NULL || (*env)->IsSameObject(env, this, root_activity);
}

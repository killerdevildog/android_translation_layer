#include <gio/gdesktopappinfo.h>
#include <gtk/gtk.h>

#include "../defines.h"
#include "../util.h"

#include "mpris-dbus.h"

#include "../generated_headers/android_app_NotificationManager.h"

#define MPRIS_BUS_NAME_PREFIX "org.mpris.MediaPlayer2."
#define MPRIS_OBJECT_NAME     "/org/mpris/MediaPlayer2"

/* ongoing notifications to be removed when the app is closed */
static GHashTable *ongoing_notifications = NULL;

/* We queue up notification updates in pending_notifications to make sure that there is at least 200ms
   delay between consecutive updates. This prevents dynamic notification updated from arriving in wrong
   order at the desktop environment.
   Normally 20ms should be enough to prevent notification update order issues, but we use a 10x larger value
   to be safe and 200ms should be more than enough as notification update interval. */
static GHashTable *pending_notifications = NULL;
static GMutex pending_notifications_mutex = {0};
static GSource *send_notifcation_timer = NULL;

static gboolean send_notifcation_func(GSource *send_notifcation_timer, GSourceFunc callback, gpointer user_data)
{
	GApplication *app = g_application_get_default();
	GHashTableIter iter;
	gpointer key, value;
	gboolean notification_sent = FALSE;

	g_mutex_lock(&pending_notifications_mutex);
	g_hash_table_iter_init(&iter, pending_notifications);
	while (g_hash_table_iter_next(&iter, &key, &value)) {
		char *id_string = g_strdup_printf("%d", GPOINTER_TO_INT(key));
		if (value)
			g_application_send_notification(app, id_string, value);
		else
			g_application_withdraw_notification(app, id_string);
		g_free(id_string);
		g_hash_table_iter_remove(&iter);
		notification_sent = TRUE;
	}
	g_mutex_unlock(&pending_notifications_mutex);

	if (notification_sent)
		g_source_set_ready_time(send_notifcation_timer, g_source_get_time(send_notifcation_timer) + 200000L); // 200ms
	else
		g_source_set_ready_time(send_notifcation_timer, -1);

	return G_SOURCE_CONTINUE;
}
static GSourceFuncs send_notifcation_funcs = {
	.dispatch = send_notifcation_func,
};
static void unref_nullsafe(void *data)
{
	if (data)
		g_object_unref(data);
}

static void queue_notification(int id, GNotification *notification)
{
	g_mutex_lock(&pending_notifications_mutex);
	if (!pending_notifications) {
		pending_notifications = g_hash_table_new_full(NULL, NULL, NULL, unref_nullsafe);
		send_notifcation_timer = g_source_new(&send_notifcation_funcs, sizeof(GSource));
		g_source_attach(send_notifcation_timer, NULL);
		GApplication *app = g_application_get_default();
		gchar *desktop_id = g_strdup_printf("%s.desktop", g_application_get_application_id(app));
		GDesktopAppInfo *info = g_desktop_app_info_new(desktop_id);
		/* Some desktop environments don't allow XDG-portal notifications without a desktop file.
		   There is no public API to force a specific backend, so we have to set the environment variable.
		   The GNOTIFICATION_BACKEND variable will be read by GIO the first time the notification backend is used.
		   This method should be future proof unless the freedesktop backend is removed. */
		if (!info)
			setenv("GNOTIFICATION_BACKEND", "freedesktop", 0);
		else
			g_object_unref(info);
		g_free(desktop_id);
		ongoing_notifications = g_hash_table_new(NULL, NULL);
	}
	g_hash_table_insert(pending_notifications, GINT_TO_POINTER(id), notification);
	g_mutex_unlock(&pending_notifications_mutex);
	if (g_source_get_ready_time(send_notifcation_timer) == -1)
		g_source_set_ready_time(send_notifcation_timer, 0); // immediately
}

JNIEXPORT jlong JNICALL Java_android_app_NotificationManager_nativeInitBuilder(JNIEnv *env, jobject this)
{
	return _INTPTR(g_notification_new(""));
}

JNIEXPORT void JNICALL Java_android_app_NotificationManager_nativeAddAction(JNIEnv *env, jobject this, jlong builder_ptr, jstring name_jstr, jint type, jobject intent)
{
	GNotification *notification = _PTR(builder_ptr);
	const char *name = "";
	if (name_jstr)
		name = (*env)->GetStringUTFChars(env, name_jstr, NULL);
	const char *action = intent_actionname_from_type(type);
	if (action)
		g_notification_add_button_with_target_value(notification, name, action, intent_serialize(env, intent));
	if (name_jstr)
		(*env)->ReleaseStringUTFChars(env, name_jstr, name);
}

JNIEXPORT void JNICALL Java_android_app_NotificationManager_nativeShowNotification(JNIEnv *env, jobject this, jlong builder_ptr, jint id, jstring title_jstr, jstring text_jstr, jstring icon_jstr, jboolean ongoing, jint type, jobject intent)
{
	GNotification *notification = _PTR(builder_ptr);

	if (title_jstr) {
		const char *title = (*env)->GetStringUTFChars(env, title_jstr, NULL);
		g_notification_set_title(notification, title);
		(*env)->ReleaseStringUTFChars(env, title_jstr, title);
	}
	if (text_jstr) {
		const char *text = (*env)->GetStringUTFChars(env, text_jstr, NULL);
		g_notification_set_body(notification, text);
		(*env)->ReleaseStringUTFChars(env, text_jstr, text);
	}
	if (icon_jstr) {
		const char *icon_path = (*env)->GetStringUTFChars(env, icon_jstr, NULL);
		extract_from_apk(icon_path, icon_path);
		char *icon_path_full = g_strdup_printf("%s/%s", get_app_data_dir(), icon_path);
		GMappedFile *icon_file = g_mapped_file_new(icon_path_full, FALSE, NULL);
		GBytes *icon_bytes = g_mapped_file_get_bytes(icon_file);
		GIcon *icon = g_bytes_icon_new(icon_bytes);
		g_notification_set_icon(notification, icon);
		g_object_unref(icon);
		g_bytes_unref(icon_bytes);
		g_mapped_file_unref(icon_file);
		g_free(icon_path_full);
		(*env)->ReleaseStringUTFChars(env, icon_jstr, icon_path);
	}
	const char *action = intent_actionname_from_type(type);
	if (action)
		g_notification_set_default_action_and_target_value(notification, action, intent_serialize(env, intent));
	queue_notification(id, notification);
	if (ongoing)
		g_hash_table_add(ongoing_notifications, GINT_TO_POINTER(id));
}

JNIEXPORT void JNICALL Java_android_app_NotificationManager_nativeCancel(JNIEnv *env, jobject this, jint id)
{
	queue_notification(id, NULL);
}

static void remove_ongoing_notification(gpointer key, gpointer value, gpointer user_data)
{
	char *id_string = g_strdup_printf("%d", GPOINTER_TO_INT(key));
	g_application_withdraw_notification(g_application_get_default(), id_string);
	g_free(id_string);
}

void remove_ongoing_notifications()
{
	if (ongoing_notifications)
		g_hash_table_foreach(ongoing_notifications, remove_ongoing_notification, NULL);
}

static MediaPlayer2 *mpris = NULL;
static int dbus_name_id = 0;
extern MediaPlayer2Player *mpris_player;
extern GtkWindow *window;

static gboolean on_media_player_handle_raise(MediaPlayer2 *mpris, GDBusMethodInvocation *invocation, gpointer user_data)
{
	gtk_window_present(window);
	media_player2_complete_raise(mpris, invocation);
	return TRUE;
}

static void on_bus_acquired(GDBusConnection *connection, const char *name, gpointer user_data)
{
	g_dbus_interface_skeleton_export(G_DBUS_INTERFACE_SKELETON(mpris),
	                                 connection, MPRIS_OBJECT_NAME, NULL);

	g_dbus_interface_skeleton_export(G_DBUS_INTERFACE_SKELETON(mpris_player),
	                                 connection, MPRIS_OBJECT_NAME, NULL);
}

JNIEXPORT void JNICALL Java_android_app_NotificationManager_nativeShowMPRIS(JNIEnv *env, jobject this, jstring package_name_jstr, jstring identity_jstr)
{
	if (!mpris) {
		mpris = media_player2_skeleton_new();
		g_signal_connect(mpris, "handle-raise", G_CALLBACK(on_media_player_handle_raise), NULL);
	}
	const char *package_name = NULL;
	const char *app_id = g_application_get_application_id(G_APPLICATION(gtk_window_get_application(window)));
	if ((app_id == NULL || strcmp(app_id, "com.example.demo_application") == 0) && package_name_jstr) {
		// fall back to package name
		app_id = package_name = (*env)->GetStringUTFChars(env, package_name_jstr, NULL);
	}
	if (!dbus_name_id) {
		gchar *bus_name = g_strdup_printf("%s%s", MPRIS_BUS_NAME_PREFIX, app_id);
		dbus_name_id = g_bus_own_name(G_BUS_TYPE_SESSION, bus_name, G_BUS_NAME_OWNER_FLAGS_NONE,
		                              on_bus_acquired, NULL, NULL, mpris, NULL);
		g_free(bus_name);
	}
	media_player2_set_can_raise(mpris, TRUE);
	media_player2_set_desktop_entry(mpris, app_id);
	if (package_name) {
		(*env)->ReleaseStringUTFChars(env, package_name_jstr, package_name);
	}
	if (identity_jstr) {
		const char *identity = (*env)->GetStringUTFChars(env, identity_jstr, NULL);
		media_player2_set_identity(mpris, identity);
		(*env)->ReleaseStringUTFChars(env, identity_jstr, identity);
	}
}

JNIEXPORT void JNICALL Java_android_app_NotificationManager_nativeCancelMPRIS(JNIEnv *env, jobject this)
{
	if (dbus_name_id) {
		g_dbus_interface_skeleton_unexport(G_DBUS_INTERFACE_SKELETON(mpris));
		g_dbus_interface_skeleton_unexport(G_DBUS_INTERFACE_SKELETON(mpris_player));
		g_clear_handle_id(&dbus_name_id, g_bus_unown_name);
	}
}

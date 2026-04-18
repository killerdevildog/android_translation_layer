#define _GNU_SOURCE

#include <gtk/gtk.h>
#include <libportal/portal.h>
#include <string.h>
#ifdef XDP_TYPE_INPUT_CAPTURE_SESSION // libportal >= 0.8
	#include <libportal/settings.h>
#endif

#include "portal-email.h"
#include "portal-openuri.h"
#include "unifiedpush-connector.h"
#include "unifiedpush-distributor.h"

#include "../defines.h"
#include "../util.h"

#include "../generated_headers/android_content_Context.h"

extern char *apk_path;

JNIEXPORT jstring JNICALL Java_android_content_Context_native_1get_1apk_1path(JNIEnv *env, jclass this)
{
	return _JSTRING(apk_path);
}

#ifdef XDP_TYPE_INPUT_CAPTURE_SESSION // libportal >= 0.8
static void settings_changed_cb(XdpSettings *xdp_settings, gchar *namestpace, gchar *key, GVariant *value, jobject configuration)
{
	JNIEnv *env;
	if (!strcmp(namestpace, "org.freedesktop.appearance") && !strcmp(key, "color-scheme")) {
		int color_sheme = g_variant_get_uint32(value);
		g_object_set(gtk_settings_get_default(), "gtk-application-prefer-dark-theme", color_sheme == 1, NULL);
		env = get_jni_env();
		if (!configuration) {
			jobject resources = _GET_STATIC_OBJ_FIELD(handle_cache.context.class, "r", "Landroid/content/res/Resources;");
			configuration = _GET_OBJ_FIELD(resources, "mConfiguration", "Landroid/content/res/Configuration;");
		}
		if (color_sheme == 1) // Prefer dark appearance
			_SET_INT_FIELD(configuration, "uiMode", /*UI_MODE_NIGHT_YES*/ 0x20);
		else if (color_sheme == 2) // Prefer light appearance
			_SET_INT_FIELD(configuration, "uiMode", /*UI_MODE_NIGHT_NO*/ 0x10);
		else // No preference
			_SET_INT_FIELD(configuration, "uiMode", /*UI_MODE_NIGHT_UNDEFINED*/ 0x00);
	}
}

static XdpSettings *xdp_settings = NULL;
#endif

JNIEXPORT void JNICALL Java_android_content_Context_native_1updateConfig(JNIEnv *env, jclass this, jobject config)
{
	GdkDisplay *display = gdk_display_get_default();
	GdkMonitor *monitor = g_list_model_get_item(gdk_display_get_monitors(display), 0);
	GdkRectangle geometry;
	gdk_monitor_get_geometry(monitor, &geometry);

	_SET_INT_FIELD(config, "screenWidthDp", geometry.width);
	_SET_INT_FIELD(config, "screenHeightDp", geometry.height);
#ifdef XDP_TYPE_INPUT_CAPTURE_SESSION // libportal >= 0.8
	if (!xdp_settings) {
		GError *error = NULL;
		XdpPortal *portal = xdp_portal_initable_new(&error);
		if (!portal) {
			printf("xdp_portal_initable_new failed: %s\n", error->message);
			g_error_free(error);
			return;
		}
		xdp_settings = xdp_portal_get_settings(portal);
		g_object_unref(portal);
		g_signal_connect(xdp_settings, "changed", G_CALLBACK(settings_changed_cb), NULL);
	}
	GVariant *color_sheme = xdp_settings_read_value(xdp_settings, "org.freedesktop.appearance", "color-scheme", NULL, NULL);
	if (color_sheme) {
		settings_changed_cb(xdp_settings, "org.freedesktop.appearance", "color-scheme", color_sheme, config);
		g_variant_unref(color_sheme);
	}
#endif
}

JNIEXPORT void JNICALL Java_android_content_Context_nativeOpenFile(JNIEnv *env, jclass class, jint fd)
{
	GDBusConnection *connection = g_bus_get_sync(G_BUS_TYPE_SESSION, NULL, NULL);
	OpenURI *openuri = open_uri_proxy_new_sync(connection, 0, "org.freedesktop.portal.Desktop", "/org/freedesktop/portal/desktop", NULL, NULL);
	GVariantBuilder opt_builder;
	g_variant_builder_init(&opt_builder, G_VARIANT_TYPE_VARDICT);
	GUnixFDList *fd_list = g_unix_fd_list_new_from_array(&fd, 1);
	open_uri_call_open_file_sync(openuri, "", g_variant_new("h", 0), g_variant_builder_end(&opt_builder), fd_list, NULL, NULL, NULL, NULL);
	g_object_unref(fd_list);
	g_object_unref(openuri);
	g_object_unref(connection);
}

char *fd_get_path(int fd)
{
	char *fdlink = g_strdup_printf("/proc/self/fd/%d", fd);
	char *buf = g_malloc(PATH_MAX);

	ssize_t len = readlink(fdlink, buf, PATH_MAX - 1);
	g_free(fdlink);
	if (len < 0)
		return NULL;

	buf[len] = '\0';
	return buf;
}

extern GtkWindow *window;

static void share_dialog_callback(GObject *dialog, GAsyncResult *result, gpointer text_jstr)
{
	JNIEnv *env = get_jni_env();
	int button_id = gtk_alert_dialog_choose_finish(GTK_ALERT_DIALOG(dialog), result, NULL);
	const char *text = NULL;
	if (text_jstr)
		text = (*env)->GetStringUTFChars(env, text_jstr, NULL);
	int fd = GPOINTER_TO_INT(g_object_get_data(G_OBJECT(dialog), "fd"));
	printf("share_dialog_callback: button_id=%d, text=%s, fd=%d\n", button_id, text, fd);
	if (button_id == 1) {
		printf("Copy\n");
		GdkClipboard *clipboard = gdk_display_get_clipboard(gtk_root_get_display(GTK_ROOT(window)));
		if (fd) {
			char *path = fd_get_path(fd);
			GFile *file = g_file_new_for_path(path);
			gdk_clipboard_set(clipboard, G_TYPE_FILE, file);
			g_object_unref(file);
			g_free(path);
		}
		if (text) {
			gdk_clipboard_set_text(clipboard, text);
		}
	} else if (button_id == 2) {
		printf("Email\n");
		GDBusConnection *connection = g_bus_get_sync(G_BUS_TYPE_SESSION, NULL, NULL);
		Email *email = email_proxy_new_sync(connection, 0, "org.freedesktop.portal.Desktop", "/org/freedesktop/portal/desktop", NULL, NULL);
		GUnixFDList *fd_list = g_unix_fd_list_new();
		GVariantBuilder opt_builder;
		g_variant_builder_init(&opt_builder, G_VARIANT_TYPE_VARDICT);
		if (text_jstr) {
			const char *text = (*env)->GetStringUTFChars(env, text_jstr, NULL);
			g_variant_builder_add(&opt_builder, "{sv}", "body", g_variant_new_string(text));
			(*env)->ReleaseStringUTFChars(env, text_jstr, text);
		}
		if (fd) {
			int fd_handle = g_unix_fd_list_append(fd_list, fd, NULL);
			GVariantBuilder fd_array_builder;
			g_variant_builder_init(&fd_array_builder, G_VARIANT_TYPE("ah"));
			g_variant_builder_add(&fd_array_builder, "h", fd_handle);
			g_variant_builder_add(&opt_builder, "{sv}", "attachment_fds", g_variant_builder_end(&fd_array_builder));
			/* The `attachment_fds` option is ignored by most email applications, so we also set the file path as subject */
			char *path = fd_get_path(fd);
			g_variant_builder_add(&opt_builder, "{sv}", "subject", g_variant_new_string(path));
			g_free(path);
		}
		email_call_compose_email_sync(email, "", g_variant_builder_end(&opt_builder), fd_list, NULL, NULL, NULL, NULL);
		g_object_unref(fd_list);
		g_object_unref(email);
		g_object_unref(connection);
	}
	if (text_jstr) {
		(*env)->ReleaseStringUTFChars(env, text_jstr, text);
		_UNREF(text_jstr);
	}
	if (fd)
		close(fd);
}

/* The XDG specification does not provide anything comparable to the Android share API yet. Therefore, we provide
 * a custom dialog allowing the user to copy to clipboard or send per mail using the composeEmail portal.
 */
JNIEXPORT void JNICALL Java_android_content_Context_nativeShareFile(JNIEnv *env, jclass class, jstring text_jstr, jint fd)
{
	GtkAlertDialog *dialog = gtk_alert_dialog_new("Share");
	if (fd != -1) {
		char *path = fd_get_path(fd);
		gtk_alert_dialog_set_detail(dialog, path);
		g_free(path);
		g_object_set_data(G_OBJECT(dialog), "fd", GINT_TO_POINTER(dup(fd)));
	} else if (text_jstr) {
		const char *text = (*env)->GetStringUTFChars(env, text_jstr, NULL);
		gtk_alert_dialog_set_detail(dialog, text);
		(*env)->ReleaseStringUTFChars(env, text_jstr, text);
	}
	gtk_alert_dialog_set_buttons(dialog, (const char *[]){"Cancel", "Copy", "Email", NULL});
	gtk_alert_dialog_set_cancel_button(dialog, 0);
	gtk_alert_dialog_set_default_button(dialog, 1);
	gtk_alert_dialog_choose(dialog, window, NULL, share_dialog_callback, _REF(text_jstr));
}

static void on_bus_acquired(GDBusConnection *connection, const char *name, gpointer user_data)
{
	Connector1 *connector1 = user_data;
	g_dbus_interface_skeleton_export(G_DBUS_INTERFACE_SKELETON(connector1),
	                                 connection, "/org/unifiedpush/Connector", NULL);
}

static gboolean on_new_endpoint(Connector1 *connector, GDBusMethodInvocation *invocation, gpointer user_data)
{
	GVariant *parameters = g_dbus_method_invocation_get_parameters(invocation);
	const char *token;
	g_variant_get_child(parameters, 0, "s", &token);
	const char *endpoint;
	g_variant_get_child(parameters, 1, "s", &endpoint);
	connector1_complete_new_endpoint(connector, invocation);

	JNIEnv *env = get_jni_env();
	jobject intent = (*env)->NewObject(env, handle_cache.intent.class, handle_cache.intent.constructor);
	_SET_OBJ_FIELD(intent, "action", "Ljava/lang/String;", _JSTRING("org.unifiedpush.android.connector.NEW_ENDPOINT"));
	(*env)->CallObjectMethod(env, intent, handle_cache.intent.putExtraCharSequence, _JSTRING("token"), _JSTRING(token));
	(*env)->CallObjectMethod(env, intent, handle_cache.intent.putExtraCharSequence, _JSTRING("endpoint"), _JSTRING(endpoint));

	jobject context = _GET_STATIC_OBJ_FIELD(handle_cache.context.class, "this_application", "Landroid/app/Application;");
	(*env)->CallVoidMethod(env, context, handle_cache.context.sendBroadcast, intent);
	if ((*env)->ExceptionCheck(env)) {
		(*env)->ExceptionDescribe(env);
	}
	return TRUE;
}

static gboolean on_message(Connector1 *connector, GDBusMethodInvocation *invocation, gpointer user_data)
{
	GVariant *parameters = g_dbus_method_invocation_get_parameters(invocation);
	const char *token;
	g_variant_get_child(parameters, 0, "s", &token);
	gsize size;
	const int8_t *message = g_variant_get_fixed_array(g_variant_get_child_value(parameters, 1), &size, 1);
	connector1_complete_message(connector, invocation);

	JNIEnv *env = get_jni_env();
	jobject intent = (*env)->NewObject(env, handle_cache.intent.class, handle_cache.intent.constructor);
	_SET_OBJ_FIELD(intent, "action", "Ljava/lang/String;", _JSTRING("org.unifiedpush.android.connector.MESSAGE"));
	(*env)->CallObjectMethod(env, intent, handle_cache.intent.putExtraCharSequence, _JSTRING("token"), _JSTRING(token));
	jbyteArray bytesMessage = (*env)->NewByteArray(env, size);
	(*env)->SetByteArrayRegion(env, bytesMessage, 0, size, message);
	(*env)->CallObjectMethod(env, intent, handle_cache.intent.putExtraByteArray, _JSTRING("bytesMessage"), bytesMessage);

	jobject context = _GET_STATIC_OBJ_FIELD(handle_cache.context.class, "this_application", "Landroid/app/Application;");
	(*env)->CallVoidMethod(env, context, handle_cache.context.sendBroadcast, intent);
	if ((*env)->ExceptionCheck(env)) {
		(*env)->ExceptionDescribe(env);
	}
	return TRUE;
}

JNIEXPORT void JNICALL Java_android_content_Context_nativeExportUnifiedPush(JNIEnv *env, jclass this, jstring application_jstr)
{
	const char *application = (*env)->GetStringUTFChars(env, application_jstr, NULL);

	Connector1 *connector1 = connector1_skeleton_new();
	g_signal_connect(connector1, "handle-new-endpoint", G_CALLBACK(on_new_endpoint), NULL);
	g_signal_connect(connector1, "handle-message", G_CALLBACK(on_message), NULL);
	g_bus_own_name(G_BUS_TYPE_SESSION, application, G_BUS_NAME_OWNER_FLAGS_NONE,
	               on_bus_acquired, NULL, NULL, connector1, NULL);
	(*env)->ReleaseStringUTFChars(env, application_jstr, application);
}

JNIEXPORT void JNICALL Java_android_content_Context_nativeRegisterUnifiedPush(JNIEnv *env, jclass this, jstring token_jstr, jstring application_jstr)
{
	const char *token = (*env)->GetStringUTFChars(env, token_jstr, NULL);
	const char *application = (*env)->GetStringUTFChars(env, application_jstr, NULL);

	GDBusConnection *connection = g_bus_get_sync(G_BUS_TYPE_SESSION, NULL, NULL);
	Distributor1 *distributor1 = distributor1_proxy_new_sync(connection, 0, "org.unifiedpush.Distributor.kde", "/org/unifiedpush/Distributor", NULL, NULL);
	GError *error = NULL;
	distributor1_call_register(distributor1, application, token, "", NULL, NULL, &error);
	if (error) {
		printf("nativeRegisterUnifiedPush: error=%s\n", error->message);
		g_error_free(error);
	}
	g_object_unref(distributor1);
	g_object_unref(connection);

	(*env)->ReleaseStringUTFChars(env, token_jstr, token);
	(*env)->ReleaseStringUTFChars(env, application_jstr, application);
}

JNIEXPORT void JNICALL Java_android_content_Context_nativeStartExternalService(JNIEnv *env, jclass this, jobject intent)
{
	GVariant *variant = intent_serialize(env, intent);
	jstring package_jstr = _GET_OBJ_FIELD(intent, "packageName", "Ljava/lang/String;");
	const char *package = (*env)->GetStringUTFChars(env, package_jstr, NULL);
	char *object_path = g_strdup_printf("/%s", package);
	g_strdelimit(object_path, ".", '/');
	GDBusConnection *connection = g_bus_get_sync(G_BUS_TYPE_SESSION, NULL, NULL);
	GActionGroup *action_group = G_ACTION_GROUP(g_dbus_action_group_get(connection, package, object_path));
	g_action_group_activate_action(action_group, "startService", variant);
	g_object_unref(action_group);
	g_object_unref(connection);
	g_free(object_path);
	(*env)->ReleaseStringUTFChars(env, package_jstr, package);
}

#include <gio/gio.h>
#include <glib.h>
#include <gtk/gtk.h>
#include <gtk4-layer-shell/gtk4-layer-shell.h>
#include <jni.h>

#include <stdio.h>

#include "../defines.h"
#include "../util.h"
#include "../generated_headers/android_atl_ATLKeyboardDialog.h"

#define DEBUG(fmt, ...) android_log_printf(ANDROID_LOG_INFO, "ATLKeyboardDialog", "%s:%d: " fmt, __func__, __LINE__, ##__VA_ARGS__)

static GDBusNodeInfo *introspection_data = NULL;
static GDBusConnection *dbus_connection = NULL;
static gboolean visible = TRUE;
static GtkWidget *osk_window = NULL;

static void emit_property_changed(GDBusConnection *connection)
{
	GVariantBuilder builder;

	g_variant_builder_init(&builder, G_VARIANT_TYPE_ARRAY);
	g_variant_builder_add(&builder, "{sv}", "Visible", g_variant_new_boolean(visible));

	g_dbus_connection_emit_signal(connection,
	                              NULL,
	                              "/sm/puri/OSK0",
	                              "org.freedesktop.DBus.Properties",
	                              "PropertiesChanged",
	                              g_variant_new("(sa{sv}as)", "sm.puri.OSK0", &builder, NULL), NULL);

	g_variant_builder_clear(&builder);
}

/* Used in IMS. */
void atlosk_set_visible(gboolean new_visible)
{
	visible = new_visible;

	if (osk_window) {
		gtk_widget_set_visible(osk_window, visible);
		if (dbus_connection)
			emit_property_changed(dbus_connection);
	}
}

static void handle_method_call(GDBusConnection *connection,
                               const gchar *sender,
                               const gchar *object_path,
                               const gchar *interface_name,
                               const gchar *method_name,
                               GVariant *parameters,
                               GDBusMethodInvocation *invocation,
                               gpointer user_data)
{
	if (g_strcmp0(method_name, "SetVisible") == 0) {
		gboolean new_visible;

		g_variant_get(parameters, "(b)", &new_visible);
		atlosk_set_visible(new_visible);
		g_dbus_method_invocation_return_value(invocation, NULL);
	}
}

static GVariant *handle_get_property(GDBusConnection *connection,
                                     const gchar *sender,
                                     const gchar *object_path,
                                     const gchar *interface_name,
                                     const gchar *property_name,
                                     GError **error,
                                     gpointer user_data)
{
	if (g_strcmp0(property_name, "Visible") == 0) {
		return g_variant_new_boolean(visible);
	}

	return NULL;
}

static const GDBusInterfaceVTable interface_vtable = {
	handle_method_call,
	handle_get_property,
	NULL,
};

static void on_name_acquired(GDBusConnection *connection, const gchar *name, gpointer user_data)
{
	DEBUG("Acquired D-Bus name: %s\n", name);
	dbus_connection = connection;
}

static int connect_osk_dbus_iface(GtkWidget *dialog)
{
	GDBusConnection *connection;
	GError *error = NULL;
	guint registration_id;
	guint owner_id;

	owner_id = g_bus_own_name(G_BUS_TYPE_SESSION,
	                          "sm.puri.OSK0",
	                          G_BUS_NAME_OWNER_FLAGS_REPLACE,
	                          NULL,
	                          on_name_acquired,
	                          NULL, NULL, NULL);

	if (owner_id == 0) {
		g_printerr("OSK: Error: Could not acquire D-Bus name\n");
		return 1;
	}

	/* https://world.pages.gitlab.gnome.org/Phosh/phosh/phosh-dbus-sm.puri.OSK0.html */
	const gchar introspection_xml[] =
	    "<node>"
	    "  <interface name='sm.puri.OSK0'>"
	    "    <method name='SetVisible'>"
	    "      <arg type='b' name='visible' direction='in'/>"
	    "    </method>"
	    "    <property name='Visible' type='b' access='read'/>"
	    "  </interface>"
	    "</node>";

	introspection_data = g_dbus_node_info_new_for_xml(introspection_xml, &error);
	if (!introspection_data) {
		g_printerr("OSK: Failed to parse introspection XML: %s\n", error->message);
		g_error_free(error);
		return 1;
	}

	connection = g_bus_get_sync(G_BUS_TYPE_SESSION, NULL, &error);
	if (!connection) {
		g_printerr("OSK: Failed to connect to D-Bus: %s\n", error->message);
		g_error_free(error);
		return 1;
	}

	registration_id = g_dbus_connection_register_object(connection,
	                                                    "/sm/puri/OSK0",
	                                                    introspection_data->interfaces[0],
	                                                    &interface_vtable,
	                                                    NULL, NULL, &error);
	if (!registration_id) {
		g_printerr("OSK: Failed to register object: %s\n", error->message);
		g_error_free(error);
		return 1;
	}

	osk_window = dialog;

	return 0;
}

static gboolean on_close_request(GtkWidget *dialog, jobject jobj)
{
	JNIEnv *env = get_jni_env();
	jmethodID dismiss = _METHOD(_CLASS(jobj), "dismiss", "()V");
	(*env)->CallVoidMethod(env, jobj, dismiss);
	return FALSE;
}

JNIEXPORT jlong JNICALL Java_android_atl_ATLKeyboardDialog_nativeInit(JNIEnv *env, jobject this)
{
	GtkWidget *dialog = gtk_window_new();
	GtkWindow *window = GTK_WINDOW(dialog);

	gtk_layer_init_for_window(window);
	gtk_layer_auto_exclusive_zone_enable(window);
	gtk_layer_set_namespace(window, "osk");
	gtk_layer_set_exclusive_zone(window, 200);

	static const gboolean anchors[] = {TRUE, TRUE, FALSE, TRUE};
	for (int i = 0; i < GTK_LAYER_SHELL_EDGE_ENTRY_NUMBER; i++)
		gtk_layer_set_anchor(window, i, anchors[i]);

	connect_osk_dbus_iface(dialog);

	gtk_window_set_child(GTK_WINDOW(dialog), gtk_box_new(GTK_ORIENTATION_VERTICAL, 1));
	g_signal_connect_swapped(dialog, "response", G_CALLBACK(gtk_window_destroy), dialog);
	g_signal_connect(GTK_WINDOW(dialog), "close-request", G_CALLBACK(on_close_request), _REF(this));

	return _INTPTR(g_object_ref(dialog));
}

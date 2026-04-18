#include <jni.h>

#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>

#include <gdk/wayland/gdkwayland.h>
#include <gtk/gtk.h>

#include "input-method-unstable-v2-client-protocol.h"
#include "virtual-keyboard-unstable-v1-client-protocol.h"

#include "defines.h"
#include "util.h"
#include "generated_headers/android_inputmethodservice_InputMethodService_ATLInputConnection.h"

#define INFO(x...)      android_log_printf(ANDROID_LOG_INFO, "ATLKeyboardIMS", x)
#define DEBUG(fmt, ...) android_log_printf(ANDROID_LOG_DEBUG, "ATLKeyboardIMS", "%s:%d: " fmt, __func__, __LINE__, ##__VA_ARGS__)

struct {
	struct wl_display *display;
	struct wl_registry *registry;
	struct wl_seat *seat;
	struct zwp_input_method_manager_v2 *input_method_manager;
	struct zwp_input_method_v2 *input_method;
	struct zwp_virtual_keyboard_manager_v1 *virtual_keyboard_manager;
	struct zwp_virtual_keyboard_v1 *virtual_keyboard;

	char compositing[4096];
	char surrounding[4096];
	guint text_len;
	guint cursor;
	guint serial;
} osk = {0};

/* android_atl_ATLKeyboardDialog.c */
#ifdef ATL_HAS_OSK
extern void atlosk_set_visible(gboolean new_visible);
#else
static void atlosk_set_visible(gboolean new_visible)
{
	DEBUG("=%d\n", new_visible);
}
#endif

/*
 * input-method-unstable-v2
 */

static void
handle_activate(void *data,
                struct zwp_input_method_v2 *zwp_input_method_v2)
{
	atlosk_set_visible(TRUE);
}

static void
handle_deactivate(void *data,
                  struct zwp_input_method_v2 *zwp_input_method_v2)
{
	atlosk_set_visible(FALSE);
}

static void
handle_surrounding_text(void *data,
                        struct zwp_input_method_v2 *zwp_input_method_v2,
                        const char *text,
                        uint32_t cursor,
                        uint32_t anchor)
{
	DEBUG("(cursor=%d, '%s')\n", cursor, text);
	osk.cursor = cursor;
	osk.text_len = strnlen(text, sizeof(osk.surrounding));
	strncpy(osk.surrounding, text, sizeof(osk.surrounding));
}

static void
handle_text_change_cause(void *data,
                         struct zwp_input_method_v2 *zwp_input_method_v2,
                         uint32_t cause)
{
	//DEBUG("\n");
}

static void
handle_content_type(void *data,
                    struct zwp_input_method_v2 *zwp_input_method_v2,
                    uint32_t hint,
                    uint32_t purpose)
{
	//DEBUG("\n");
}

static void
handle_done(void *data,
            struct zwp_input_method_v2 *zwp_input_method_v2)
{
	osk.serial++;
}

static void
handle_unavailable(void *data,
                   struct zwp_input_method_v2 *zwp_input_method_v2)
{
	INFO("Input method unavailable");
}

static const struct zwp_input_method_v2_listener input_method_listener = {
	.activate = handle_activate,
	.deactivate = handle_deactivate,
	.surrounding_text = handle_surrounding_text,
	.text_change_cause = handle_text_change_cause,
	.content_type = handle_content_type,
	.done = handle_done,
	.unavailable = handle_unavailable,
};

static void
registry_handle_global(void *data,
                       struct wl_registry *registry,
                       uint32_t name,
                       const char *interface,
                       uint32_t version)
{
	if (!strcmp(interface, zwp_input_method_manager_v2_interface.name)) {
		osk.input_method_manager = wl_registry_bind(registry, name, &zwp_input_method_manager_v2_interface, 1);
		osk.input_method = zwp_input_method_manager_v2_get_input_method(osk.input_method_manager, osk.seat);
		zwp_input_method_v2_add_listener(osk.input_method, &input_method_listener, &osk);
	} else if (!strcmp(interface, zwp_virtual_keyboard_manager_v1_interface.name)) {
		osk.virtual_keyboard_manager = wl_registry_bind(registry, name, &zwp_virtual_keyboard_manager_v1_interface, 1);
		osk.virtual_keyboard = zwp_virtual_keyboard_manager_v1_create_virtual_keyboard(osk.virtual_keyboard_manager, osk.seat);
	}
}

static void
registry_handle_global_remove(void *data,
                              struct wl_registry *registry,
                              uint32_t name)
{
	INFO("Global %d removed but not handled", name);
}

static const struct wl_registry_listener registry_listener = {
	registry_handle_global,
	registry_handle_global_remove,
};

JNIEXPORT jlong JNICALL Java_android_inputmethodservice_InputMethodService_00024ATLInputConnection_nativeInit(JNIEnv *env, jobject this)
{
	GdkDisplay *gdk_display;
	GdkSeat *gdk_seat;
	extern GtkWindow *window; /* Main activity window. */

	INFO("Native init!");

	gdk_display = gtk_root_get_display(GTK_ROOT(window));
	if (gdk_display == NULL) {
		g_critical("ATLKeyboardIMS: Failed to get display: %m\n");
		return 0;
	}

	gdk_seat = gdk_display_get_default_seat(gdk_display);
	if (gdk_seat == NULL) {
		g_critical("ATLKeyboardIMS: Failed to get seat: %m\n");
		return 0;
	}

	osk.display = gdk_wayland_display_get_wl_display(gdk_display);
	osk.seat = gdk_wayland_seat_get_wl_seat(gdk_seat);
	osk.registry = wl_display_get_registry(osk.display);
	wl_registry_add_listener(osk.registry, &registry_listener, &osk);

	gtk_widget_set_visible(GTK_WIDGET(window), false);

	return 1;
}

JNIEXPORT jboolean JNICALL Java_android_inputmethodservice_InputMethodService_00024ATLInputConnection_nativeSetCompositingText(JNIEnv *env, jobject this, jlong ptr, jstring text, jint newCursorPosition)
{
	const char *data = (*env)->GetStringUTFChars(env, text, NULL);

	INFO("nativeSetCompositingText('%s', cur=%d)\n", data, newCursorPosition);

	if (osk.input_method) {
		size_t text_len = strlen(data);
		int cursor;

		if (newCursorPosition > 0)
			cursor = text_len - newCursorPosition + 1;
		else
			cursor = -1 * newCursorPosition;

		zwp_input_method_v2_set_preedit_string(osk.input_method, data, cursor, cursor);
		strncpy(osk.compositing, data, sizeof(osk.compositing));
		zwp_input_method_v2_commit(osk.input_method, osk.serial);
	}

	return true;
}

JNIEXPORT jboolean JNICALL Java_android_inputmethodservice_InputMethodService_00024ATLInputConnection_nativeSetCompositingRegion(JNIEnv *env, jobject this, jlong ptr, jint start, jint end)
{
	INFO("nativeSetCompositingRegion(start=%d, end=%d)\n", start, end);

	if (osk.input_method) {
		int beforeLength, afterLength, cursor = osk.cursor;
		char tmp[4096] = {0};

		if (start > end) {
			int tmp = end;
			end = start;
			start = tmp;
		}

		beforeLength = (end - start);
		afterLength = 0;

		strncpy(tmp, &osk.surrounding[cursor - beforeLength], beforeLength);
		cursor = strlen(tmp);

		zwp_input_method_v2_delete_surrounding_text(osk.input_method, beforeLength, afterLength);
		zwp_input_method_v2_set_preedit_string(osk.input_method, tmp, cursor, cursor);
		zwp_input_method_v2_commit(osk.input_method, osk.serial);
	}

	return true;
}

JNIEXPORT jboolean JNICALL Java_android_inputmethodservice_InputMethodService_00024ATLInputConnection_nativeFinishComposingText(JNIEnv *env, jobject this, jlong ptr)
{
	INFO("nativeFinishCompositingText()\n");

	if (osk.input_method) {
		zwp_input_method_v2_commit_string(osk.input_method, osk.compositing);
		zwp_input_method_v2_commit(osk.input_method, osk.serial);
		osk.compositing[0] = '\0';
	}

	return true;
}

JNIEXPORT jboolean JNICALL Java_android_inputmethodservice_InputMethodService_00024ATLInputConnection_nativeCommitText(JNIEnv *env, jobject this, jlong ptr, jstring text, jint newCursorPosition)
{
	const char *data = (*env)->GetStringUTFChars(env, text, NULL);

	INFO("nativeCommitText('%s', cur=%d)\n", data, newCursorPosition);

	if (osk.input_method) {
		zwp_input_method_v2_commit_string(osk.input_method, data);
		zwp_input_method_v2_commit(osk.input_method, osk.serial);
		osk.compositing[0] = '\0';
	}

	return true;
}

JNIEXPORT jboolean JNICALL Java_android_inputmethodservice_InputMethodService_00024ATLInputConnection_nativeDeleteSurroundingText(JNIEnv *env, jobject this, jlong ptr, jint beforeLength, jint afterLength)
{
	INFO("nativeDeleteSurroundingText(before=%d, after=%d)\n", beforeLength, afterLength);

	if (osk.input_method) {
		zwp_input_method_v2_delete_surrounding_text(osk.input_method, beforeLength, afterLength);
		osk.cursor -= beforeLength;
		zwp_input_method_v2_commit(osk.input_method, osk.serial);
	}

	return true;
}

JNIEXPORT jboolean JNICALL Java_android_inputmethodservice_InputMethodService_00024ATLInputConnection_nativeSetSelection(JNIEnv *env, jobject this, jlong ptr, jint start, jint end)
{
	INFO("nativeSetSelection(start=%d, end=%d)\n", start, end);

	if (osk.input_method) {
	}

	return true;
}

JNIEXPORT jboolean JNICALL Java_android_inputmethodservice_InputMethodService_00024ATLInputConnection_nativeSendKeyEvent(JNIEnv *env, jobject this, jlong ptr, jlong time, jlong key, jlong state)
{
	INFO("nativeSendKeyEvent(time=%ld, key=%ld, state=%ld)\n", ptr, time, key, state);

	if (key == 67 /* KEYCODE_DEL */ && state == 1 /* ACTION_UP */) {
		if (osk.input_method) {
			zwp_input_method_v2_delete_surrounding_text(osk.input_method, 1, 0);
			zwp_input_method_v2_commit(osk.input_method, osk.serial);
		}
	}

	return true;
}

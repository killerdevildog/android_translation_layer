#include <gtk/gtk.h>

#include "../defines.h"
#include "../generated_headers/android_view_WindowManagerImpl.h"
#include "gdk/gdk.h"
#include "glib-object.h"

#define FIRST_SUB_WINDOW 1000
#define LAST_SUB_WINDOW  1999

extern GtkWindow *window;

JNIEXPORT void JNICALL Java_android_view_WindowManagerImpl_native_1addView(JNIEnv *env, jclass clazz, jlong widget_ptr, jint type, jint x, jint y, jint width, jint height)
{
	GtkWidget *widget = _PTR(widget_ptr);
	if (type < FIRST_SUB_WINDOW || type > LAST_SUB_WINDOW) {
		// TODO: handle toplevel windows properly
		printf("WARNING: non subwindow types not implemented properly in WindowManagerImpl\n");
	}
	GtkPopover *popover = GTK_POPOVER(g_object_ref(gtk_popover_new()));
	gtk_popover_set_child(popover, gtk_widget_get_parent(widget));
	printf("::: x=%d, y=%d, width=%d, height=%d\n", x, y, width, height);
	gtk_popover_set_autohide(popover, FALSE);
	gtk_popover_set_pointing_to(popover, &(GdkRectangle){.x = x, .y = y});
	gtk_widget_insert_before(GTK_WIDGET(popover), gtk_window_get_child(window), NULL);
	gtk_popover_present(popover);
	gtk_popover_popup(popover);
	gtk_widget_queue_allocate(gtk_widget_get_parent(gtk_window_get_child(window)));
}

JNIEXPORT void JNICALL Java_android_view_WindowManagerImpl_native_1updateViewLayout(JNIEnv *env, jclass clazz, jlong widget_ptr, jint x, jint y, jint width, jint height)
{
	GtkPopover *popover = GTK_POPOVER(gtk_widget_get_parent(gtk_widget_get_parent(gtk_widget_get_parent(GTK_WIDGET(_PTR(widget_ptr))))));
	printf("updateViewLayout::: x=%d, y=%d, width=%d, height=%d\n", x, y, width, height);
	gtk_popover_set_pointing_to(popover, &(GdkRectangle){.x = x, .y = y});
}

JNIEXPORT void JNICALL Java_android_view_WindowManagerImpl_native_1removeView(JNIEnv *env, jclass clazz, jlong widget_ptr)
{
	GtkPopover *popover = GTK_POPOVER(gtk_widget_get_parent(gtk_widget_get_parent(gtk_widget_get_parent(GTK_WIDGET(_PTR(widget_ptr))))));
	gtk_popover_popdown(popover);
	gtk_widget_unparent(GTK_WIDGET(popover));
	gtk_popover_set_child(popover, NULL);
	g_object_unref(popover);
}

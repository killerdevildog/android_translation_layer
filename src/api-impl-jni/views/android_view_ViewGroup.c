#include <gtk/gtk.h>

#include "../defines.h"
#include "../util.h"

#include "../views/AndroidLayout.h"
#include "../widgets/WrapperWidget.h"

#include "../generated_headers/android_view_View.h"
#include "../generated_headers/android_view_ViewGroup.h"

JNIEXPORT void JNICALL Java_android_view_ViewGroup_native_1addView(JNIEnv *env, jobject this, jlong widget, jlong child, jint index, jobject layout_params)
{
	if (layout_params) {
		/*
		GtkWidget *_child = gtk_widget_get_parent(GTK_WIDGET(_PTR(child)));
		jint child_width = -1;
		jint child_height = -1;

		jint child_width = _GET_INT_FIELD(layout_params, "width");
		jint child_height = _GET_INT_FIELD(layout_params, "height");

		jint child_gravity = _GET_INT_FIELD(layout_params, "gravity");

		if(child_width > 0)
			g_object_set(G_OBJECT(_child), "width-request", child_width, NULL);
		if(child_height > 0)
			g_object_set(G_OBJECT(_child), "height-request", child_height, NULL);

		if(child_gravity != -1) {
			printf(":::-: setting child gravity: %d", child_gravity);
			Java_android_view_View_setGravity(env, child, child_gravity);
		}*/
	}
	GtkWidget *parent = _PTR(widget);
	GtkWidget *iter = gtk_widget_get_first_child(parent);
	for (int i = 0; i < index; i++) {
		iter = gtk_widget_get_next_sibling(iter);
		if (iter == NULL)
			break;
	}

	gtk_widget_insert_before(gtk_widget_get_parent(GTK_WIDGET(_PTR(child))), parent, iter);
}

JNIEXPORT void JNICALL Java_android_view_ViewGroup_native_1removeView(JNIEnv *env, jobject this, jlong widget, jlong child)
{
	gtk_widget_unparent(gtk_widget_get_parent(GTK_WIDGET(_PTR(child))));
}

JNIEXPORT void JNICALL Java_android_view_ViewGroup_native_1drawChildren(JNIEnv *env, jobject this, jlong widget_ptr, jlong snapshot_ptr)
{
	WrapperWidget *wrapper = WRAPPER_WIDGET(gtk_widget_get_parent(GTK_WIDGET(_PTR(widget_ptr))));
	GdkSnapshot *snapshot = GDK_SNAPSHOT(_PTR(snapshot_ptr));
	gtk_widget_snapshot_child(&wrapper->parent_instance, wrapper->child, snapshot);
}

JNIEXPORT void JNICALL Java_android_view_ViewGroup_native_1drawChild(JNIEnv *env, jobject this, jlong widget_ptr, jlong child_ptr, jlong snapshot_ptr)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));
	GtkWidget *child = gtk_widget_get_parent(GTK_WIDGET(_PTR(child_ptr)));
	GdkSnapshot *snapshot = GDK_SNAPSHOT(_PTR(snapshot_ptr));
	gtk_widget_queue_draw(child); // FIXME: why didn't compose UI invalidate the child?
	gtk_widget_snapshot_child(widget, child, snapshot);
}

/* FIXME: put this in a header */
G_DECLARE_FINAL_TYPE(JavaWidget, java_widget, JAVA, WIDGET, GtkWidget)
bool view_dispatch_motionevent(JNIEnv *env, WrapperWidget *wrapper, GtkPropagationPhase phase, jobject motion_event, gpointer event, int action);

static bool dispatch_motionevent_if_JavaWidget(GtkWidget *widget, GtkPropagationPhase phase, jobject motion_event, GtkWidget *toplevel)
{
	if (!JAVA_IS_WIDGET(widget))
		return false;
	JNIEnv *env = get_jni_env();

	WrapperWidget *wrapper = WRAPPER_WIDGET(gtk_widget_get_parent(widget));
	if (widget == toplevel && wrapper->custom_dispatch_touch) {
		// don't self propagate if the widget already had its chance to handle the event
		return false;
	}
	int action = _GET_INT_FIELD(motion_event, "action");
	return view_dispatch_motionevent(env, wrapper, phase, motion_event, motion_event, action);
}

/* used by atl_propagate_synthetic_motionevent */
#define GDK_ARRAY_ELEMENT_TYPE GtkWidget *
#define GDK_ARRAY_TYPE_NAME    GtkWidgetStack
#define GDK_ARRAY_NAME         gtk_widget_stack
#define GDK_ARRAY_FREE_FUNC    g_object_unref
#define GDK_ARRAY_PREALLOC     16
#include "gdkarrayimpl.c"

/* based on gtk_propagate_event_internal © GTK Team */
bool atl_propagate_synthetic_motionevent(GtkWidget *widget, jobject motionevent, GtkWidget *toplevel)
{
	int handled_event = false;
	GtkWidgetStack widget_array;
	int i;

	/* First, propagate event down */
	gtk_widget_stack_init(&widget_array);
	gtk_widget_stack_append(&widget_array, g_object_ref(widget));

	for (;;) {
		widget = gtk_widget_get_parent(widget);
		if (!widget)
			break;

		if (widget == toplevel)
			break;

		gtk_widget_stack_append(&widget_array, g_object_ref(widget));
	}

	i = gtk_widget_stack_get_size(&widget_array) - 1;
	for (;;) {
		widget = gtk_widget_stack_get(&widget_array, i);

		if (!gtk_widget_is_sensitive(widget)) {
			handled_event = true;
		} else if (gtk_widget_get_realized(widget))
			handled_event = dispatch_motionevent_if_JavaWidget(widget, GTK_PHASE_CAPTURE, motionevent, toplevel);

		handled_event |= !gtk_widget_get_realized(widget);

		if (handled_event)
			break;

		if (i == 0)
			break;

		i--;
	}

	/* If not yet handled, also propagate back up */
	if (!handled_event) {
		/* Propagate event up the widget tree so that
		 * parents can see the button and motion
		 * events of the children.
		 */
		for (i = 0; i < gtk_widget_stack_get_size(&widget_array); i++) {
			widget = gtk_widget_stack_get(&widget_array, i);

			/* Scroll events are special cased here because it
			 * feels wrong when scrolling a GtkViewport, say,
			 * to have children of the viewport eat the scroll
			 * event
			 */
			if (!gtk_widget_is_sensitive(widget))
				handled_event = true;
			else if (gtk_widget_get_realized(widget))
				handled_event = dispatch_motionevent_if_JavaWidget(widget, GTK_PHASE_BUBBLE, motionevent, toplevel);

			handled_event |= !gtk_widget_get_realized(widget);

			if (handled_event)
				break;
		}
	}

	gtk_widget_stack_clear(&widget_array);
	return handled_event;
}

JNIEXPORT jboolean JNICALL Java_android_view_ViewGroup_native_1dispatchTouchEvent(JNIEnv *env, jobject this, jlong widget_ptr, jobject motion_event, jdouble x, jdouble y)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));
	GtkWidget *picked_child = gtk_widget_pick(widget, x, y, GTK_PICK_DEFAULT);

	return atl_propagate_synthetic_motionevent(picked_child, motion_event, widget);
}

#include <gtk/gtk.h>

#include "../defines.h"
#include "../util.h"

#include "WrapperWidget.h"
#include "marshal.h"

#include "../generated_headers/android_view_SurfaceView.h"
#include "android_view_SurfaceView.h"

G_DEFINE_TYPE(SurfaceViewWidget, surface_view_widget, GTK_TYPE_WIDGET)

static void surface_view_widget_init(SurfaceViewWidget *surface_view_widget)
{
}

// resize signal copied from GtkDrawingArea
enum {
	RESIZE,
	LAST_SIGNAL
};

static guint signals[LAST_SIGNAL] = {
	0,
};

static void surface_view_widget_size_allocate(GtkWidget *widget, int width, int height, int baseline)
{
	g_signal_emit(widget, signals[RESIZE], 0, width, height);
	for (GtkWidget *child = gtk_widget_get_first_child(widget); child; child = gtk_widget_get_next_sibling(child)) {
		gtk_widget_size_allocate(child, &(GtkAllocation){.x = 0, .y = 0, .width = width, .height = height}, baseline);
	}
}

static void surface_view_widget_snapshot(GtkWidget *widget, GdkSnapshot *snapshot)
{
	SurfaceViewWidget *surface_view_widget = SURFACE_VIEW_WIDGET(widget);
	if (surface_view_widget->texture) {
		graphene_rect_t bounds = GRAPHENE_RECT_INIT(0, 0, gtk_widget_get_width(widget), gtk_widget_get_height(widget));
		if (surface_view_widget->needs_flip) {
			gtk_snapshot_save(snapshot);
			gtk_snapshot_translate(snapshot, &GRAPHENE_POINT_INIT(0, gtk_widget_get_height(widget)));
			gtk_snapshot_scale(snapshot, 1, -1);
		}
		gtk_snapshot_append_texture(snapshot, surface_view_widget->texture, &bounds);
		if (surface_view_widget->needs_flip)
			gtk_snapshot_restore(snapshot);
	}
	if (surface_view_widget->frame_callback) {
		surface_view_widget->frame_callback(surface_view_widget);
		surface_view_widget->frame_callback = NULL;
	}
}

static void surface_view_widget_dispose(GObject *object)
{
	SurfaceViewWidget *surface_view_widget = SURFACE_VIEW_WIDGET(object);
	if (surface_view_widget->texture) {
		g_object_unref(surface_view_widget->texture);
		surface_view_widget->texture = NULL;
	}
	G_OBJECT_CLASS(surface_view_widget_parent_class)->dispose(object);
}

static void surface_view_widget_class_init(SurfaceViewWidgetClass *class)
{
	GtkWidgetClass *widget_class = GTK_WIDGET_CLASS(class);

	// resize signal copied from GtkDrawingArea
	widget_class->size_allocate = surface_view_widget_size_allocate;
	widget_class->snapshot = surface_view_widget_snapshot;
	G_OBJECT_CLASS(class)->dispose = surface_view_widget_dispose;

	signals[RESIZE] =
	    g_signal_new("resize",
	                 G_TYPE_FROM_CLASS(class),
	                 G_SIGNAL_RUN_LAST,
	                 G_STRUCT_OFFSET(GtkDrawingAreaClass, resize),
	                 NULL, NULL,
	                 g_cclosure_user_marshal_VOID__INT_INT,
	                 G_TYPE_NONE, 2, G_TYPE_INT, G_TYPE_INT);
	g_signal_set_va_marshaller(signals[RESIZE],
	                           G_TYPE_FROM_CLASS(class),
	                           g_cclosure_user_marshal_VOID__INT_INTv);
}

GtkWidget *surface_view_widget_new(void)
{
	return g_object_new(surface_view_widget_get_type(), NULL);
}

void surface_view_widget_set_texture(SurfaceViewWidget *surface_view_widget, GdkTexture *texture, gboolean needs_flip)
{
	if (surface_view_widget->texture)
		g_object_unref(surface_view_widget->texture);
	surface_view_widget->texture = texture;
	surface_view_widget->needs_flip = needs_flip;
	gtk_widget_queue_draw(GTK_WIDGET(surface_view_widget));
}

// ---

struct jni_callback_data {
	JavaVM *jvm;
	jobject this;
	jclass this_class;
	gint resize_width;
	gint resize_height;
};

static gboolean on_resize_delayed(struct jni_callback_data *d)
{
	JNIEnv *env;
	(*d->jvm)->GetEnv(d->jvm, (void **)&env, JNI_VERSION_1_6);

	// TODO: are there cases where returning RGBA_8888 is a bad idea?
	// NOTE: we want to call the private method of android.view.SurfaceView, not the related method with this name in the API
	(*env)->CallVoidMethod(env, d->this, handle_cache.surface_view.surfaceChanged, 1 /*RGBA_8888*/, d->resize_width, d->resize_height);

	return G_SOURCE_REMOVE;
}

static void on_resize(GtkWidget *self, gint width, gint height, struct jni_callback_data *d)
{
	d->resize_width = width;
	d->resize_height = height;

	g_idle_add_full(G_PRIORITY_HIGH_IDLE + 20, G_SOURCE_FUNC(on_resize_delayed), d, NULL);
}

static void on_realize(GtkWidget *self, struct jni_callback_data *d)
{
	JNIEnv *env;
	(*d->jvm)->GetEnv(d->jvm, (void **)&env, JNI_VERSION_1_6);

	// NOTE: we want to call the private method of android.view.SurfaceView, not the related method with this name in the API
	(*env)->CallVoidMethod(env, d->this, handle_cache.surface_view.surfaceCreated);
}

JNIEXPORT jlong JNICALL Java_android_view_SurfaceView_native_1constructor(JNIEnv *env, jobject this, jobject context, jobject attrs)
{
	GtkWidget *wrapper = g_object_ref(wrapper_widget_new());
	GtkWidget *dummy = surface_view_widget_new();
	gtk_widget_set_name(dummy, "dummy widget for SurfaceView");
	GtkWidget *graphics_offload = gtk_graphics_offload_new(dummy);
	wrapper_widget_set_child(WRAPPER_WIDGET(wrapper), graphics_offload);
	wrapper_widget_set_jobject(WRAPPER_WIDGET(wrapper), env, this);
	// TODO: is this correct for all usecases? how do we know when it's not?
	gtk_widget_set_hexpand(wrapper, true);
	gtk_widget_set_vexpand(wrapper, true);

	JavaVM *jvm;
	(*env)->GetJavaVM(env, &jvm);

	struct jni_callback_data *callback_data = malloc(sizeof(struct jni_callback_data));
	callback_data->jvm = jvm;
	callback_data->this = _REF(this);
	callback_data->this_class = _REF((*env)->FindClass(env, "android/view/SurfaceView"));

	g_signal_connect(dummy, "resize", G_CALLBACK(on_resize), callback_data);
	g_signal_connect(dummy, "realize", G_CALLBACK(on_realize), callback_data);

	return _INTPTR(graphics_offload);
}

JNIEXPORT jlong JNICALL Java_android_view_SurfaceView_native_1createSnapshot(JNIEnv *env, jclass class)
{
	return _INTPTR(gtk_snapshot_new());
}

extern GtkWindow *window;

JNIEXPORT void JNICALL Java_android_view_SurfaceView_native_1postSnapshot(JNIEnv *env, jclass class, jlong surface_view, jlong snapshot_ptr)
{
	GtkWidget *view = GTK_WIDGET(_PTR(surface_view));
	SurfaceViewWidget *surface_view_widget = SURFACE_VIEW_WIDGET(gtk_widget_get_first_child(view));
	GtkSnapshot *snapshot = GTK_SNAPSHOT(_PTR(snapshot_ptr));
	static GType renderer_type = 0;
	if (!renderer_type) {
		// Use same renderer type as for onscreen rendering.
		GdkSurface *surface = gdk_surface_new_toplevel(gdk_display_get_default());
		GskRenderer *renderer = gsk_renderer_new_for_surface(surface);
		renderer_type = G_OBJECT_TYPE(renderer);
		gsk_renderer_unrealize(renderer);
		g_object_unref(renderer);
		gdk_surface_destroy(surface);
	}
	GskRenderer *renderer = g_object_new(renderer_type, NULL);
	gsk_renderer_realize(renderer, NULL, NULL);
	GskRenderNode *node = gtk_snapshot_free_to_node(snapshot);
	GdkTexture *texture = gsk_renderer_render_texture(renderer, node, NULL);
	gsk_render_node_unref(node);
	gsk_renderer_unrealize(renderer);
	g_object_unref(renderer);

	surface_view_widget_set_texture(surface_view_widget, texture, FALSE);
}

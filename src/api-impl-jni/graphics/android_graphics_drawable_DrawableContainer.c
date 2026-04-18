#include <gdk/gdk.h>

#include "../defines.h"
#include "../generated_headers/android_graphics_drawable_DrawableContainer.h"

struct _ContainerPaintable {
	GObject parent_instance;
	GdkPaintable *child;
};
G_DECLARE_FINAL_TYPE(ContainerPaintable, container_paintable, CONTAINER, PAINTABLE, GObject)

static void container_paintable_snapshot(GdkPaintable *paintable, GdkSnapshot *snapshot, double width, double height)
{
	ContainerPaintable *container = CONTAINER_PAINTABLE(paintable);
	if (container->child)
		gdk_paintable_snapshot(container->child, snapshot, width, height);
}

static double container_paintable_get_intrinsic_aspect_ratio(GdkPaintable *paintable)
{
	ContainerPaintable *container = CONTAINER_PAINTABLE(paintable);
	if (container->child)
		return gdk_paintable_get_intrinsic_aspect_ratio(container->child);
	else
		return 0;
}

static int container_paintable_get_intrinsic_width(GdkPaintable *paintable)
{
	ContainerPaintable *container = CONTAINER_PAINTABLE(paintable);
	if (container->child)
		return gdk_paintable_get_intrinsic_width(container->child);
	else
		return 0;
}

static int container_paintable_get_intrinsic_height(GdkPaintable *paintable)
{
	ContainerPaintable *container = CONTAINER_PAINTABLE(paintable);
	if (container->child)
		return gdk_paintable_get_intrinsic_height(container->child);
	else
		return 0;
}

static void container_paintable_init(ContainerPaintable *container_paintable)
{
}

static void container_paintable_paintable_init(GdkPaintableInterface *iface)
{
	iface->snapshot = container_paintable_snapshot;
	iface->get_intrinsic_aspect_ratio = container_paintable_get_intrinsic_aspect_ratio;
	iface->get_intrinsic_width = container_paintable_get_intrinsic_width;
	iface->get_intrinsic_height = container_paintable_get_intrinsic_height;
}

static void container_paintable_class_init(ContainerPaintableClass *class)
{
}

G_DEFINE_TYPE_WITH_CODE(ContainerPaintable, container_paintable, G_TYPE_OBJECT,
                        G_IMPLEMENT_INTERFACE(GDK_TYPE_PAINTABLE, container_paintable_paintable_init))

JNIEXPORT jlong JNICALL Java_android_graphics_drawable_DrawableContainer_native_1constructor(JNIEnv *env, jobject this)
{
	return _INTPTR(g_object_new(container_paintable_get_type(), NULL));
}

JNIEXPORT void JNICALL Java_android_graphics_drawable_DrawableContainer_native_1selectChild(JNIEnv *env, jobject this, jlong ptr, jlong child_ptr)
{
	ContainerPaintable *container = CONTAINER_PAINTABLE(_PTR(ptr));
	container->child = GDK_PAINTABLE(_PTR(child_ptr));
	gdk_paintable_invalidate_contents(GDK_PAINTABLE(container));
	gdk_paintable_invalidate_size(GDK_PAINTABLE(container));
}

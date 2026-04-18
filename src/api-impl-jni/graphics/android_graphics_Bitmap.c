#include <gtk/gtk.h>

#include "../defines.h"
#include "../util.h"
#include "gdk/gdk.h"
#include "glib.h"

#include "../generated_headers/android_graphics_Bitmap.h"

JNIEXPORT jlong JNICALL Java_android_graphics_Bitmap_native_1create_1snapshot(JNIEnv *env, jclass class, jlong texture_ptr)
{
	GtkSnapshot *snapshot = gtk_snapshot_new();
	if (texture_ptr) {
		GdkTexture *texture = GDK_TEXTURE(_PTR(texture_ptr));
		gtk_snapshot_append_texture(snapshot, texture, &GRAPHENE_RECT_INIT(0, 0, gdk_texture_get_width(texture), gdk_texture_get_height(texture)));
		g_object_unref(texture);
	}
	return _INTPTR(snapshot);
}

extern GThread *main_thread_id;

JNIEXPORT jlong JNICALL Java_android_graphics_Bitmap_native_1create_1texture(JNIEnv *env, jclass class, jlong snapshot_ptr, jint width, jint height, jint stride, jint format)
{
	static GType renderer_type = 0;
	static GdkDisplay *off_screen_display;
	GtkSnapshot *snapshot = _PTR(snapshot_ptr);
	GskRenderNode *node = snapshot ? gtk_snapshot_free_to_node(snapshot) : NULL;
	GdkTexture *texture = NULL;
	if (node) {
		graphene_rect_t bounds = GRAPHENE_RECT_INIT(0, 0, width, height);
		GskRenderer *renderer;
		if (g_thread_self() == main_thread_id) {
			GObject *default_display = G_OBJECT(gdk_display_get_default());
			while (default_display->ref_count < 100)
				g_object_ref(default_display); // workaround for https://gitlab.gnome.org/GNOME/gtk/-/issues/7848
			if (!off_screen_display) {
				off_screen_display = gdk_display_open(NULL);
				// Create and destroy a dummy surface to get the renderer type
				GdkSurface *surface = gdk_surface_new_toplevel(off_screen_display);
				GskRenderer *renderer = gsk_renderer_new_for_surface(surface);
				renderer_type = G_OBJECT_TYPE(renderer);
				gsk_renderer_unrealize(renderer);
				g_object_unref(renderer);
				gdk_surface_destroy(surface);
			}
			renderer = g_object_new(renderer_type, NULL);
			gsk_renderer_realize_for_display(renderer, off_screen_display, NULL);
		} else {
			renderer = gsk_cairo_renderer_new();
			gsk_renderer_realize(renderer, NULL, NULL);
		}
		texture = gsk_renderer_render_texture(renderer, node, &bounds);
		gsk_render_node_unref(node);
		gsk_renderer_unrealize(renderer);
		g_object_unref(renderer);
	} else {
		if (format == -1) {
			format = GDK_MEMORY_R8G8B8A8;
			stride = width * 4;
		}
		GBytes *bytes = g_bytes_new_take(g_malloc0(height * stride), height * stride);
		texture = gdk_memory_texture_new(width, height, format, bytes, stride);
		g_bytes_unref(bytes);
	}

	return _INTPTR(texture);
}

JNIEXPORT jint JNICALL Java_android_graphics_Bitmap_native_1get_1width(JNIEnv *env, jclass class, jlong texture_ptr)
{
	return gdk_texture_get_width(GDK_TEXTURE(_PTR(texture_ptr)));
}

JNIEXPORT jint JNICALL Java_android_graphics_Bitmap_native_1get_1height(JNIEnv *env, jclass class, jlong texture_ptr)
{
	return gdk_texture_get_height(GDK_TEXTURE(_PTR(texture_ptr)));
}

JNIEXPORT jlong JNICALL Java_android_graphics_Bitmap_native_1erase_1color(JNIEnv *env, jclass class, jint color, jint width, jint height)
{
	GdkRGBA rgba = {
		.red = ((color >> 16) & 0xFF) / 255.f,
		.green = ((color >> 8) & 0xFF) / 255.f,
		.blue = ((color >> 0) & 0xFF) / 255.f,
		.alpha = ((color >> 24) & 0xFF) / 255.f,
	};
	graphene_rect_t bounds = GRAPHENE_RECT_INIT(0, 0, width, height);
	GtkSnapshot *snapshot = gtk_snapshot_new();
	gtk_snapshot_append_color(snapshot, &rgba, &bounds);
	return _INTPTR(snapshot);
}

JNIEXPORT void JNICALL Java_android_graphics_Bitmap_native_1recycle(JNIEnv *env, jclass class, jlong texture_ptr, jlong snapshot_ptr)
{
	if (texture_ptr)
		g_object_unref(GDK_TEXTURE(_PTR(texture_ptr)));
	if (snapshot_ptr)
		g_object_unref(GTK_SNAPSHOT(_PTR(snapshot_ptr)));
}

JNIEXPORT jlong JNICALL Java_android_graphics_Bitmap_native_1ref_1texture(JNIEnv *env, jclass class, jlong texture_ptr)
{
	return _INTPTR(g_object_ref(GDK_TEXTURE(_PTR(texture_ptr))));
}

JNIEXPORT void JNICALL Java_android_graphics_Bitmap_native_1get_1pixels(JNIEnv *env, jclass class, jlong texture_ptr, jintArray pixels, jint offset, jint stride, jint x, jint y, jint width, jint height)
{
	GdkTexture *texture = GDK_TEXTURE(_PTR(texture_ptr));
	if (x != 0 || y != 0 || width != gdk_texture_get_width(texture) || height != gdk_texture_get_height(texture)) {
		printf("Bitmap.readPixels: partial read not supported\n");
		exit(1);
	}
	jint *array = (*env)->GetIntArrayElements(env, pixels, NULL);
	gdk_texture_download(texture, (guchar *)(array + offset), stride * 4);
	(*env)->ReleaseIntArrayElements(env, pixels, array, 0);
}

JNIEXPORT void JNICALL Java_android_graphics_Bitmap_native_1copy_1to_1buffer(JNIEnv *env, jclass class, jlong texture_ptr, jobject buffer, jint memory_format, jint stride)
{
	GdkTexture *texture = GDK_TEXTURE(_PTR(texture_ptr));
	GdkTextureDownloader *downloader = gdk_texture_downloader_new(texture);
	gdk_texture_downloader_set_format(downloader, memory_format);
	jarray array_ref;
	jbyte *array;
	guchar *data = get_nio_buffer(env, buffer, &array_ref, &array);
	gdk_texture_downloader_download_into(downloader, data, stride);
	release_nio_buffer(env, array_ref, array);
	gdk_texture_downloader_free(downloader);
}

JNIEXPORT jbyteArray JNICALL Java_android_graphics_Bitmap_native_1save_1to_1png(JNIEnv *env, jclass class, jlong texture_ptr)
{
	GdkTexture *texture = GDK_TEXTURE(_PTR(texture_ptr));
	GBytes *bytes = gdk_texture_save_to_png_bytes(texture);
	jbyteArray result = (*env)->NewByteArray(env, g_bytes_get_size(bytes));
	gsize size;
	gconstpointer data = g_bytes_get_data(bytes, &size);
	(*env)->SetByteArrayRegion(env, result, 0, size, data);
	g_bytes_unref(bytes);
	return result;
}

JNIEXPORT void JNICALL Java_android_graphics_Bitmap_native_1set_1pixels(JNIEnv *env, jclass class, jlong snapshot_ptr, jintArray pixels, jint offset, jint stride, jint x, jint y, jint width, jint height)
{
	GtkSnapshot *snapshot = GTK_SNAPSHOT(_PTR(snapshot_ptr));
	gpointer data = g_malloc0(height * stride * 4);
	jint *array = (*env)->GetIntArrayElements(env, pixels, NULL);
	memcpy(data, array + offset, height * stride * 4);
	(*env)->ReleaseIntArrayElements(env, pixels, array, 0);
	GBytes *bytes = g_bytes_new_take(data, height * stride * 4);
	GdkTexture *texture = gdk_memory_texture_new(width, height, GDK_MEMORY_R8G8B8A8, bytes, stride * 4);
	g_bytes_unref(bytes);
	gtk_snapshot_append_texture(snapshot, texture, &GRAPHENE_RECT_INIT(x, y, width, height));
	g_object_unref(texture);
}

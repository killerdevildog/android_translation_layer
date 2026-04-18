#include <gtk/gtk.h>
#include <string.h>

#include "../defines.h"
#include "../util.h"

#include "../generated_headers/android_graphics_BitmapFactory.h"

struct _JavaInputStream {
	GInputStream parent_instance;

	jobject is;
	jbyteArray storage;
	int storage_size;
};

G_DECLARE_FINAL_TYPE(JavaInputStream, java_input_stream, ATL, JAVA_INPUT_STREAM, GInputStream);

static gssize java_input_stream_read(GInputStream *gstream, void *buffer, gsize count, GCancellable *cancellable, GError **error)
{
	JavaInputStream *stream = ATL_JAVA_INPUT_STREAM(gstream);
	JNIEnv *env = get_jni_env();

	count = MIN(count, stream->storage_size);

	count = (*env)->CallIntMethod(env, stream->is, _METHOD(_CLASS(stream->is), "read", "([BII)I"), stream->storage, 0, count);
	if (count == -1) { // end of stream
		return 0;
	}
	jbyte *storage_buf = (*env)->GetByteArrayElements(env, stream->storage, NULL);
	memcpy(buffer, storage_buf, count);
	(*env)->ReleaseByteArrayElements(env, stream->storage, storage_buf, 0);

	return count;
}

static void java_input_stream_class_init(JavaInputStreamClass *klass)
{
	klass->parent_class.read_fn = java_input_stream_read;
}

static void java_input_stream_init(JavaInputStream *self)
{
}

G_DEFINE_TYPE(JavaInputStream, java_input_stream, G_TYPE_INPUT_STREAM)

static GInputStream *java_input_stream_new(JNIEnv *env, jobject is, jbyteArray storage)
{
	JavaInputStream *stream = g_object_new(java_input_stream_get_type(), NULL);
	stream->is = is;
	stream->storage = storage;
	stream->storage_size = (*env)->GetArrayLength(env, storage);
	return &stream->parent_instance;
}

JNIEXPORT jlong JNICALL Java_android_graphics_BitmapFactory_nativeDecodeStream(JNIEnv *env, jclass, jobject is, jbyteArray storage, jobject outPadding, jobject opts)
{
	GInputStream *stream = java_input_stream_new(env, is, storage);

	GdkPixbuf *pixbuf = gdk_pixbuf_new_from_stream(stream, NULL, NULL);
	g_object_unref(stream);
	GdkTexture *texture = gdk_texture_new_for_pixbuf(pixbuf);
	g_object_unref(pixbuf);
	return _INTPTR(texture);
}

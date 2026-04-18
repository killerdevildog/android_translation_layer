#include <gtk/gtk.h>

#include "../defines.h"
#include "../util.h"
#include "../generated_headers/android_media_MediaPlayer.h"

static void on_prepared(GtkMediaStream *media_stream)
{
	// play once muted to ensure file is fully loaded
	gtk_media_stream_set_muted(media_stream, true);
	gtk_media_stream_play(media_stream);
}

JNIEXPORT jlong JNICALL Java_android_media_MediaPlayer_native_1setDataSource(JNIEnv *env, jobject this, jstring path_jstr)
{
	const char *path = _CSTRING(path_jstr);

	GtkMediaStream *media_stream = gtk_media_file_new_for_filename(path);

	g_object_set_data(G_OBJECT(media_stream), "media_player", _REF(this));
	g_signal_connect(media_stream, "notify::prepared", G_CALLBACK(on_prepared), NULL);
	return _INTPTR(media_stream);
}

static void on_ended(GtkMediaStream *media_stream, GParamSpec *pspec, jobject listener)
{
	JNIEnv *env = get_jni_env();

	jmethodID onCompletion = _METHOD(_CLASS(listener), "onCompletion", "(Landroid/media/MediaPlayer;)V");
	(*env)->CallVoidMethod(env, listener, onCompletion, g_object_get_data(G_OBJECT(media_stream), "media_player"));
}

JNIEXPORT void JNICALL Java_android_media_MediaPlayer_native_1setOnCompletionListener(JNIEnv *env, jclass this, jlong media_stream_ptr, jobject listener)
{
	GtkMediaStream *media_stream = _PTR(media_stream_ptr);

	g_signal_connect(media_stream, "notify::ended", G_CALLBACK(on_ended), _REF(listener));
}

JNIEXPORT void JNICALL Java_android_media_MediaPlayer_native_1prepare(JNIEnv *env, jclass this, jlong media_stream_ptr)
{
	GtkMediaStream *media_stream = _PTR(media_stream_ptr);

	if (!gtk_media_stream_is_prepared(media_stream)) {
		/* HACK: GtkMediaStream doesn't support synchronous initialization */
		gtk_media_stream_stream_prepared(media_stream, true, true, false, 0);
	}
}

JNIEXPORT void JNICALL Java_android_media_MediaPlayer_native_1start(JNIEnv *env, jclass this, jlong media_stream_ptr)
{
	GtkMediaStream *media_stream = _PTR(media_stream_ptr);

	gtk_media_stream_set_muted(media_stream, false);
	gtk_media_stream_set_volume(media_stream, 1.0);
	gtk_media_stream_play(media_stream);
}

JNIEXPORT jint JNICALL Java_android_media_MediaPlayer_native_1getDuration(JNIEnv *env, jclass this, jlong media_stream_ptr)
{
	GtkMediaStream *media_stream = _PTR(media_stream_ptr);

	// convert from microseconds to milliseconds
	return gtk_media_stream_get_duration(media_stream) / 1000;
}

JNIEXPORT jint JNICALL Java_android_media_MediaPlayer_native_1getCurrentPosition(JNIEnv *env, jclass this, jlong media_stream_ptr)
{
	GtkMediaStream *media_stream = _PTR(media_stream_ptr);

	// convert from microseconds to milliseconds
	return gtk_media_stream_get_timestamp(media_stream) / 1000;
}

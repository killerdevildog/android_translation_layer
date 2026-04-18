#include <gtk/gtk.h>

#include "../defines.h"
#include "../generated_headers/android_media_SoundPool.h"

JNIEXPORT jlong JNICALL Java_android_media_SoundPool_native_1constructor(JNIEnv *env, jclass)
{
	GArray *sound_pool_array = g_array_new(FALSE, FALSE, sizeof(GtkMediaStream *));
	return _INTPTR(sound_pool_array);
}

static void on_prepared(GtkMediaStream *media_stream)
{
	// play once muted to ensure file is fully loaded
	gtk_media_stream_set_muted(media_stream, TRUE);
	gtk_media_stream_play(media_stream);
}

JNIEXPORT jint JNICALL Java_android_media_SoundPool_nativeLoad(JNIEnv *env, jclass, jlong pool, jstring path)
{
	GArray *sound_pool_array = _PTR(pool);
	const char *nativePath = (*env)->GetStringUTFChars(env, path, NULL);
	GtkMediaStream *media_stream = gtk_media_file_new_for_filename(nativePath);
	g_signal_connect(media_stream, "notify::prepared", G_CALLBACK(on_prepared), NULL);
	(*env)->ReleaseStringUTFChars(env, path, nativePath);
	return g_array_append_val(sound_pool_array, media_stream)->len - 1;
}

JNIEXPORT jint JNICALL Java_android_media_SoundPool_nativePlay(JNIEnv *env, jclass, jlong pool, jint soundID)
{
	GArray *sound_pool_array = _PTR(pool);
	GtkMediaStream *media_stream = g_array_index(sound_pool_array, GtkMediaStream *, soundID);
	gtk_media_stream_set_muted(media_stream, FALSE);
	gtk_media_stream_play(media_stream);
	return 0;
}

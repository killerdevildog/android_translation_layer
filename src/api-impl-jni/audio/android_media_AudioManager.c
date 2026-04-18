#include <alsa/asoundlib.h>
#include <gio/gio.h>
#include <stdio.h>

#include "../generated_headers/android_media_AudioManager.h"

static void set_stream_volume(GTask *task, gpointer source_object, gpointer task_data, GCancellable *cancellable)
{
	snd_mixer_t *handle;
	snd_mixer_selem_id_t *sid;
	long minv, maxv;
	int volume = GPOINTER_TO_INT(task_data);

	snd_mixer_open(&handle, 0);
	snd_mixer_attach(handle, "default");
	snd_mixer_selem_register(handle, NULL, NULL);
	snd_mixer_load(handle);

	snd_mixer_selem_id_malloc(&sid);
	snd_mixer_selem_id_set_index(sid, 0);
	snd_mixer_selem_id_set_name(sid, "Master");

	snd_mixer_elem_t *elem = snd_mixer_find_selem(handle, sid);
	if (!elem) {
		fprintf(stderr, "AudioManager: Unable to find alsamixer Master element\n");
		return;
	}

	snd_mixer_selem_get_playback_volume_range(elem, &minv, &maxv);
	snd_mixer_selem_set_playback_volume_all(elem, volume * (maxv - minv) / 100 + minv);

	snd_mixer_close(handle);
	snd_mixer_selem_id_free(sid);
}

JNIEXPORT void JNICALL Java_android_media_AudioManager_nativeSetStreamVolume(JNIEnv *env, jobject clazz, jint volume)
{
	GTask *task = g_task_new(NULL, NULL, NULL, NULL);
	g_task_set_task_data(task, GINT_TO_POINTER(volume), NULL);
	g_task_run_in_thread(task, set_stream_volume);
	g_object_unref(task);
}

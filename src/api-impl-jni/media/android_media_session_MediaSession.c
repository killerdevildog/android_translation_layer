#include "../defines.h"
#include "../util.h"

#include "mpris-dbus.h"

#include "../generated_headers/android_media_session_MediaSession.h"
#include "../generated_headers/android_os_SystemClock.h"

#define MPRIS_OBJECT_NAME "/org/mpris/MediaPlayer2"

MediaPlayer2Player *mpris_player = NULL;
static jobject callback = NULL;
static jlong last_position = 0; // playback_position - SystemClock.elapsedRealtime in ms

static gboolean on_media_player_handle_action(MediaPlayer2Player *mpris_player, GDBusMethodInvocation *invocation, char *method)
{
	if (callback) {
		JNIEnv *env = get_jni_env();
		(*env)->CallVoidMethod(env, callback, _METHOD(_CLASS(callback), method, "()V"));
	}
	g_dbus_method_invocation_return_value(invocation, g_variant_new("()"));
	return TRUE;
}

static gboolean on_media_player_handle_play_pause(MediaPlayer2Player *mpris_player, GDBusMethodInvocation *invocation, gpointer user_data)
{
	gboolean is_playing = !strcmp("Playing", media_player2_player_get_playback_status(mpris_player));
	return on_media_player_handle_action(mpris_player, invocation, is_playing ? "onPause" : "onPlay");
}

static gboolean on_media_player_handle_seek(MediaPlayer2Player *mpris_player, GDBusMethodInvocation *invocation, gint64 offset_us, gpointer user_data)
{
	if (callback) {
		JNIEnv *env = get_jni_env();
		last_position += offset_us / 1000;
		(*env)->CallVoidMethod(env, callback, _METHOD(_CLASS(callback), "onSeekTo", "(J)V"), last_position + Java_android_os_SystemClock_elapsedRealtime(env, NULL));
	}
	media_player2_player_complete_seek(mpris_player, invocation);
	return TRUE;
}

static gboolean on_media_player_handle_set_position(MediaPlayer2Player *mpris_player, GDBusMethodInvocation *invocation, GVariant *trackid, gint64 pos_us, gpointer user_data)
{
	if (callback) {
		JNIEnv *env = get_jni_env();
		(*env)->CallVoidMethod(env, callback, _METHOD(_CLASS(callback), "onSeekTo", "(J)V"), pos_us / 1000);
	}
	media_player2_player_complete_set_position(mpris_player, invocation);
	return TRUE;
}

#define ACTION_PAUSE            (1 << 1)
#define ACTION_PLAY             (1 << 2)
#define ACTION_SKIP_TO_PREVIOUS (1 << 4)
#define ACTION_SKIP_TO_NEXT     (1 << 5)
#define ACTION_SEEK_TO          (1 << 8)

JNIEXPORT void JNICALL Java_android_media_session_MediaSession_nativeSetState(JNIEnv *env, jobject this, jint state,
                                                                              jlong actions, jlong position, jlong update_time, jstring title_str, jstring artist_str, jstring art_url_str)
{
	const char *playback_states[] = {"None", "Stopped", "Paused", "Playing"};
	if (!mpris_player) {
		mpris_player = media_player2_player_skeleton_new();
		g_object_connect(mpris_player,
		                 "signal::handle-play", on_media_player_handle_action, "onPlay",
		                 "signal::handle-pause", on_media_player_handle_action, "onPause",
		                 "signal::handle-next", on_media_player_handle_action, "onSkipToNext",
		                 "signal::handle-previous", on_media_player_handle_action, "onSkipToPrevious",
		                 "signal::handle-play-pause", on_media_player_handle_play_pause, NULL,
		                 "signal::handle-seek", on_media_player_handle_seek, NULL,
		                 "signal::handle-set-position", on_media_player_handle_set_position, NULL,
		                 NULL);
	}
	media_player2_player_set_playback_status(mpris_player, playback_states[state < 4 ? state : 0]);
	media_player2_player_set_position(mpris_player, position * 1000);
	last_position = position - update_time;
	media_player2_player_set_can_control(mpris_player, !!(actions));
	media_player2_player_set_can_play(mpris_player, !!(actions & ACTION_PLAY));
	media_player2_player_set_can_pause(mpris_player, !!(actions & ACTION_PAUSE));
	media_player2_player_set_can_seek(mpris_player, !!(actions & ACTION_SEEK_TO));
	media_player2_player_set_can_go_next(mpris_player, !!(actions & ACTION_SKIP_TO_NEXT));
	media_player2_player_set_can_go_previous(mpris_player, !!(actions & ACTION_SKIP_TO_PREVIOUS));

	GVariantDict dict;
	g_variant_dict_init(&dict, NULL);
	g_variant_dict_insert(&dict, "mpris:trackid", "s", MPRIS_OBJECT_NAME "/Track/0");
	if (art_url_str) {
		const char *art_url = (*env)->GetStringUTFChars(env, art_url_str, NULL);
		g_variant_dict_insert(&dict, "mpris:artUrl", "s", art_url);
		(*env)->ReleaseStringUTFChars(env, art_url_str, art_url);
	}
	if (title_str) {
		const char *title = (*env)->GetStringUTFChars(env, title_str, NULL);
		g_variant_dict_insert(&dict, "xesam:title", "s", title);
		(*env)->ReleaseStringUTFChars(env, title_str, title);
	}
	if (artist_str) {
		const char *artist = (*env)->GetStringUTFChars(env, artist_str, NULL);
		g_variant_dict_insert(&dict, "xesam:artist", "s", artist);
		(*env)->ReleaseStringUTFChars(env, artist_str, artist);
	}
	media_player2_player_set_metadata(mpris_player, g_variant_dict_end(&dict));
}

JNIEXPORT void JNICALL Java_android_media_session_MediaSession_nativeSetCallback(JNIEnv *env, jobject this, jobject new_callback)
{
	callback = _REF(new_callback);
}

#include <alsa/asoundlib.h>

#include "../defines.h"
#include "audio.h"

#include "../generated_headers/android_media_AudioRecord.h"

JNIEXPORT jlong JNICALL Java_android_media_AudioRecord_native_1constructor(JNIEnv *env, jobject this, jint streamType, jint rate, jint channel_config, jint audioFormat, jint buffer_size)
{
	snd_pcm_t *pcm_handle;
	snd_pcm_hw_params_t *params;
	unsigned int channels;
	unsigned int channels_out;
	int ret;

	if (!getenv("ATL_UGLY_ENABLE_MICROPHONE"))
		return 0;

	/* Open the PCM device in playback mode */
	ret = snd_pcm_open(&pcm_handle, PCM_DEVICE, SND_PCM_STREAM_CAPTURE, 0);
	if (ret < 0)
		printf("ERROR: Can't open \"%s\" PCM device. %s\n", PCM_DEVICE, snd_strerror(ret));

	snd_pcm_hw_params_alloca(&params);
	helper_hw_params_init(pcm_handle, params, rate, channel_config, SND_PCM_FORMAT_S16_LE, &channels);

	snd_pcm_uframes_t buffer_size_as_uframes_t = buffer_size / channels / 2; // 2 means PCM16
	snd_pcm_hw_params_set_buffer_size_near(pcm_handle, params, &buffer_size_as_uframes_t);

	/* Write parameters */
	ret = snd_pcm_hw_params(pcm_handle, params);
	if (ret < 0)
		printf("ERROR: Can't set harware parameters. %s\n", snd_strerror(ret));

	snd_pcm_uframes_t period_size;

	ret = snd_pcm_hw_params_get_period_size(params, &period_size, 0);
	if (ret < 0)
		printf("Error calling snd_pcm_hw_params_get_period_size: %s\n", snd_strerror(ret));

	snd_pcm_sw_params_t *sw_params;

	snd_pcm_sw_params_malloc(&sw_params);
	snd_pcm_sw_params_current(pcm_handle, sw_params);

	snd_pcm_sw_params_set_start_threshold(pcm_handle, sw_params, (buffer_size_as_uframes_t / period_size) * period_size);
	snd_pcm_sw_params_set_avail_min(pcm_handle, sw_params, period_size);

	snd_pcm_sw_params(pcm_handle, sw_params);

	snd_pcm_hw_params_get_channels(params, &channels_out);

	_SET_INT_FIELD(this, "channels", channels_out);
	return _INTPTR(pcm_handle);
}

JNIEXPORT jint JNICALL Java_android_media_AudioRecord_getMinBufferSize(JNIEnv *env, jclass this_class, jint sampleRateInHz, jint channelConfig, jint audioFormat)
{
	snd_pcm_t *pcm_handle;
	snd_pcm_hw_params_t *params;
	snd_pcm_uframes_t frames;
	int ret;
	unsigned int num_channels;

	ret = snd_pcm_open(&pcm_handle, PCM_DEVICE, SND_PCM_STREAM_CAPTURE, 0);
	if (ret < 0)
		printf("Error calling snd_pcm_open: %s\n", snd_strerror(ret));

	snd_pcm_hw_params_alloca(&params);
	helper_hw_params_init(pcm_handle, params, sampleRateInHz, channelConfig, SND_PCM_FORMAT_S16_LE, &num_channels); // FIXME: a switch?

	ret = snd_pcm_hw_params(pcm_handle, params);
	if (ret < 0)
		printf("Error calling snd_pcm_hw_params: %s\n", snd_strerror(ret));

	ret = snd_pcm_hw_params_get_period_size(params, &frames, 0);
	if (ret < 0)
		printf("Error calling snd_pcm_hw_params_get_period_size: %s\n", snd_strerror(ret));

	snd_pcm_close(pcm_handle);

	printf("\n\nJava_android_media_AudioRecord_getMinBufferSize is returning: %ld\n\n\n", frames * num_channels * 2);
	return frames * num_channels * 2; // 2 bytes = 16 bits (s16)
}

JNIEXPORT void JNICALL Java_android_media_AudioRecord_native_1record(JNIEnv *env, jobject this, long pcm_handle_ptr)
{
	if (!getenv("ATL_UGLY_ENABLE_MICROPHONE"))
		return;

	snd_pcm_t *pcm_handle = _PTR(pcm_handle_ptr);

	snd_pcm_start(pcm_handle);
}

JNIEXPORT void JNICALL Java_android_media_AudioRecord_native_1stop(JNIEnv *env, jobject this, long pcm_handle_ptr)
{
	if (!getenv("ATL_UGLY_ENABLE_MICROPHONE"))
		return;

	snd_pcm_t *pcm_handle = _PTR(pcm_handle_ptr);

	snd_pcm_drain(pcm_handle);
}

JNIEXPORT jint JNICALL Java_android_media_AudioRecord_native_1read(JNIEnv *env, jobject this, long pcm_handle_ptr, jshortArray audio_data, jint offset_in_shorts, jint frames_to_read)
{
	if (!getenv("ATL_UGLY_ENABLE_MICROPHONE"))
		return 0;

	snd_pcm_t *pcm_handle = _PTR(pcm_handle_ptr);
	snd_pcm_sframes_t frames_read;

	jshort *buffer = (*env)->GetShortArrayElements(env, audio_data, NULL);

	frames_read = snd_pcm_readi(pcm_handle, buffer + offset_in_shorts, frames_to_read);
	if (frames_read == -EPIPE) {
		printf("XRUN.\n");
		snd_pcm_recover(pcm_handle, frames_read, 0);
		frames_read = snd_pcm_readi(pcm_handle, buffer + offset_in_shorts, frames_to_read);
		snd_pcm_start(pcm_handle);
	}
	if (frames_read < 0) {
		printf("ERROR. Can't read from PCM capture device. %s\n", snd_strerror(frames_read));
		frames_read = 0;
	}
	(*env)->ReleaseShortArrayElements(env, audio_data, buffer, 0);

	return frames_read;
}

JNIEXPORT void JNICALL Java_android_media_AudioRecord_native_1release(JNIEnv *env, jobject this, long pcm_handle_ptr)
{
	if (!getenv("ATL_UGLY_ENABLE_MICROPHONE"))
		return;

	snd_pcm_t *pcm_handle = _PTR(pcm_handle_ptr);
	snd_pcm_close(pcm_handle);
}

#include <alsa/asoundlib.h>

#include "audio.h"

void helper_hw_params_init(snd_pcm_t *pcm_handle, snd_pcm_hw_params_t *params, unsigned int rate, int channel_config, snd_pcm_format_t format, unsigned int *channels)
{
	int ret;

	switch (channel_config) {
		case 2:
			*channels = 1;
			break;
		case 12:
			*channels = 2;
			break;
		default:
			*channels = 1;
	}

	snd_pcm_hw_params_any(pcm_handle, params);

	ret = snd_pcm_hw_params_set_access(pcm_handle, params, SND_PCM_ACCESS_RW_INTERLEAVED);
	if (ret < 0)
		printf("ERROR: Can't set interleaved mode. %s\n", snd_strerror(ret));

	ret = snd_pcm_hw_params_set_format(pcm_handle, params, format);
	if (ret < 0)
		printf("ERROR: Can't set format. %s\n", snd_strerror(ret));

	ret = snd_pcm_hw_params_set_channels(pcm_handle, params, *channels);
	if (ret < 0)
		printf("ERROR: Can't set channels number. %s\n", snd_strerror(ret));

	ret = snd_pcm_hw_params_set_rate_near(pcm_handle, params, &rate, 0);
	if (ret < 0)
		printf("ERROR: Can't set rate. %s\n", snd_strerror(ret));
}

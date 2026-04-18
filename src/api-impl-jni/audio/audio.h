#include <alsa/asoundlib.h>

#define PCM_DEVICE "default"

void helper_hw_params_init(snd_pcm_t *pcm_handle, snd_pcm_hw_params_t *params, unsigned int rate, int channel_config, snd_pcm_format_t format, unsigned int *channels);

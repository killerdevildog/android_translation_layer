#include <gudev/gudev.h>

#include <fcntl.h>
#include <stdbool.h>
#include <unistd.h>

#include <linux/input.h>

#include "defines.h"
#include "util.h"
#include "generated_headers/android_hardware_SensorManager.h"

/* finds a feedbackd-recognized vibrator */
char *find_vibrator(void)
{
	char *device_file = NULL;

	GUdevClient *udev_client = g_udev_client_new(NULL);
	GList *udev_devices = g_udev_client_query_by_subsystem(udev_client, "input");
	for (GList *l = udev_devices; l != NULL; l = l->next) {
		GUdevDevice *device = l->data;
		if (!g_strcmp0(g_udev_device_get_property(device, "FEEDBACKD_TYPE"), "vibra")) {
			device_file = strdup(g_udev_device_get_device_file(device));
			break;
		}
	}

	g_list_free_full(udev_devices, g_object_unref);
	g_object_unref(udev_client);

	return device_file;
}

JNIEXPORT jint JNICALL Java_android_os_Vibrator_native_1constructor(JNIEnv *env, jobject this)
{
	char *device_file;

	/* if there are multiple instances of Vibrator for some reason, reuse the fd */
	static int fd = -1;
	if (fd != -1)
		return fd;

	device_file = find_vibrator();
	if (!device_file) {
		g_log(NULL, G_LOG_LEVEL_WARNING, "no feedbackd-recognized vibrator found");
		return -1;
	}

	fd = open(device_file, O_RDWR);
	if (fd < 0) {
		g_log(NULL, G_LOG_LEVEL_WARNING, "cannot open vibrator device '%s': %m", device_file);
		free(device_file);
		return -1;
	}
	free(device_file);

	struct input_event set_gain = {
		.type = EV_FF,
		.code = FF_GAIN,
		/* arbitrary, could possibly be improved */
		.value = 0xFFFFUL * 80 / 100,
	};

	if (write(fd, &set_gain, sizeof(set_gain)) < 0) {
		g_log(NULL, G_LOG_LEVEL_WARNING, "failed to set gain on vibrator: %m");
	}

	return fd;
}

JNIEXPORT void JNICALL Java_android_os_Vibrator_native_1vibrate(JNIEnv *env, jobject this, jint fd, jlong duration)
{
	/* FIXME: not thread-safe */
	static struct ff_effect effect = {.id = -1};

	if (effect.id != -1 && effect.replay.length != duration) {
		ioctl(fd, EVIOCRMFF, effect.id);
		effect.id = -1;
	}

	if (effect.id == -1) {
		/* arbitrary, could possibly be improved */
		effect.type = FF_PERIODIC;
		effect.id = -1;
		effect.u.periodic.waveform = FF_SINE;
		effect.u.periodic.period = 10;
		effect.u.periodic.magnitude = 0x7fff;
		effect.u.periodic.offset = 0;
		effect.u.periodic.phase = 0;
		effect.direction = 0x4000;
		effect.u.periodic.envelope.attack_length = 1000;
		effect.u.periodic.envelope.attack_level = 0x7fff;
		effect.u.periodic.envelope.fade_length = 1000;
		effect.u.periodic.envelope.fade_level = 0x7fff;
		effect.trigger.button = 0;
		effect.trigger.interval = 0;

		effect.replay.length = duration;
		effect.replay.delay = 0;

		ioctl(fd, EVIOCSFF, &effect);
	}

	struct input_event play = {
		.type = EV_FF,
		.code = effect.id,
		.value = 1,
	};

	if (write(fd, (const void *)&play, sizeof(play)) < 0) {
		g_log(NULL, G_LOG_LEVEL_WARNING, "failed to play vibraton: %m");
	}
}

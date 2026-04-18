#include <graphene.h>
#include <libportal/portal.h>
#include <sys/param.h>

#include "../defines.h"
#include "../util.h"
#include "../generated_headers/android_hardware_SensorManager.h"

struct iio_accel_files {
	GString *in_accel_x_raw;
	GString *in_accel_y_raw;
	GString *in_accel_z_raw;
	GString *in_accel_scale;
	GString *in_mount_matrix;
};

struct accel_callback_data {
	JavaVM *jvm;
	jobject listener;
	jclass listener_class;
	jobject sensor;
	struct iio_accel_files *iio_accel_files;
	graphene_matrix_t *matrix;
};
gboolean on_accel_data(struct accel_callback_data *d)
{
	JNIEnv *env;
	(*d->jvm)->GetEnv(d->jvm, (void **)&env, JNI_VERSION_1_6);

	char *x_str;
	char *y_str;
	char *z_str;
	char *scale_str;

	g_file_get_contents(d->iio_accel_files->in_accel_x_raw->str, &x_str, NULL, NULL);
	g_file_get_contents(d->iio_accel_files->in_accel_y_raw->str, &y_str, NULL, NULL);
	g_file_get_contents(d->iio_accel_files->in_accel_z_raw->str, &z_str, NULL, NULL);
	g_file_get_contents(d->iio_accel_files->in_accel_scale->str, &scale_str, NULL, NULL);
	graphene_vec3_t *vector = graphene_vec3_alloc();
	graphene_vec3_init(vector, atof(x_str), atof(y_str), atof(z_str));
	graphene_vec3_scale(vector, (-1) * atof(scale_str), vector); // multiply by -1 to get what android expects
	graphene_matrix_transform_vec3(d->matrix, vector, vector);

	jfloatArray values = (*env)->NewFloatArray(env, 3);
	(*env)->SetFloatArrayRegion(env, values, 0, 3, (jfloat[]){graphene_vec3_get_x(vector), graphene_vec3_get_y(vector), graphene_vec3_get_z(vector)});
	graphene_vec3_free(vector);
	jobject sensor_event = (*env)->NewObject(env, handle_cache.sensor_event.class, handle_cache.sensor_event.constructor, values, d->sensor);

	(*env)->CallVoidMethod(env, d->listener, _METHOD(d->listener_class, "onSensorChanged", "(Landroid/hardware/SensorEvent;)V"), sensor_event);
	(*env)->DeleteLocalRef(env, values);
	(*env)->DeleteLocalRef(env, sensor_event);

	return G_SOURCE_CONTINUE;
}

graphene_matrix_t *get_mount_matrix(char *mount_matrix_path)
{
	graphene_matrix_t *matrix = NULL;

	char *matrix_str;
	g_file_get_contents(mount_matrix_path, &matrix_str, NULL, NULL);

	float matrix_f[] = {1, 0, 0, 0,
	                    0, 1, 0, 0,
	                    0, 0, 1, 0,
	                    0, 0, 0, 1};

	sscanf(matrix_str, "%f, %f, %f; %f, %f, %f; %f, %f, %f",
	       &matrix_f[0], &matrix_f[1], &matrix_f[2],
	       &matrix_f[4], &matrix_f[5], &matrix_f[6],
	       &matrix_f[8], &matrix_f[9], &matrix_f[10]);
	matrix = graphene_matrix_alloc();
	graphene_matrix_init_from_float(matrix, matrix_f);

	return matrix;
}

struct iio_accel_files *get_iio_accel_files(void)
{
	GError *error = NULL;
	const gchar *iio_device_path;
	bool found_accelerometer = false;

	GDir *iio_dir = g_dir_open("/sys/bus/iio/devices", 0, &error);
	if (!iio_dir) {
		g_log(NULL, G_LOG_LEVEL_WARNING, "cannot register accelerometer listener: cannot open /sys/bus/iio/devices (error: %s)", error->message);
		return NULL;
	}

	struct iio_accel_files *iio_accel_files = malloc(sizeof(struct iio_accel_files));
	iio_accel_files->in_accel_x_raw = g_string_new(NULL);
	iio_accel_files->in_accel_y_raw = g_string_new(NULL);
	iio_accel_files->in_accel_z_raw = g_string_new(NULL);
	iio_accel_files->in_accel_scale = g_string_new(NULL);
	iio_accel_files->in_mount_matrix = g_string_new(NULL);

	while ((iio_device_path = g_dir_read_name(iio_dir))) {
		g_string_printf(iio_accel_files->in_accel_x_raw, "/sys/bus/iio/devices/%s/in_accel_x_raw", iio_device_path);
		g_string_printf(iio_accel_files->in_accel_y_raw, "/sys/bus/iio/devices/%s/in_accel_y_raw", iio_device_path);
		g_string_printf(iio_accel_files->in_accel_z_raw, "/sys/bus/iio/devices/%s/in_accel_z_raw", iio_device_path);
		g_string_printf(iio_accel_files->in_accel_scale, "/sys/bus/iio/devices/%s/in_accel_scale", iio_device_path);
		g_string_printf(iio_accel_files->in_mount_matrix, "/sys/bus/iio/devices/%s/in_mount_matrix", iio_device_path);
		if (g_file_test(iio_accel_files->in_accel_x_raw->str, G_FILE_TEST_IS_REGULAR)
		    && g_file_test(iio_accel_files->in_accel_y_raw->str, G_FILE_TEST_IS_REGULAR)
		    && g_file_test(iio_accel_files->in_accel_z_raw->str, G_FILE_TEST_IS_REGULAR)
		    && g_file_test(iio_accel_files->in_accel_scale->str, G_FILE_TEST_IS_REGULAR)
		    && g_file_test(iio_accel_files->in_mount_matrix->str, G_FILE_TEST_IS_REGULAR)) {
			found_accelerometer = true;
			break;
		}
	}

	g_dir_close(iio_dir);

	if (!found_accelerometer) {
		g_log(NULL, G_LOG_LEVEL_WARNING, "cannot register accelerometer listener: haven't found an iio accelerometer");
		g_string_free(iio_accel_files->in_accel_x_raw, true);
		g_string_free(iio_accel_files->in_accel_y_raw, true);
		g_string_free(iio_accel_files->in_accel_z_raw, true);
		g_string_free(iio_accel_files->in_accel_scale, true);
		g_string_free(iio_accel_files->in_mount_matrix, true);
		free(iio_accel_files);
		return NULL;
	}

	return iio_accel_files;
}

JNIEXPORT void JNICALL Java_android_hardware_SensorManager_register_1accelerometer_1listener_1native(JNIEnv *env, jobject this, jobject listener, jobject sensor, jint sampling_period)
{
	JavaVM *jvm;
	(*env)->GetJavaVM(env, &jvm);

	struct iio_accel_files *iio_accel_files = get_iio_accel_files();
	if (!iio_accel_files)
		return;

	struct accel_callback_data *callback_data = malloc(sizeof(struct accel_callback_data));
	callback_data->jvm = jvm;
	callback_data->listener = _REF(listener);
	callback_data->listener_class = _REF(_CLASS(callback_data->listener));
	callback_data->sensor = _REF(sensor);
	callback_data->iio_accel_files = iio_accel_files;
	callback_data->matrix = get_mount_matrix(iio_accel_files->in_mount_matrix->str);

	/* FIXME: too short of a sampling period can lock up the process */
	g_timeout_add(MAX(10, sampling_period), G_SOURCE_FUNC(on_accel_data), callback_data);
}

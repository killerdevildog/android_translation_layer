#include <dlfcn.h>
#include <pthread.h>
#include <stdint.h>

#include <gtk/gtk.h>

#include "src/api-impl-jni/defines.h"
#include "util.h"

const char *attribute_set_get_string(JNIEnv *env, jobject attrs, char *attribute, char *schema)
{
	if (!attrs)
		return NULL;

	if (!schema)
		schema = "http://schemas.android.com/apk/res/android";

	jstring string = (jstring)(*env)->CallObjectMethod(env, attrs, handle_cache.attribute_set.getAttributeValue_string, _JSTRING(schema), _JSTRING(attribute));
	return string ? _CSTRING(string) : NULL;
}

int attribute_set_get_int(JNIEnv *env, jobject attrs, char *attribute, char *schema, int default_value)
{
	if (!attrs)
		return default_value;

	if (!schema)
		schema = "http://schemas.android.com/apk/res/android";

	return (*env)->CallIntMethod(env, attrs, handle_cache.attribute_set.getAttributeValue_int, _JSTRING(schema), _JSTRING(attribute), default_value);
}

JavaVM *jvm;

// TODO: use this everywhere, not just for gdb helper functions
JNIEnv *get_jni_env(void)
{
	JNIEnv *env;
	(*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_6);
	return env;
}

JNIEnv *_gdb_get_jni_env(void)
{
	return get_jni_env();
}

void _gdb_get_java_stack_trace(void)
{
	JNIEnv *env = get_jni_env();
	(*env)->ExceptionDescribe(env);
}

void _gdb_force_java_stack_trace(void)
{
	JNIEnv *env = get_jni_env();
	(*env)->ThrowNew(env, (*env)->FindClass(env, "java/lang/Exception"), "forced stack trace");
	(*env)->ExceptionDescribe(env);
	(*env)->ExceptionClear(env);
}

extern char *apk_path;
void extract_from_apk(const char *path, const char *target)
{
	JNIEnv *env = get_jni_env();
	(*env)->CallStaticVoidMethod(env, handle_cache.asset_manager.class, handle_cache.asset_manager.extractFromAPK, _JSTRING(apk_path), _JSTRING(path), _JSTRING(target));
}

/* logging with fallback to stderr */

typedef int __android_log_vprint_type(int prio, const char *tag, const char *fmt, va_list ap);

static int fallback_verbose_log(int prio, const char *tag, const char *fmt, va_list ap)
{
	int ret;

	static pthread_mutex_t mutex = PTHREAD_MUTEX_INITIALIZER;
	pthread_mutex_lock(&mutex);
	static char buf[1024];
	ret = vsnprintf(buf, sizeof(buf), fmt, ap);
	fprintf(stderr, "%w64u: %s\n", (uint64_t)pthread_self(), buf);
	pthread_mutex_unlock(&mutex);

	return ret;
}

static int android_log_vprintf(int prio, const char *tag, const char *fmt, va_list ap)
{

	static __android_log_vprint_type *_android_log_vprintf = NULL;
	if (!_android_log_vprintf) {
		_android_log_vprintf = dlsym(RTLD_DEFAULT, "__android_log_vprint");

		if (!_android_log_vprintf) {
			_android_log_vprintf = &fallback_verbose_log;
		}
	}

	return _android_log_vprintf(prio, tag, fmt, ap);
}

int android_log_printf(android_LogPriority prio, const char *tag, const char *fmt, ...)
{
	int ret;

	va_list ap;
	va_start(ap, fmt);

	ret = android_log_vprintf(prio, tag, fmt, ap);

	va_end(ap);

	return ret;
}

void *get_nio_buffer(JNIEnv *env, jobject buffer, jarray *array_ref, jbyte **array)
{
	jclass class;
	void *pointer;
	int elementSizeShift, position;

	if (!buffer) {
		*array_ref = NULL;
		return NULL;
	}
	class = _CLASS(buffer);
	pointer = _PTR((*env)->GetLongField(env, buffer, _FIELD_ID(class, "address", "J")));
	elementSizeShift = (*env)->GetIntField(env, buffer, _FIELD_ID(class, "_elementSizeShift", "I"));
	position = (*env)->GetIntField(env, buffer, _FIELD_ID(class, "position", "I"));
	if (pointer) { // buffer is direct
		*array_ref = NULL;
		pointer += position << elementSizeShift;
	} else { // buffer is indirect
		*array_ref = (*env)->CallObjectMethod(env, buffer, _METHOD(class, "array", "()Ljava/lang/Object;"));
		jint offset = (*env)->CallIntMethod(env, buffer, _METHOD(class, "arrayOffset", "()I"));
		pointer = *array = (*env)->GetPrimitiveArrayCritical(env, *array_ref, NULL);
		pointer += (offset + position) << elementSizeShift;
	}
	return pointer;
}

void release_nio_buffer(JNIEnv *env, jarray array_ref, jbyte *array)
{
	if (array_ref)
		(*env)->ReleasePrimitiveArrayCritical(env, array_ref, array, 0);
}

int get_nio_buffer_size(JNIEnv *env, jobject buffer)
{
	jclass class = _CLASS(buffer);
	;
	int limit = (*env)->GetIntField(env, buffer, _FIELD_ID(class, "limit", "I"));
	int position = (*env)->GetIntField(env, buffer, _FIELD_ID(class, "position", "I"));

	return limit - position;
}

/* Calling these functions while snapshotting will cause Gtk to not snapshot the affected widgets.
 * Below are "safe" wrappers which will postpone the calls if inside a snapshot.
 * Specifically, gtk_widget_add_tick_callback will make sure the calls are made in the next
 * Update phase. */

/* callbacks */
static gboolean queue_set_text(GtkWidget *label, GdkFrameClock *frame_clock, gpointer str)
{
	gtk_label_set_text(GTK_LABEL(label), str);
	/* we always call strdup so we always want to free */
	free(str);
	return G_SOURCE_REMOVE;
}

static gboolean queue_queue_allocate(GtkWidget *widget, GdkFrameClock *frame_clock, gpointer user_data)
{
	gtk_widget_queue_allocate(widget);
	return G_SOURCE_REMOVE;
}

static gboolean queue_queue_resize(GtkWidget *widget, GdkFrameClock *frame_clock, gpointer user_data)
{
	gtk_widget_queue_resize(widget);
	return G_SOURCE_REMOVE;
}

/* Some functions call gtk_widget_queue_allocate or similar internally.
 * To prevent that from breaking the snapshotting process, when called at the wrong time,
 * we have to follow those functions with this pile of hacks that will unset the problematic flags. */
extern int snapshot_in_progress;
void atl_ensure_widget_snapshotability(GtkWidget *widget)
{
	if (snapshot_in_progress) {
		GtkAllocation allocation;
		G_GNUC_BEGIN_IGNORE_DEPRECATIONS
		/* we probably don't need to use this deprecated function but it sure is convenient */
		gtk_widget_get_allocation(widget, &allocation);
		G_GNUC_END_IGNORE_DEPRECATIONS
		/* this clears resize request, which seems to be necessary in some cases */
		gtk_widget_get_request_mode(widget);
		gtk_widget_size_allocate(widget, &allocation, gtk_widget_get_baseline(widget));
		// maybe we should schedule a call to queue_queue_allocate, but that causes problems in composeUI apps.

		/* the problematic flags get set all the way up the hierarchy */
		GtkWidget *parent = gtk_widget_get_parent(widget);
		if (parent) {
			atl_ensure_widget_snapshotability(parent);
		}
	}
}

void atl_safe_gtk_label_set_text(GtkLabel *label, const char *str)
{
	if (!snapshot_in_progress) {
		gtk_label_set_text(label, str);
	} else {
		/* strdup since the string may not exist by the time the callback runs */
		gtk_widget_add_tick_callback(GTK_WIDGET(label), queue_set_text, (gpointer)strdup(str), NULL);
	}
}

void atl_safe_gtk_widget_set_visible(GtkWidget *widget, gboolean visible)
{
	gtk_widget_set_visible(widget, visible);
	GtkWidget *parent = gtk_widget_get_parent(widget);
	if (parent) {
		atl_ensure_widget_snapshotability(parent);
	}
}

void atl_safe_gtk_widget_queue_allocate(GtkWidget *widget)
{
	if (!snapshot_in_progress) {
		gtk_widget_queue_allocate(widget);
	} else {
		gtk_widget_add_tick_callback(widget, queue_queue_allocate, NULL, NULL);
	}
}

void atl_safe_gtk_widget_queue_resize(GtkWidget *widget)
{
	if (!snapshot_in_progress) {
		gtk_widget_queue_resize(widget);
	} else {
		gtk_widget_add_tick_callback(widget, queue_queue_resize, NULL, NULL);
	}
}

GVariant *intent_serialize(JNIEnv *env, jobject intent)
{
	if (!intent)
		return NULL;
	jstring action_jstr = _GET_OBJ_FIELD(intent, "action", "Ljava/lang/String;");
	jobject component = _GET_OBJ_FIELD(intent, "component", "Landroid/content/ComponentName;");
	jstring className_jstr = component ? _GET_OBJ_FIELD(component, "mClass", "Ljava/lang/String;") : NULL;
	jstring data_jstr = (*env)->CallObjectMethod(env, intent, handle_cache.intent.getDataString);

	GVariantBuilder extras_builder;
	g_variant_builder_init(&extras_builder, G_VARIANT_TYPE_VARDICT);
	jobject extras = _GET_OBJ_FIELD(intent, "extras", "Landroid/os/Bundle;");
	jobject extras_key_set = (*env)->CallObjectMethod(env, extras, handle_cache.bundle.keySet);
	jobjectArray extras_keys = (*env)->CallObjectMethod(env, extras_key_set, handle_cache.set.toArray);
	jsize extras_keys_length = (*env)->GetArrayLength(env, extras_keys);
	jclass parcelable_class = (*env)->FindClass(env, "android/os/Parcelable");
	for (jint i = 0; i < extras_keys_length; i++) {
		jstring key_jstr = (*env)->GetObjectArrayElement(env, extras_keys, i);
		jobject value_jobj = (*env)->CallObjectMethod(env, extras, handle_cache.bundle.get, key_jstr);
		if (!key_jstr || !value_jobj)
			continue;
		const char *key = (*env)->GetStringUTFChars(env, key_jstr, NULL);
		if ((*env)->IsInstanceOf(env, value_jobj, _CLASS(key_jstr))) {
			const char *value = (*env)->GetStringUTFChars(env, value_jobj, NULL);
			g_variant_builder_add(&extras_builder, "{sv}", key, g_variant_new_string(value));
			(*env)->ReleaseStringUTFChars(env, value_jobj, value);
		} else if ((*env)->IsInstanceOf(env, value_jobj, parcelable_class)) {
			GVariantBuilder parcel_builder;
			g_variant_builder_init(&parcel_builder, G_VARIANT_TYPE_TUPLE);
			jobject parcel = (*env)->NewObject(env, handle_cache.builder_parcel.class, handle_cache.builder_parcel.constructor, _INTPTR(&parcel_builder));
			(*env)->CallVoidMethod(env, parcel, handle_cache.parcel.writeParcelable, value_jobj, 0);
			GVariant *parcel_variant = g_variant_builder_end(&parcel_builder);
			g_variant_builder_add(&extras_builder, "{sv}", key, parcel_variant);
			(*env)->DeleteLocalRef(env, parcel);
		} else {
			printf("intent_serialize: skipping non-string, non-parcelable extra: %s\n", key);
		}
		(*env)->ReleaseStringUTFChars(env, key_jstr, key);
		(*env)->DeleteLocalRef(env, key_jstr);
		(*env)->DeleteLocalRef(env, value_jobj);
	}

	const char *action = action_jstr ? (*env)->GetStringUTFChars(env, action_jstr, NULL) : NULL;
	const char *className = className_jstr ? (*env)->GetStringUTFChars(env, className_jstr, NULL) : NULL;
	const char *data = data_jstr ? (*env)->GetStringUTFChars(env, data_jstr, NULL) : NULL;
	const char *dbus_name = g_application_get_application_id(g_application_get_default());
	GVariant *variant = g_variant_new(INTENT_G_VARIANT_TYPE_STRING, action ?: "", className ?: "", data ?: "", &extras_builder, dbus_name);
	if (action_jstr)
		(*env)->ReleaseStringUTFChars(env, action_jstr, action);
	if (className_jstr)
		(*env)->ReleaseStringUTFChars(env, className_jstr, className);
	if (data_jstr)
		(*env)->ReleaseStringUTFChars(env, data_jstr, data);
	return variant;
}

jobject intent_deserialize(JNIEnv *env, GVariant *variant)
{
	const char *action;
	const char *className;
	const char *data;
	GVariantIter *extras;
	g_variant_get(variant, INTENT_G_VARIANT_TYPE_STRING, &action, &className, &data, &extras, NULL);
	if (action && action[0] == '\0')
		action = NULL;
	if (className && className[0] == '\0')
		className = NULL;
	if (data && data[0] == '\0')
		data = NULL;

	jobject intent = (*env)->NewObject(env, handle_cache.intent.class, handle_cache.intent.constructor);
	_SET_OBJ_FIELD(intent, "action", "Ljava/lang/String;", _JSTRING(action));
	if (className)
		(*env)->CallObjectMethod(env, intent, handle_cache.intent.setClassName, _GET_STATIC_OBJ_FIELD(handle_cache.context.class, "this_application", "Landroid/app/Application;"), _JSTRING(className));
	if (data)
		_SET_OBJ_FIELD(intent, "data", "Landroid/net/Uri;", (*env)->CallStaticObjectMethod(env, handle_cache.uri.class, handle_cache.uri.parse, _JSTRING(data)));
	const char *key;
	GVariant *value;
	while (g_variant_iter_loop(extras, "{sv}", &key, &value)) {
		if (g_variant_is_of_type(value, G_VARIANT_TYPE_STRING)) {
			(*env)->CallObjectMethod(env, intent, handle_cache.intent.putExtraCharSequence, _JSTRING(key), _JSTRING(g_variant_get_string(value, NULL)));
		} else if (g_variant_is_of_type(value, G_VARIANT_TYPE_INT32)) {
			(*env)->CallObjectMethod(env, intent, handle_cache.intent.putExtraInt, _JSTRING(key), g_variant_get_int32(value));
		} else if (g_variant_is_of_type(value, G_VARIANT_TYPE_INT64)) {
			(*env)->CallObjectMethod(env, intent, handle_cache.intent.putExtraLong, _JSTRING(key), g_variant_get_int64(value));
		} else if (g_variant_is_of_type(value, G_VARIANT_TYPE_BYTESTRING)) {
			gsize size;
			const int8_t *message = g_variant_get_fixed_array(value, &size, 1);
			jbyteArray bytesMessage = (*env)->NewByteArray(env, size);
			(*env)->SetByteArrayRegion(env, bytesMessage, 0, size, message);
			(*env)->CallObjectMethod(env, intent, handle_cache.intent.putExtraByteArray, _JSTRING(key), bytesMessage);
		} else if (g_variant_is_of_type(value, G_VARIANT_TYPE_TUPLE)) {
			GVariantIter parcel_iter;
			g_variant_iter_init(&parcel_iter, value);
			jobject parcel = (*env)->NewObject(env, handle_cache.iter_parcel.class, handle_cache.iter_parcel.constructor, _INTPTR(&parcel_iter));
			jmethodID getClassLoader = _METHOD((*env)->FindClass(env, "java/lang/Class"), "getClassLoader", "()Ljava/lang/ClassLoader;");
			jobject class_loader = (*env)->CallObjectMethod(env, handle_cache.parcel.class, getClassLoader);
			jobject parcelable = (*env)->CallObjectMethod(env, parcel, handle_cache.parcel.readParcelable, class_loader);
			if ((*env)->ExceptionCheck(env)) {
				(*env)->ExceptionDescribe(env);
				(*env)->ExceptionClear(env);
			}
			(*env)->CallObjectMethod(env, intent, handle_cache.intent.putExtraParcelable, _JSTRING(key), parcelable);
			(*env)->DeleteLocalRef(env, parcelable);
			(*env)->DeleteLocalRef(env, parcel);
		}
	}
	g_variant_iter_free(extras);
	return intent;
}

const char *intent_actionname_from_type(int type)
{
	switch (type) {
		case 0:
			return "app.startActivity";
		case 1:
			return "app.startService";
		case 2:
			return "app.sendBroadcast";
		default:
			return NULL;
	}
}

#include <gio/gio.h>
#include <jni.h>

#include "../api-impl-jni/defines.h"
#include "../api-impl-jni/util.h"

static void action_start_activity(GSimpleAction *action, GVariant *parameter, gpointer user_data)
{
	JNIEnv *env = get_jni_env();

	jobject intent = intent_deserialize(env, parameter);
	jobject context = _GET_STATIC_OBJ_FIELD(handle_cache.context.class, "this_application", "Landroid/app/Application;");
	(*env)->CallVoidMethod(env, context, handle_cache.context.startActivity, intent);
	if ((*env)->ExceptionCheck(env)) {
		(*env)->ExceptionDescribe(env);
	}
}

static void action_start_service(GSimpleAction *action, GVariant *parameter, gpointer user_data)
{
	JNIEnv *env = get_jni_env();

	jobject intent = intent_deserialize(env, parameter);
	jobject context = _GET_STATIC_OBJ_FIELD(handle_cache.context.class, "this_application", "Landroid/app/Application;");
	(*env)->CallObjectMethod(env, context, handle_cache.context.startService, intent);
	if ((*env)->ExceptionCheck(env)) {
		(*env)->ExceptionDescribe(env);
	}
}

static void action_send_broadcast(GSimpleAction *action, GVariant *parameter, gpointer user_data)
{
	JNIEnv *env = get_jni_env();

	jobject intent = intent_deserialize(env, parameter);
	jobject context = _GET_STATIC_OBJ_FIELD(handle_cache.context.class, "this_application", "Landroid/app/Application;");
	(*env)->CallVoidMethod(env, context, handle_cache.context.sendBroadcast, intent);
	if ((*env)->ExceptionCheck(env)) {
		(*env)->ExceptionDescribe(env);
	}
}

const GActionEntry action_entries[] = {
	{"startActivity", action_start_activity, INTENT_G_VARIANT_TYPE_STRING},
	{ "startService",  action_start_service, INTENT_G_VARIANT_TYPE_STRING},
	{"sendBroadcast", action_send_broadcast, INTENT_G_VARIANT_TYPE_STRING},
};

const int action_entries_count = ARRAY_SIZE(action_entries);

#include "defines.h"
#include "util.h"

#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>

#include <glib.h>

#include "generated_headers/android_os_MessageQueue.h"
#include "../libandroid/looper.h"

struct native_message_queue {
	ALooper *looper;
	bool in_callback;
	bool is_main_thread;
};

GThread *main_thread_id;

/**
 * callback to execute java handlers on glib managed thread loops
 */
static gboolean dispatch_func(GSource *source, GSourceFunc callback, gpointer user_data)
{
	JavaVM *jvm = user_data;
	JNIEnv *env;
	(*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_6);
	g_source_set_ready_time(source, -1); // clear previous timeout
	(*env)->CallStaticVoidMethod(env, handle_cache.looper.class, handle_cache.looper.loop);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);

	return G_SOURCE_CONTINUE;
}
static GSourceFuncs source_funcs = {
	.dispatch = dispatch_func,
};
static GSource *source = NULL;

void prepare_main_looper(JNIEnv *env)
{
	main_thread_id = g_thread_self();

	(*env)->CallStaticVoidMethod(env, handle_cache.looper.class, handle_cache.looper.prepareMainLooper);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
	source = g_source_new(&source_funcs, sizeof(GSource));
	JavaVM *jvm;
	(*env)->GetJavaVM(env, &jvm);
	g_source_set_callback(source, NULL, jvm, NULL); // func can be NULL, as we handle everything in the dispatch_func
	g_source_set_ready_time(source, 0);             // first call will be immediately
	g_source_attach(source, NULL);
}

JNIEXPORT jlong JNICALL Java_android_os_MessageQueue_nativeInit(JNIEnv *env, jclass this)
{
	struct native_message_queue *message_queue = malloc(sizeof(struct native_message_queue));

	message_queue->in_callback = false;
	message_queue->looper = ALooper_prepare(0);
	message_queue->is_main_thread = g_thread_self() == main_thread_id;

	return _INTPTR(message_queue);
}

JNIEXPORT void JNICALL Java_android_os_MessageQueue_nativeDestroy(JNIEnv *env, jclass this, jlong ptr)
{
	struct native_message_queue *message_queue = _PTR(ptr);

	free(message_queue);
}

JNIEXPORT jboolean JNICALL Java_android_os_MessageQueue_nativePollOnce(JNIEnv *env, jclass this, jlong ptr, jint timeout_millis)
{
	struct native_message_queue *message_queue = _PTR(ptr);

	if (message_queue->is_main_thread) { // thread loop is managed by glib
		if (timeout_millis) {
			if (timeout_millis != -1) // -1 means no more messages
				g_source_set_ready_time(source, g_source_get_time(source) + timeout_millis * 1000L);
			return true; // indicate that java side should return to block in glib managed loop
		} else {
			return false;
		}
	}

	//printf("Java_android_os_MessageQueue_nativePollOnce: entry (timeout: %d)\n", timeout_millis);
	message_queue->in_callback = true;
	ALooper_pollOnce(timeout_millis, NULL, NULL, NULL);
	message_queue->in_callback = false;
	//printf("Java_android_os_MessageQueue_nativePollOnce: exit\n");

	/* TODO: what's with the exception stuff */
	return false;
}

JNIEXPORT void JNICALL Java_android_os_MessageQueue_nativeWake(JNIEnv *env, jclass this, jlong ptr)
{
	struct native_message_queue *message_queue = _PTR(ptr);

	if (message_queue->is_main_thread) {        // thread loop is managed by glib
		g_source_set_ready_time(source, 0); // immediately
		return;
	}

	ALooper_wake(message_queue->looper);
}

JNIEXPORT jboolean JNICALL Java_android_os_MessageQueue_nativeIsIdling(JNIEnv *env, jclass this, jlong ptr)
{
	struct native_message_queue *message_queue = _PTR(ptr);

	return ALooper_isPolling(message_queue->looper);
}

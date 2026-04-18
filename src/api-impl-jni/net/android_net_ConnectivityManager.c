#include <gio/gio.h>

#include "../defines.h"
#include "../util.h"

#include "../generated_headers/android_net_ConnectivityManager.h"

JNIEXPORT jboolean JNICALL Java_android_net_ConnectivityManager_isActiveNetworkMetered(JNIEnv *env, jobject this)
{
	return g_network_monitor_get_network_metered(g_network_monitor_get_default());
}

JNIEXPORT jboolean JNICALL Java_android_net_ConnectivityManager_nativeGetNetworkAvailable(JNIEnv *env, jobject this)
{
	return g_network_monitor_get_network_available(g_network_monitor_get_default());
}

static void on_network_changed(GNetworkMonitor *self, gboolean network_available, jobject callback)
{
	JNIEnv *env = get_jni_env();
	jmethodID method;
	if (network_available) {
		method = _METHOD(_CLASS(callback), "onAvailable", "(Landroid/net/Network;)V");
	} else {
		method = _METHOD(_CLASS(callback), "onLost", "(Landroid/net/Network;)V");
	}
	(*env)->CallVoidMethod(env, callback, method, NULL);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
}

JNIEXPORT void JNICALL Java_android_net_ConnectivityManager_registerNetworkCallback(JNIEnv *env, jobject this, jobject request, jobject callback)
{
	g_signal_connect(g_network_monitor_get_default(), "network-changed", G_CALLBACK(on_network_changed), _REF(callback));
}

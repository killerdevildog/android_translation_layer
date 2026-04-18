#include <libportal/portal.h>

#include "../defines.h"
#include "../generated_headers/android_location_LocationManager.h"

static void location_updated(
    XdpPortal *self,
    gdouble latitude,
    gdouble longitude,
    gdouble altitude,
    gdouble accuracy,
    gdouble speed,
    gdouble heading,
    gchar *description,
    gint64 timestamp_s,
    gint64 timestamp_ms,
    JavaVM *jvm)
{
	JNIEnv *env;
	(*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_6);
	jclass class = (*env)->FindClass(env, "android/location/LocationManager");
	jlong timestamp = timestamp_s * 1000 + timestamp_ms;
	(*env)->CallStaticVoidMethod(env, class, _STATIC_METHOD(class, "locationUpdated", "(DDDDDDJ)V"), latitude, longitude, altitude, accuracy, speed, heading, timestamp);
}

JNIEXPORT void JNICALL Java_android_location_LocationManager_nativeGetLocation(JNIEnv *env, jobject)
{
	if (!getenv("ATL_UGLY_ENABLE_LOCATION")) {
		// Location access is prohibited by default until sanboxing is implemented.
		// Set ATL_UGLY_ENABLE_LOCATION environment variable to enable it.
		return;
	}

	static XdpPortal *portal = NULL;
	if (!portal) {
		portal = xdp_portal_new();
		JavaVM *jvm;
		(*env)->GetJavaVM(env, &jvm);
		g_signal_connect(portal, "location-updated", G_CALLBACK(location_updated), jvm);
	}

	xdp_portal_location_monitor_start(portal, NULL, 0, 0, XDP_LOCATION_ACCURACY_EXACT, XDP_LOCATION_MONITOR_FLAG_NONE, NULL, NULL, NULL);
}

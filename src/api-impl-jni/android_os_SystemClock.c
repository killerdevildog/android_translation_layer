#include <math.h>
#include <time.h>

#include "generated_headers/android_os_SystemClock.h"

JNIEXPORT jlong JNICALL Java_android_os_SystemClock_elapsedRealtime(JNIEnv *env, jclass this)
{
	struct timespec t;
	clock_gettime(CLOCK_BOOTTIME, &t);
	jlong ret = t.tv_sec * 1000 + lround(t.tv_nsec / 1e6);
	return ret;
}

JNIEXPORT jlong JNICALL Java_android_os_SystemClock_uptimeMillis(JNIEnv *env, jclass this)
{
	struct timespec now;
	clock_gettime(CLOCK_MONOTONIC, &now);
	return now.tv_sec * 1000 + lround(now.tv_nsec / 1e6);
}

JNIEXPORT jlong JNICALL Java_android_os_SystemClock_elapsedRealtimeNanos(JNIEnv *env, jclass this)
{
	struct timespec t;
	clock_gettime(CLOCK_BOOTTIME, &t);
	return t.tv_sec * 1000000000 + t.tv_nsec;
}

JNIEXPORT jlong JNICALL Java_android_os_SystemClock_currentThreadTimeMillis(JNIEnv *env, jclass this)
{
	struct timespec now;
	clock_gettime(CLOCK_THREAD_CPUTIME_ID, &now);
	return now.tv_sec * 1000 + lround(now.tv_nsec / 1e6);
}

/*
 * parts of this file originally from AOSP:
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <dlfcn.h>
#include <errno.h>
#include <fcntl.h>
#include <poll.h>
#include <pthread.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include "defines.h"
#include "util.h"

//#include "generated_headers/android_util_AssetManager.h"
//#include "generated_headers/android_view_Surface.h"

#include "generated_headers/android_app_NativeActivity.h"
#include "native_activity.h"

//#include "JNIHelp.h"
//#include "android_os_MessageQueue.h"
//#include "android_view_InputChannel.h"
//#include "android_view_KeyEvent.h"

/*static struct {
	jmethodID finish;
	jmethodID setWindowFlags;
	jmethodID setWindowFormat;
	jmethodID showIme;
	jmethodID hideIme;
} gNativeActivityClassInfo;*/

typedef void ANativeActivity_createFunc(ANativeActivity *activity, void *savedState, size_t savedStateSize);

// ------------------------------------------------------------------------

struct ActivityWork {
	int32_t cmd;
	int32_t arg1;
	int32_t arg2;
};

enum {
	CMD_FINISH = 1,
	CMD_SET_WINDOW_FORMAT,
	CMD_SET_WINDOW_FLAGS,
	CMD_SHOW_SOFT_INPUT,
	CMD_HIDE_SOFT_INPUT,
};

/*static void write_work(int fd, int32_t cmd, int32_t arg1, int32_t arg2)
{
	struct ActivityWork work;
	work.cmd = cmd;
	work.arg1 = arg1;
	work.arg2 = arg2;

	printf("write_work: cmd=%d", cmd);

restart:
	int res = write(fd, &work, sizeof(work));
	if (res < 0 && errno == EINTR) {
		goto restart;
	}

	if (res == sizeof(work)) return;

	if (res < 0) printf("Failed writing to work fd: %s", strerror(errno));
	else printf("Truncated writing to work fd: %d", res);
}*/

/*static bool read_work(int fd, struct ActivityWork* outWork)
{
	int res = read(fd, outWork, sizeof(struct ActivityWork));
	// no need to worry about EINTR, poll loop will just come back again.
	if (res == sizeof(struct ActivityWork)) return true;

	if (res < 0) printf("Failed reading work fd: %s", strerror(errno));
	else printf("Truncated reading work fd: %d", res);
	return false;
}*/

/*
 * Native state for interacting with the NativeActivity class.
 */

struct NativeCode {
	// must have offset of 0 so that our struct acts as a transparent wrapper
	struct ANativeActivity native_activity;

	ANativeActivityCallbacks callbacks;

	void *dlhandle;
	ANativeActivity_createFunc *createActivityFunc;

	//const char* internalDataPathObj;
	//const char* externalDataPathObj;
	//const char* obbPathObj;

	ANativeWindow *nativeWindow;
	int32_t lastWindowWidth;
	int32_t lastWindowHeight;

	// These are used to wake up the main thread to process work.
	//int mainWorkRead;
	//int mainWorkWrite;
	//MessageQueue *messageQueue;
};

struct NativeCode *NativeCode_new(void *_dlhandle, ANativeActivity_createFunc *_createFunc)
{
	struct NativeCode *this = malloc(sizeof(struct NativeCode));
	memset(&this->callbacks, 0, sizeof(this->callbacks));
	this->dlhandle = _dlhandle;
	this->createActivityFunc = _createFunc;
	this->nativeWindow = NULL;
	//this->mainWorkRead = this->mainWorkWrite = -1;

	return this;
}

// FIXME: this is in libandroid.so, should use header files
ANativeWindow *ANativeWindow_fromSurface(JNIEnv *env, jobject surface);
void ANativeWindow_release(ANativeWindow *native_window);
struct AssetManager *AAssetManager_fromJava(JNIEnv *env, jobject asset_manager);

void NativeCode_setSurface(struct NativeCode *this, jobject _surface)
{
	if (_surface != NULL) {
		this->nativeWindow = ANativeWindow_fromSurface(this->native_activity.env, _surface);
	} else {
		if (this->nativeWindow) {
			ANativeWindow_release(this->nativeWindow);
			this->nativeWindow = NULL;
		}
	}
}

void NativeCode_destroy(struct NativeCode *this)
{
	if (this->callbacks.onDestroy != NULL) {
		this->callbacks.onDestroy((struct ANativeActivity *)this);
	}
	//if (env != NULL && clazz != NULL) {
	//	(*env)->DeleteGlobalRef(env, clazz);
	//}
	//if (messageQueue != NULL && mainWorkRead >= 0) {
	//	messageQueue->getLooper()->removeFd(mainWorkRead);
	//}
	NativeCode_setSurface(this, NULL);
	//if (this->mainWorkRead >= 0) close(this->mainWorkRead);
	//if (this->mainWorkWrite >= 0) close(this->mainWorkWrite);
	if (this->dlhandle != NULL) {
		// for now don't unload...  we probably should clean this
		// up and only keep one open dlhandle per proc, since there
		// is really no benefit to unloading the code.
		//dlclose(this->dlhandle);
	}
	free(this);
}

// ------------------------------------------------------------------------

/*
 * Callback for handling native events on the application's main thread.
 */
/*static int mainWorkCallback(int fd, int events, void* data)
{
	struct NativeCode* code = (struct NativeCode*)data;
	if ((events & POLLIN) == 0) {
		printf("STUB - mainWorkCallback - returning -1\n");
		return 1;
	}

	struct ActivityWork work;
	if (!read_work(code->mainWorkRead, &work)) {
		return 1;
	}

	printf("STUB - mainWorkCallback\n");
	//printf("mainWorkCallback: cmd=%d", work.cmd);

	switch (work.cmd) {
		case CMD_FINISH: {
			code->(*env)->CallVoidMethod(env, code->clazz, gNativeActivityClassInfo.finish);
			code->messageQueue->raiseAndClearException(code->env, "finish");
		} break;
		case CMD_SET_WINDOW_FORMAT: {
			code->(*env)->CallVoidMethod(env, code->clazz,
					gNativeActivityClassInfo.setWindowFormat, work.arg1);
			code->messageQueue->raiseAndClearException(code->env, "setWindowFormat");
		} break;
		case CMD_SET_WINDOW_FLAGS: {
			code->(*env)->CallVoidMethod(env, code->clazz,
					gNativeActivityClassInfo.setWindowFlags, work.arg1, work.arg2);
			code->messageQueue->raiseAndClearException(code->env, "setWindowFlags");
		} break;
		case CMD_SHOW_SOFT_INPUT: {
			code->(*env)->CallVoidMethod(env, code->clazz,
					gNativeActivityClassInfo.showIme, work.arg1);
			code->messageQueue->raiseAndClearException(code->env, "showIme");
		} break;
		case CMD_HIDE_SOFT_INPUT: {
			code->(*env)->CallVoidMethod(env, code->clazz,
					gNativeActivityClassInfo.hideIme, work.arg1);
			code->messageQueue->raiseAndClearException(code->env, "hideIme");
		} break;
		default:
			printf("Unknown work command: %d", work.cmd);
			break;
	}

	return 1;
}*/

// ------------------------------------------------------------------------

void *bionic_dlopen(const char *filename, int flag);
void *bionic_dlsym(void *handle, const char *symbol);

// constructor: android::Looper::Looper(bool)
void _ZN7android6LooperC2Eb(void *dest, bool allowNonCallbacks);
typedef int (*Looper_callbackFunc)(int fd, int events, void *data);
// android::Looper::addFd
int _ZN7android6Looper5addFdEiiiPFiiiPvES1_(void *this, int fd, int ident, int events, Looper_callbackFunc callback, void *data);
// android::Looper::pollOnce
int _ZN7android6Looper8pollOnceEiPiS1_PPv(void *this, int timeoutMillis, int *outFd, int *outEvents, void **outData);
#define ALOOPER_EVENT_INPUT (1 << 0)

/*static pthread_t looper_thread;

static void * looper_thread_worker(void *looper)
{
	printf("!!!!! in looper_thread_worker\n");
	_ZN7android6Looper8pollOnceEiPiS1_PPv(looper, -1, NULL, NULL, NULL);
	printf("!!!!! pollOnce returned\n");
}*/

jlong Java_android_app_NativeActivity_loadNativeCode(JNIEnv *env, jobject clazz, jstring path, jstring funcName,
                                                     jobject messageQueue, jstring internalDataDir, jstring obbDir,
                                                     jstring externalDataDir, int sdkVersion,
                                                     jobject jAssetMgr, jbyteArray savedState)
{
	const char *pathStr = (*env)->GetStringUTFChars(env, path, NULL);
	struct NativeCode *code = NULL;

	static void *libnb_handle = NULL;
	bool (*NativeBridgeIsSupported)(const char *);
	void *(*NativeBridgeLoadLibrary)(const char *, int);
	void *(*NativeBridgeGetTrampoline)(void *, const char *, const char *, uint32_t);
	if (!libnb_handle) {
		libnb_handle = dlopen("libnativebridge.so", RTLD_LAZY);
		NativeBridgeIsSupported = dlsym(libnb_handle, "NativeBridgeIsSupported");
		NativeBridgeLoadLibrary = dlsym(libnb_handle, "NativeBridgeLoadLibrary");
		NativeBridgeGetTrampoline = dlsym(libnb_handle, "NativeBridgeGetTrampoline");
	}

	bool use_native_bridge = NativeBridgeIsSupported(pathStr);

	void *handle;
	if (use_native_bridge)
		handle = NativeBridgeLoadLibrary(pathStr, RTLD_LAZY);
	else
		handle = bionic_dlopen(pathStr, RTLD_LAZY);

	(*env)->ReleaseStringUTFChars(env, path, pathStr);

	if (handle != NULL) {
		const char *funcStr = (*env)->GetStringUTFChars(env, funcName, NULL);
		ANativeActivity_createFunc *create_func;
		if (use_native_bridge)
			create_func = NativeBridgeGetTrampoline(handle, funcStr, NULL, 0);
		else
			create_func = bionic_dlsym(handle, funcStr);
		code = NativeCode_new(handle, (ANativeActivity_createFunc *)create_func);
		(*env)->ReleaseStringUTFChars(env, funcName, funcStr);

		if (code->createActivityFunc == NULL) {
			printf("ANativeActivity_onCreate not found\n");
			NativeCode_destroy(code);
			return 0;
		}
		/*
		code->messageQueue = android_os_MessageQueue_getMessageQueue(env, messageQueue);
		if (code->messageQueue == NULL) {
			printf("Unable to retrieve native MessageQueue\n");
			NativeCode_destroy(code);
			return 0;
		}*/

		int msgpipe[2];
		if (pipe(msgpipe)) {
			fprintf(stderr, "could not create pipe: %s", strerror(errno));
			NativeCode_destroy(code);
			return 0;
		}
#if 0
		code->mainWorkRead = msgpipe[0];
		code->mainWorkWrite = msgpipe[1];
		int result = fcntl(code->mainWorkRead, F_SETFL, O_NONBLOCK);
		SLOGW_IF(result != 0, "Could not make main work read pipe "
				"non-blocking: %s", strerror(errno));
		result = fcntl(code->mainWorkWrite, F_SETFL, O_NONBLOCK);
		SLOGW_IF(result != 0, "Could not make main work write pipe "
				"non-blocking: %s", strerror(errno));
		code->messageQueue->getLooper()->addFd(
				code->mainWorkRead, 0, ALOOPER_EVENT_INPUT, mainWorkCallback, code);

		// new android::Looper()

		void *a_looper = malloc(224/*sizeof(android::Looper)*/);
		_ZN7android6LooperC2Eb(a_looper, true); // TODO: or false?
		// android::Looper::addFd
		_ZN7android6Looper5addFdEiiiPFiiiPvES1_(a_looper, code->mainWorkRead, 0, ALOOPER_EVENT_INPUT, mainWorkCallback, code);
		pthread_create(&looper_thread, NULL, looper_thread_worker, a_looper);
#endif
		code->native_activity.callbacks = &code->callbacks;
		if ((*env)->GetJavaVM(env, &code->native_activity.vm) < 0) {
			printf("NativeActivity GetJavaVM failed\n");
			NativeCode_destroy(code);
			return 0;
		}
		code->native_activity.env = env;
		code->native_activity.clazz = (*env)->NewGlobalRef(env, clazz);

		char *tmp;
		code->native_activity.internalDataPath = strdup(tmp = (char *)((*env)->GetStringUTFChars(env, internalDataDir, NULL)));
		(*env)->ReleaseStringUTFChars(env, internalDataDir, tmp);

		if (externalDataDir != NULL) {
			code->native_activity.externalDataPath = strdup(tmp = (char *)((*env)->GetStringUTFChars(env, externalDataDir, NULL)));
			(*env)->ReleaseStringUTFChars(env, externalDataDir, tmp);
		} else {
			code->native_activity.externalDataPath = NULL; // TODO: or ""?
		}

		code->native_activity.sdkVersion = sdkVersion;

		code->native_activity.assetManager = AAssetManager_fromJava(env, jAssetMgr);

		if (obbDir != NULL) {
			code->native_activity.obbPath = (*env)->GetStringUTFChars(env, obbDir, NULL);
			(*env)->ReleaseStringUTFChars(env, obbDir, code->native_activity.obbPath);
		} else {
			code->native_activity.obbPath = NULL; // TODO: or ""?
		}

		jbyte *rawSavedState = NULL;
		jsize rawSavedSize = 0;
		if (savedState != NULL) {
			rawSavedState = (*env)->GetByteArrayElements(env, savedState, NULL);
			rawSavedSize = (*env)->GetArrayLength(env, savedState);
		}

		code->createActivityFunc((struct ANativeActivity *)code, rawSavedState, rawSavedSize);

		if (rawSavedState != NULL) {
			(*env)->ReleaseByteArrayElements(env, savedState, rawSavedState, 0);
		}
	}

	return _INTPTR(code);
}

void Java_android_app_NativeActivity_unloadNativeCode(JNIEnv *env, jobject clazz, jlong handle)
{
	printf("STUB - unloadNativeCode_native\n");
	/*if (handle != 0) {
		struct NativeCode* code = (struct NativeCode*)_PTR(handle);
		NativeCode_destroy(code);
	}*/
}

void Java_android_app_NativeActivity_onStartNative(JNIEnv *env, jobject clazz, jlong handle)
{
	if (handle != 0) {
		struct NativeCode *code = (struct NativeCode *)_PTR(handle);
		if (code->callbacks.onStart != NULL) {
			code->callbacks.onStart((ANativeActivity *)code);
		}
	}
}

void Java_android_app_NativeActivity_onResumeNative(JNIEnv *env, jobject clazz, jlong handle)
{
	if (handle != 0) {
		struct NativeCode *code = (struct NativeCode *)_PTR(handle);
		if (code->callbacks.onResume != NULL) {
			code->callbacks.onResume((ANativeActivity *)code);
		}
	}
}

jbyteArray Java_android_app_NativeActivity_onSaveInstanceStateNative(JNIEnv *env, jobject clazz, jlong handle)
{
	printf("STUB - onSaveInstanceState_native\n");
	/*
	jbyteArray array = NULL;

	if (handle != 0) {
		struct NativeCode* code = (struct NativeCode*)_PTR(handle);
		if (code->callbacks.onSaveInstanceState != NULL) {
			size_t len = 0;
			jbyte* state = (jbyte*)code->callbacks.onSaveInstanceState((ANativeActivity *)code, &len);
			if (len > 0) {
				array = (*env)->NewByteArray(env, len);
				if (array != NULL) {
					(*env)->SetByteArrayRegion(env, array, 0, len, state);
				}
			}
			if (state != NULL) {
				free(state);
			}
		}
	}

	return array;*/
	return NULL;
}

void Java_android_app_NativeActivity_onPauseNative(JNIEnv *env, jobject clazz, jlong handle)
{
	if (handle != 0) {
		struct NativeCode *code = (struct NativeCode *)_PTR(handle);
		if (code->callbacks.onPause != NULL) {
			code->callbacks.onPause((ANativeActivity *)code);
		}
	}
}

void Java_android_app_NativeActivity_onStopNative(JNIEnv *env, jobject clazz, jlong handle)
{
	if (handle != 0) {
		struct NativeCode *code = (struct NativeCode *)_PTR(handle);
		if (code->callbacks.onStop != NULL) {
			code->callbacks.onStop((ANativeActivity *)code);
		}
	}
}

void Java_android_app_NativeActivity_onConfigurationChangedNative(JNIEnv *env, jobject clazz, jlong handle)
{
	printf("STUB - onConfigurationChanged_native\n");
	/*if (handle != 0) {
		struct NativeCode* code = (struct NativeCode*)_PTR(handle);
		if (code->callbacks.onConfigurationChanged != NULL) {
			code->callbacks.onConfigurationChanged((ANativeActivity *)code);
		}
	}*/
}

void Java_android_app_NativeActivity_onLowMemoryNative(JNIEnv *env, jobject clazz, jlong handle)
{
	printf("STUB - onLowMemory_native\n");
	/*if (handle != 0) {
		struct NativeCode* code = (struct NativeCode*)_PTR(handle);
		if (code->callbacks.onLowMemory != NULL) {
			code->callbacks.onLowMemory((ANativeActivity *)code);
		}
	}*/
}

void Java_android_app_NativeActivity_onWindowFocusChangedNative(JNIEnv *env, jobject clazz, jlong handle, jboolean focused)
{
	if (handle != 0) {
		struct NativeCode *code = (struct NativeCode *)_PTR(handle);
		if (code->callbacks.onWindowFocusChanged != NULL) {
			code->callbacks.onWindowFocusChanged((ANativeActivity *)code, focused ? 1 : 0);
		}
	}
}

void Java_android_app_NativeActivity_onSurfaceCreatedNative(JNIEnv *env, jobject clazz, jlong handle, jobject surface)
{
	printf("STUB - onSurfaceCreated_native\n");
	/*	if (handle != 0) {
		struct NativeCode* code = (struct NativeCode*)_PTR(handle);
		code->setSurface(surface);
		if (code->nativeWindow != NULL && code->callbacks.onNativeWindowCreated != NULL) {
			code->callbacks.onNativeWindowCreated((ANativeActivity *)code, code->nativeWindow.get());
		}
	}*/
}

void Java_android_app_NativeActivity_onSurfaceChangedNative(JNIEnv *env, jobject clazz, jlong handle, jobject surface,
                                                            jint format, jint width, jint height)
{
	if (handle != 0) {
		struct NativeCode *code = (struct NativeCode *)_PTR(handle);
		ANativeWindow *oldNativeWindow = code->nativeWindow;
		NativeCode_setSurface(code, surface);
		if (oldNativeWindow != code->nativeWindow) {
			if (oldNativeWindow != NULL && code->callbacks.onNativeWindowDestroyed != NULL) {
				code->callbacks.onNativeWindowDestroyed((ANativeActivity *)code, oldNativeWindow);
				ANativeWindow_release(oldNativeWindow); // TODO: can it happen that this will be done by the callback and we will have double free?
			}
			if (code->nativeWindow != NULL) {
				if (code->callbacks.onNativeWindowCreated != NULL) {
					code->callbacks.onNativeWindowCreated((ANativeActivity *)code, code->nativeWindow);
				}
				code->lastWindowWidth = width;
				code->lastWindowHeight = height;
			}
		} else {
			// Maybe it resized?
			if (width != code->lastWindowWidth
			    || height != code->lastWindowHeight) {
				if (code->callbacks.onNativeWindowResized != NULL) {
					code->callbacks.onNativeWindowResized((ANativeActivity *)code, code->nativeWindow);
				}
			}
		}
	}
}

void Java_android_app_NativeActivity_onSurfaceRedrawNeededNative(JNIEnv *env, jobject clazz, jlong handle, jobject surface /*?*/)
{
	printf("STUB - onSurfaceRedrawNeeded_native\n");
	/*if (handle != 0) {
		struct NativeCode* code = (struct NativeCode*)_PTR(handle);
		if (code->nativeWindow != NULL && code->callbacks.onNativeWindowRedrawNeeded != NULL) {
			code->callbacks.onNativeWindowRedrawNeeded((ANativeActivity *)code, code->nativeWindow.get());
		}
	}*/
}

void Java_android_app_NativeActivity_onSurfaceDestroyedNative(JNIEnv *env, jobject clazz, jlong handle)
{
	printf("STUB - onSurfaceDestroyed_native\n");
	/*if (handle != 0) {
		struct NativeCode* code = (struct NativeCode*)_PTR(handle);
		if (code->nativeWindow != NULL && code->callbacks.onNativeWindowDestroyed != NULL) {
			code->callbacks.onNativeWindowDestroyed(code,
					code->nativeWindow.get());
		}
		code->setSurface(NULL);
	}*/
}

void Java_android_app_NativeActivity_onInputQueueCreatedNative(JNIEnv *env, jobject clazz, jlong handle, jlong queue)
{
	printf("STUB - onInputChannelCreated_native\n");
	if (handle != 0) {
		struct NativeCode *code = (struct NativeCode *)_PTR(handle);
		if (code->callbacks.onInputQueueCreated != NULL) {
			code->callbacks.onInputQueueCreated((ANativeActivity *)code, (AInputQueue *)_PTR(queue));
		}
	}
}

void Java_android_app_NativeActivity_onInputQueueDestroyedNative(JNIEnv *env, jobject clazz, jlong handle, jlong queuePtr)
{
	printf("STUB - onInputChannelDestroyed_native\n");
	/*if (handle != 0) {
		struct NativeCode* code = (struct NativeCode*)_PTR(handle);
		if (code->callbacks.onInputQueueDestroyed != NULL) {
			AInputQueue* queue = reinterpret_cast<AInputQueue*>(queuePtr);
			code->callbacks.onInputQueueDestroyed((ANativeActivity *)code, queue);
		}
	}*/
}

void Java_android_app_NativeActivity_onContentRectChangedNative(JNIEnv *env, jobject clazz, jlong handle,
                                                                jint x, jint y, jint w, jint h)
{
	printf("STUB - onContentRectChanged_native\n");
	/*if (handle != 0) {
		struct NativeCode* code = (struct NativeCode*)_PTR(handle);
		if (code->callbacks.onContentRectChanged != NULL) {
			ARect rect;
			rect.left = x;
			rect.top = y;
			rect.right = x+w;
			rect.bottom = y+h;
			code->callbacks.onContentRectChanged((ANativeActivity *)code, &rect);
		}
	}*/
}

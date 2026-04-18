#include <stdio.h>

#include "defines.h"
#include "util.h"

/* copied from AOSP:
**
** Copyright 2006, The Android Open Source Project
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
**	 http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/

JNIEXPORT jint JNICALL Java_android_util_Log_println_1native(JNIEnv *env, jobject this, jint bufID, jint priority, jstring tagObj, jstring msgObj)
{
	const char *tag = NULL;
	const char *msg = NULL;

	if (msgObj == NULL) {
		//jniThrowNullPointerException(env, "println needs a message");
		fprintf(stderr, "Log.println_native: println needs a message\n");
		return -1;
	}

	if (bufID < 0 || bufID >= LOG_ID_MAX) {
		//jniThrowNullPointerException(env, "bad bufID");
		fprintf(stderr, "Log.println_native: bad bufID\n");
		return -1;
	}

	if (tagObj != NULL)
		tag = (*env)->GetStringUTFChars(env, tagObj, NULL);
	msg = (*env)->GetStringUTFChars(env, msgObj, NULL);

	int res = __android_log_buf_write(bufID, priority, tag, msg);

	if (tag != NULL)
		(*env)->ReleaseStringUTFChars(env, tagObj, tag);
	(*env)->ReleaseStringUTFChars(env, msgObj, msg);

	return res;
}

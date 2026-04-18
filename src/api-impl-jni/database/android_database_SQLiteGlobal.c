/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
** Modified to support SQLite extensions by the SQLite developers:
** sqlite-dev@sqlite.org.
*/
/*
** Rewritten from C++ to C for Android Translation Layer:
*/

#include <jni.h>

#include <sqlite3.h>

#include "../generated_headers/android_database_sqlite_SQLiteGlobal.h"

// Limit heap to 8MB for now.  This is 4 times the maximum cursor window
// size, as has been used by the original code in SQLiteDatabase for
// a long time.
static const int SOFT_HEAP_LIMIT = 8 * 1024 * 1024;

JNIEXPORT jint JNICALL Java_android_database_sqlite_SQLiteGlobal_nativeReleaseMemory(JNIEnv *env, jclass clazz)
{
	return sqlite3_release_memory(SOFT_HEAP_LIMIT);
}

# Copyright (C) 2008 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH:= $(call my-dir)

dex_src_files := \
	CmdUtils.cpp \
	DexCatch.cpp \
	DexClass.cpp \
	DexDataMap.cpp \
	DexDebugInfo.cpp \
	DexFile.cpp \
	DexInlines.cpp \
	DexOptData.cpp \
	DexOpcodes.cpp \
	DexProto.cpp \
	DexSwapVerify.cpp \
	DexUtf.cpp \
	InstrUtils.cpp \
	Leb128.cpp \
	OptInvocation.cpp \
	sha1.cpp \
	SysUtil.cpp \

dex_include_files := \
	dalvik \
	external/zlib \
	external/safe-iop/include

##
##
## Build the host version of libdex
##
##
include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(dex_src_files)
LOCAL_C_INCLUDES += $(dex_include_files)
LOCAL_SHARED_LIBRARIES := liblog libutils libziparchive
LOCAL_MODULE_HOST_OS := darwin linux windows
LOCAL_MODULE := libdex
include $(BUILD_HOST_SHARED_LIBRARY)

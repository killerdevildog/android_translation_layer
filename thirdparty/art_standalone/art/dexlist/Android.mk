LOCAL_PATH := $(call my-dir)
include art/build/Android.common_build.mk
# link dexlist binary
include $(CLEAR_VARS)
LOCAL_MODULE := dexlist
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   = $(ART_HOST_CFLAGS)
LOCAL_SRC_FILES = dexlist.cc
LOCAL_SHARED_LIBRARIES =  \
        libdexfile \
        libartbase \
        libbase
LOCAL_C_INCLUDES = $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
include $(BUILD_HOST_EXECUTABLE)

include art/build/Android.common_build.mk
# link dexlists binary
include $(CLEAR_VARS)
LOCAL_MODULE := dexlists
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   = $(ART_HOST_CFLAGS)
LOCAL_SRC_FILES = dexlist.cc
LOCAL_C_INCLUDES = $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
include $(BUILD_HOST_EXECUTABLE)


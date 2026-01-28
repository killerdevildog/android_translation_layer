LOCAL_PATH := $(call my-dir)
include art/build/Android.common_build.mk
# link timeout_dumper binary
include $(CLEAR_VARS)
LOCAL_MODULE := timeout_dumper
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   = $(ART_HOST_CFLAGS)
LOCAL_SRC_FILES = timeout_dumper.cc
LOCAL_SHARED_LIBRARIES =  \
        libbacktrace \
        libbase
LOCAL_C_INCLUDES = $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
include $(BUILD_HOST_EXECUTABLE)


LOCAL_PATH := $(call my-dir)
include art/build/Android.common_build.mk
# link dmtracedump binary
include $(CLEAR_VARS)
LOCAL_MODULE := dmtracedump
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   =  \
        $(ART_HOST_CFLAGS) \
        -O0 \
        -g \
        -Wall
LOCAL_SRC_FILES = tracedump.cc
LOCAL_C_INCLUDES = $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
include $(BUILD_HOST_EXECUTABLE)

include art/build/Android.common_build.mk
# link create_test_dmtrace binary
include $(CLEAR_VARS)
LOCAL_MODULE := create_test_dmtrace
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   =  \
        $(ART_HOST_CFLAGS) \
        -O0 \
        -g \
        -Wall
LOCAL_SRC_FILES = createtesttrace.cc
LOCAL_C_INCLUDES = $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
include $(BUILD_HOST_EXECUTABLE)


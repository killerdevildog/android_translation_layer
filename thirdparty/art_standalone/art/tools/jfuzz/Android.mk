LOCAL_PATH := $(call my-dir)
# link jfuzz binary
include $(CLEAR_VARS)
LOCAL_MODULE := jfuzz
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   =  \
        -O0 \
        -g \
        -Wall
LOCAL_SRC_FILES = jfuzz.cc
include $(BUILD_HOST_EXECUTABLE)


LOCAL_PATH := $(call my-dir)
# link libammtestjni shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libammtestjni
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_SRC_FILES = AmmTest/jni/ammtest.c
include $(BUILD_HOST_SHARED_LIBRARY)


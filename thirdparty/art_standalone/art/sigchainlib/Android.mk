LOCAL_PATH := $(call my-dir)
include art/build/Android.common_build.mk
# link libsigchain shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libsigchain
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   = $(ART_HOST_CFLAGS)
LOCAL_SRC_FILES = sigchain.cc
LOCAL_C_INCLUDES = $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
include $(BUILD_HOST_SHARED_LIBRARY)

# link libsigchain static library
include $(CLEAR_VARS)
LOCAL_MODULE := libsigchain
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   = $(ART_HOST_CFLAGS)
LOCAL_SRC_FILES = sigchain.cc
LOCAL_C_INCLUDES = $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
include $(BUILD_HOST_STATIC_LIBRARY)

# link libsigchain_dummy static library
include $(CLEAR_VARS)
LOCAL_MODULE := libsigchain_dummy
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_SRC_FILES = sigchain_dummy.cc
include $(BUILD_HOST_STATIC_LIBRARY)


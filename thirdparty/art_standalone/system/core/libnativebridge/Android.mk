LOCAL_PATH:= $(call my-dir)

NATIVE_BRIDGE_COMMON_SRC_FILES := \
  native_bridge.cc

# Shared library for host
# ========================================================
include $(CLEAR_VARS)

LOCAL_MODULE:= libnativebridge

LOCAL_SRC_FILES:= $(NATIVE_BRIDGE_COMMON_SRC_FILES)
LOCAL_CLANG := true
LOCAL_CPP_EXTENSION := .cc
LOCAL_CFLAGS += -Werror -Wall
LOCAL_CPPFLAGS := -std=gnu++11 -fvisibility=protected
LOCAL_LDFLAGS := -ldl
LOCAL_SHARED_LIBRARIES := liblog
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include

include $(BUILD_HOST_SHARED_LIBRARY)

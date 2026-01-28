LOCAL_PATH := $(call my-dir)
include art/build/Android.common_build.mk
# link dalvikvm binary
include $(CLEAR_VARS)
LOCAL_MODULE := dalvikvm
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   =  \
        $(ART_HOST_CFLAGS) \
        -Wall \
        -Wextra
LOCAL_LDFLAGS  = -Wl,--export-dynamic
LOCAL_SRC_FILES = dalvikvm.cc
LOCAL_SHARED_LIBRARIES = libnativehelper
LOCAL_WHOLE_STATIC_LIBRARIES = libsigchain
LOCAL_C_INCLUDES = $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
include $(BUILD_HOST_EXECUTABLE)


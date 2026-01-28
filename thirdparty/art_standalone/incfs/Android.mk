LOCAL_PATH := $(call my-dir)
libincfs_defaults_common_CFLAGS   =  \
        -Wall \
        -Wextra \
        -Wno-deprecated-enum-enum-conversion \
        -D_FILE_OFFSET_BITS=64

# link libincfs-utils static library
include $(CLEAR_VARS)
LOCAL_MODULE := libincfs-utils
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   =  \
        $(libincfs_defaults_common_CFLAGS)
LOCAL_SRC_FILES_ =  \
        util/map_ptr.cpp
LOCAL_SRC_FILES = $(sort $(LOCAL_SRC_FILES_))
LOCAL_SHARED_LIBRARIES =  \
        libbase \
        libutils
LOCAL_EXPORT_C_INCLUDE_DIRS =  \
        $(libincfs_defaults_common_EXPORT_INCLUDE_DIRS) \
        $(LOCAL_PATH)/util/include
LOCAL_C_INCLUDES = $(LOCAL_EXPORT_C_INCLUDE_DIRS)

include $(BUILD_HOST_STATIC_LIBRARY)

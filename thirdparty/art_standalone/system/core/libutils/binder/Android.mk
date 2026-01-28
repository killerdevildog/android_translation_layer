LOCAL_PATH := $(call my-dir)
libutils_binder_impl_defaults_nodeps_CFLAGS   =  \
        -Winvalid-offsetof \
        -Wsequence-point \
        -Wzero-as-null-pointer-constant
libutils_binder_impl_defaults_nodeps_SRCS     =  \
        Errors.cpp \
        RefBase.cpp \
        SharedBuffer.cpp \
        String16.cpp \
        String8.cpp \
        StrongPointer.cpp \
        Unicode.cpp \
        VectorImpl.cpp
libutils_binder_impl_defaults_nodeps_EXPORT_INCLUDE_DIRS = $(LOCAL_PATH)/include
libutils_binder_impl_defaults_nodeps_INCLUDE_DIRS = $(LOCAL_PATH)/../include

libutils_binder_impl_defaults_CXXFLAGS = $(libutils_binder_impl_defaults_nodeps_CXXFLAGS)
libutils_binder_impl_defaults_CFLAGS   = $(libutils_binder_impl_defaults_nodeps_CFLAGS)
libutils_binder_impl_defaults_LDFLAGS  = $(libutils_binder_impl_defaults_nodeps_LDFLAGS)
libutils_binder_impl_defaults_LDLIBS   = $(libutils_binder_impl_defaults_nodeps_LDLIBS)
libutils_binder_impl_defaults_SRCS     = $(libutils_binder_impl_defaults_nodeps_SRCS)
libutils_binder_impl_defaults_INCLUDE_DIRS = $(libutils_binder_impl_defaults_nodeps_INCLUDE_DIRS)
libutils_binder_impl_defaults_EXPORT_INCLUDE_DIRS = $(libutils_binder_impl_defaults_nodeps_EXPORT_INCLUDE_DIRS)
libutils_binder_impl_defaults_GENERATED_SOURCES = $(libutils_binder_impl_defaults_nodeps_GENERATED_SOURCES)

# link libutils_binder static library
include $(CLEAR_VARS)
LOCAL_MODULE := libutils_binder
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CPPFLAGS = $(libutils_binder_impl_defaults_CXXFLAGS)
LOCAL_CFLAGS   = $(libutils_binder_impl_defaults_CFLAGS)
LOCAL_LDFLAGS  = $(libutils_binder_impl_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libutils_binder_impl_defaults_LDLIBS)
LOCAL_SRC_FILES_ = $(libutils_binder_impl_defaults_SRCS)
LOCAL_SRC_FILES = $(sort $(LOCAL_SRC_FILES_))
LOCAL_SHARED_LIBRARIES = $(libutils_binder_impl_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libutils_binder_impl_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES = $(libutils_binder_impl_defaults_INCLUDE_DIRS)
$(eval $(libutils_binder_impl_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

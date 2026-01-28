LOCAL_PATH := $(call my-dir)
libutils_defaults_nodeps_CFLAGS   =  \
        -Wall \
        -Wno-exit-time-destructors \
        -DANDROID_UTILS_REF_BASE_DISABLE_IMPLICIT_CONSTRUCTION
libutils_defaults_nodeps_EXPORT_INCLUDE_DIRS = $(LOCAL_PATH)/include
libutils_defaults_nodeps_INCLUDE_DIRS = $(libutils_defaults_nodeps_EXPORT_INCLUDE_DIRS)

libutils_defaults_CXXFLAGS = $(libutils_defaults_nodeps_CXXFLAGS)
libutils_defaults_CFLAGS   = $(libutils_defaults_nodeps_CFLAGS)
libutils_defaults_LDFLAGS  = $(libutils_defaults_nodeps_LDFLAGS)
libutils_defaults_LDLIBS   = $(libutils_defaults_nodeps_LDLIBS)
libutils_defaults_SHARED_LIBS =  \
        libcutils \
        liblog
libutils_defaults_SRCS     = $(libutils_defaults_nodeps_SRCS)
libutils_defaults_INCLUDE_DIRS = $(libutils_defaults_nodeps_INCLUDE_DIRS)
libutils_defaults_EXPORT_INCLUDE_DIRS = $(libutils_defaults_nodeps_EXPORT_INCLUDE_DIRS)
libutils_defaults_GENERATED_SOURCES = $(libutils_defaults_nodeps_GENERATED_SOURCES)

libutils_impl_defaults_CXXFLAGS = $(libutils_defaults_CXXFLAGS)
libutils_impl_defaults_CFLAGS   = $(libutils_defaults_CFLAGS)
libutils_impl_defaults_LDFLAGS  = $(libutils_defaults_LDFLAGS)
libutils_impl_defaults_LDLIBS   = $(libutils_defaults_LDLIBS)
libutils_impl_defaults_SHARED_LIBS = \
        libbase
libutils_impl_defaults_SRCS     =  \
        $(libutils_defaults_SRCS) \
        Looper.cpp \
        FileMap.cpp \
        JenkinsHash.cpp \
        LightRefBase.cpp \
        NativeHandle.cpp \
        Printer.cpp \
        StopWatch.cpp \
        SystemClock.cpp \
        Threads.cpp \
        Timers.cpp \
        Tokenizer.cpp \
        misc.cpp
libutils_impl_defaults_INCLUDE_DIRS = $(libutils_defaults_INCLUDE_DIRS)
libutils_impl_defaults_EXPORT_INCLUDE_DIRS = $(libutils_defaults_EXPORT_INCLUDE_DIRS)
libutils_impl_defaults_GENERATED_SOURCES = $(libutils_defaults_GENERATED_SOURCES)

# link libutils shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libutils
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CPPFLAGS = $(libutils_impl_defaults_CXXFLAGS)
LOCAL_CFLAGS   = $(libutils_impl_defaults_CFLAGS)
LOCAL_LDFLAGS  = $(libutils_impl_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libutils_impl_defaults_LDLIBS)
LOCAL_SRC_FILES_ = $(libutils_impl_defaults_SRCS)
LOCAL_SRC_FILES = $(sort $(LOCAL_SRC_FILES_))
LOCAL_SHARED_LIBRARIES = $(libutils_impl_defaults_SHARED_LIBS)
LOCAL_WHOLE_STATIC_LIBRARIES = libutils_binder
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libutils_impl_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES = $(libutils_impl_defaults_INCLUDE_DIRS)
$(eval $(libutils_impl_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libutilscallstack shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libutilscallstack
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CPPFLAGS = $(libutils_defaults_CXXFLAGS)
LOCAL_CFLAGS   = $(libutils_defaults_CFLAGS)
LOCAL_LDFLAGS  = $(libutils_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libutils_defaults_LDLIBS)
LOCAL_SRC_FILES_ =  \
        $(libutils_defaults_SRCS) \
        ProcessCallStack.cpp \
        CallStack.cpp
LOCAL_SRC_FILES = $(sort $(LOCAL_SRC_FILES_))
LOCAL_SHARED_LIBRARIES =  \
        libutils \
        libunwindstack \
        $(libutils_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libutils_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES = $(libutils_defaults_INCLUDE_DIRS)
$(eval $(libutils_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

include $(call first-makefiles-under,$(LOCAL_PATH))

LOCAL_PATH := $(call my-dir)
titrace-defaults_SHARED_LIBS = libbase
titrace-defaults_SRCS     =  \
        titrace.cc \
        instruction_decoder.cc
titrace-defaults_INCLUDE_DIRS = art/libdexfile

include art/build/Android.common_build.mk
# link libtitrace shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libtitrace
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(titrace-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(titrace-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(titrace-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(titrace-defaults_LDLIBS)
LOCAL_SRC_FILES = $(titrace-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(titrace-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(titrace-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(titrace-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(titrace-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libtitrace static library
include $(CLEAR_VARS)
LOCAL_MODULE := libtitrace
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(titrace-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(titrace-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(titrace-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(titrace-defaults_LDLIBS)
LOCAL_SRC_FILES = $(titrace-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(titrace-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(titrace-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(titrace-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(titrace-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libtitraced shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libtitraced
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(titrace-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(titrace-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(titrace-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(titrace-defaults_LDLIBS)
LOCAL_SRC_FILES = $(titrace-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(titrace-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(titrace-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(titrace-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(titrace-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libtitraced static library
include $(CLEAR_VARS)
LOCAL_MODULE := libtitraced
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(titrace-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(titrace-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(titrace-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(titrace-defaults_LDLIBS)
LOCAL_SRC_FILES = $(titrace-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(titrace-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(titrace-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(titrace-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(titrace-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


LOCAL_PATH := $(call my-dir)
jitload-defaults_SHARED_LIBS = libbase
jitload-defaults_SRCS     = jitload.cc

include art/build/Android.common_build.mk
# link libjitload shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libjitload
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(jitload-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(jitload-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(jitload-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(jitload-defaults_LDLIBS)
LOCAL_SRC_FILES = $(jitload-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libdexfile \
        libprofile \
        libartbase \
        $(jitload-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(jitload-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(jitload-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(jitload-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libjitload static library
include $(CLEAR_VARS)
LOCAL_MODULE := libjitload
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(jitload-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(jitload-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(jitload-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(jitload-defaults_LDLIBS)
LOCAL_SRC_FILES = $(jitload-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libdexfile \
        libprofile \
        libartbase \
        $(jitload-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(jitload-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(jitload-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(jitload-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libjitloadd shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libjitloadd
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(jitload-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(jitload-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(jitload-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(jitload-defaults_LDLIBS)
LOCAL_SRC_FILES = $(jitload-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libdexfiled \
        libprofiled \
        libartbased \
        $(jitload-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(jitload-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(jitload-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(jitload-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libjitloadd static library
include $(CLEAR_VARS)
LOCAL_MODULE := libjitloadd
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(jitload-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(jitload-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(jitload-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(jitload-defaults_LDLIBS)
LOCAL_SRC_FILES = $(jitload-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libdexfiled \
        libprofiled \
        libartbased \
        $(jitload-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(jitload-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(jitload-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(jitload-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


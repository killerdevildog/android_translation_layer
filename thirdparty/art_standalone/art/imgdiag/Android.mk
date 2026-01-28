LOCAL_PATH := $(call my-dir)
imgdiag-defaults_SHARED_LIBS =  \
        libziparchive \
        libbacktrace \
        libbase
imgdiag-defaults_SRCS     = imgdiag.cc

include art/build/Android.common_build.mk
# link imgdiag binary
include $(CLEAR_VARS)
LOCAL_MODULE := imgdiag
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(imgdiag-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(imgdiag-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(imgdiag-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(imgdiag-defaults_LDLIBS)
LOCAL_SRC_FILES = $(imgdiag-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libartbase \
        libart-compiler \
        $(imgdiag-defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(imgdiag-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(imgdiag-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)

include art/build/Android.common_build.mk
# link imgdiagd binary
include $(CLEAR_VARS)
LOCAL_MODULE := imgdiagd
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(imgdiag-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(imgdiag-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(imgdiag-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(imgdiag-defaults_LDLIBS)
LOCAL_SRC_FILES = $(imgdiag-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libartbased \
        libartd-compiler \
        $(imgdiag-defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(imgdiag-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(imgdiag-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)


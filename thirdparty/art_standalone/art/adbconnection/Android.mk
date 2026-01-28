LOCAL_PATH := $(call my-dir)
adbconnection-defaults_SHARED_LIBS = libbase
adbconnection-defaults_SRCS     = adbconnection.cc

include art/build/Android.common_build.mk
# link libadbconnection shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libadbconnection
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(adbconnection-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(adbconnection-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(adbconnection-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(adbconnection-defaults_LDLIBS)
LOCAL_SRC_FILES = $(adbconnection-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libartbase \
        $(adbconnection-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(adbconnection-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(adbconnection-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(adbconnection-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libadbconnection static library
include $(CLEAR_VARS)
LOCAL_MODULE := libadbconnection
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(adbconnection-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(adbconnection-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(adbconnection-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(adbconnection-defaults_LDLIBS)
LOCAL_SRC_FILES = $(adbconnection-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libartbase \
        $(adbconnection-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(adbconnection-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(adbconnection-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(adbconnection-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libadbconnectiond shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libadbconnectiond
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(adbconnection-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(adbconnection-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(adbconnection-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(adbconnection-defaults_LDLIBS)
LOCAL_SRC_FILES = $(adbconnection-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libartbased \
        $(adbconnection-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(adbconnection-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(adbconnection-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(adbconnection-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libadbconnectiond static library
include $(CLEAR_VARS)
LOCAL_MODULE := libadbconnectiond
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(adbconnection-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(adbconnection-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(adbconnection-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(adbconnection-defaults_LDLIBS)
LOCAL_SRC_FILES = $(adbconnection-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libartbased \
        $(adbconnection-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(adbconnection-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(adbconnection-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(adbconnection-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


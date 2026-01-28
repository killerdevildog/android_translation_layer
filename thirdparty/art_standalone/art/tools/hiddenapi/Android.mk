LOCAL_PATH := $(call my-dir)
hiddenapi-defaults_SHARED_LIBS = libbase
hiddenapi-defaults_SRCS     = hiddenapi.cc

include art/build/Android.common_build.mk
# link hiddenapi binary
include $(CLEAR_VARS)
LOCAL_MODULE := hiddenapi
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(hiddenapi-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(hiddenapi-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(hiddenapi-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(hiddenapi-defaults_LDLIBS)
LOCAL_SRC_FILES = $(hiddenapi-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libdexfile \
        libartbase \
        $(hiddenapi-defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(hiddenapi-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(hiddenapi-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)

include art/build/Android.common_build.mk
# link hiddenapid binary
include $(CLEAR_VARS)
LOCAL_MODULE := hiddenapid
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(hiddenapi-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(hiddenapi-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(hiddenapi-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(hiddenapi-defaults_LDLIBS)
LOCAL_SRC_FILES = $(hiddenapi-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libdexfiled \
        libartbased \
        $(hiddenapi-defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(hiddenapi-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(hiddenapi-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)


LOCAL_PATH := $(call my-dir)
libopenjdkjvm_defaults_SHARED_LIBS = libbase
libopenjdkjvm_defaults_SRCS     = OpenjdkJvm.cc

include art/build/Android.common_build.mk
# link libopenjdkjvm shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libopenjdkjvm
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libopenjdkjvm_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libopenjdkjvm_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libopenjdkjvm_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libopenjdkjvm_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libopenjdkjvm_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libartbase \
        $(libopenjdkjvm_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libopenjdkjvm_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libopenjdkjvm_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libopenjdkjvm_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libopenjdkjvm static library
include $(CLEAR_VARS)
LOCAL_MODULE := libopenjdkjvm
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libopenjdkjvm_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libopenjdkjvm_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libopenjdkjvm_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libopenjdkjvm_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libopenjdkjvm_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libartbase \
        $(libopenjdkjvm_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libopenjdkjvm_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libopenjdkjvm_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libopenjdkjvm_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libopenjdkjvmd shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libopenjdkjvmd
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libopenjdkjvm_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libopenjdkjvm_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libopenjdkjvm_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libopenjdkjvm_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libopenjdkjvm_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libartbased \
        $(libopenjdkjvm_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libopenjdkjvm_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libopenjdkjvm_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libopenjdkjvm_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libopenjdkjvmd static library
include $(CLEAR_VARS)
LOCAL_MODULE := libopenjdkjvmd
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libopenjdkjvm_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libopenjdkjvm_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libopenjdkjvm_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libopenjdkjvm_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libopenjdkjvm_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libartbased \
        $(libopenjdkjvm_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libopenjdkjvm_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libopenjdkjvm_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libopenjdkjvm_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


LOCAL_PATH := $(call my-dir)
forceredefine-defaults_LDLIBS   = -lz
forceredefine-defaults_SHARED_LIBS =  \
        libbase \
        liblog
forceredefine-defaults_SRCS     = forceredefine.cc
forceredefine-defaults_INCLUDE_DIRS =  \
        libnativehelper/include_jni \
        libnativehelper/header_only_include

include art/build/Android.common_build.mk
# link libforceredefine shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libforceredefine
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(forceredefine-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(forceredefine-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(forceredefine-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(forceredefine-defaults_LDLIBS)
LOCAL_SRC_FILES = $(forceredefine-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(forceredefine-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(forceredefine-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(forceredefine-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(forceredefine-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libforceredefine static library
include $(CLEAR_VARS)
LOCAL_MODULE := libforceredefine
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(forceredefine-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(forceredefine-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(forceredefine-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(forceredefine-defaults_LDLIBS)
LOCAL_SRC_FILES = $(forceredefine-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(forceredefine-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(forceredefine-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(forceredefine-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(forceredefine-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libforceredefined shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libforceredefined
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(forceredefine-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(forceredefine-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(forceredefine-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(forceredefine-defaults_LDLIBS)
LOCAL_SRC_FILES = $(forceredefine-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(forceredefine-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(forceredefine-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(forceredefine-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(forceredefine-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libforceredefined static library
include $(CLEAR_VARS)
LOCAL_MODULE := libforceredefined
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(forceredefine-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(forceredefine-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(forceredefine-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(forceredefine-defaults_LDLIBS)
LOCAL_SRC_FILES = $(forceredefine-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(forceredefine-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(forceredefine-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(forceredefine-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(forceredefine-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


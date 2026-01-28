LOCAL_PATH := $(call my-dir)
fieldnull-defaults_SHARED_LIBS = libbase
fieldnull-defaults_SRCS     = fieldnull.cc

include art/build/Android.common_build.mk
# link libfieldnull shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libfieldnull
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(fieldnull-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(fieldnull-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(fieldnull-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(fieldnull-defaults_LDLIBS)
LOCAL_SRC_FILES = $(fieldnull-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(fieldnull-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(fieldnull-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(fieldnull-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(fieldnull-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libfieldnull static library
include $(CLEAR_VARS)
LOCAL_MODULE := libfieldnull
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(fieldnull-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(fieldnull-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(fieldnull-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(fieldnull-defaults_LDLIBS)
LOCAL_SRC_FILES = $(fieldnull-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(fieldnull-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(fieldnull-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(fieldnull-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(fieldnull-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libfieldnulld shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libfieldnulld
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(fieldnull-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(fieldnull-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(fieldnull-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(fieldnull-defaults_LDLIBS)
LOCAL_SRC_FILES = $(fieldnull-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(fieldnull-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(fieldnull-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(fieldnull-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(fieldnull-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libfieldnulld static library
include $(CLEAR_VARS)
LOCAL_MODULE := libfieldnulld
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(fieldnull-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(fieldnull-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(fieldnull-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(fieldnull-defaults_LDLIBS)
LOCAL_SRC_FILES = $(fieldnull-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(fieldnull-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(fieldnull-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(fieldnull-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(fieldnull-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


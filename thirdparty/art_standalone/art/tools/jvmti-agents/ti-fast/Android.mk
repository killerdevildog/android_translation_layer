LOCAL_PATH := $(call my-dir)
tifast-defaults_SHARED_LIBS = libbase
tifast-defaults_SRCS     = tifast.cc

include art/build/Android.common_build.mk
# link libtifast shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libtifast
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(tifast-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(tifast-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(tifast-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(tifast-defaults_LDLIBS)
LOCAL_SRC_FILES = $(tifast-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(tifast-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(tifast-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(tifast-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(tifast-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libtifast static library
include $(CLEAR_VARS)
LOCAL_MODULE := libtifast
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(tifast-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(tifast-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(tifast-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(tifast-defaults_LDLIBS)
LOCAL_SRC_FILES = $(tifast-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(tifast-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(tifast-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(tifast-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(tifast-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libtifastd shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libtifastd
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(tifast-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(tifast-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(tifast-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(tifast-defaults_LDLIBS)
LOCAL_SRC_FILES = $(tifast-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(tifast-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(tifast-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(tifast-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(tifast-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libtifastd static library
include $(CLEAR_VARS)
LOCAL_MODULE := libtifastd
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(tifast-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(tifast-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(tifast-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(tifast-defaults_LDLIBS)
LOCAL_SRC_FILES = $(tifast-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(tifast-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(tifast-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(tifast-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(tifast-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


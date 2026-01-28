LOCAL_PATH := $(call my-dir)
wrapagentproperties-defaults_SHARED_LIBS = libbase
wrapagentproperties-defaults_SRCS     = wrapagentproperties.cc

include art/build/Android.common_build.mk
# link libwrapagentproperties shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libwrapagentproperties
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(wrapagentproperties-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(wrapagentproperties-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(wrapagentproperties-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(wrapagentproperties-defaults_LDLIBS)
LOCAL_SRC_FILES = $(wrapagentproperties-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(wrapagentproperties-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(wrapagentproperties-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(wrapagentproperties-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(wrapagentproperties-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libwrapagentproperties static library
include $(CLEAR_VARS)
LOCAL_MODULE := libwrapagentproperties
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(wrapagentproperties-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(wrapagentproperties-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(wrapagentproperties-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(wrapagentproperties-defaults_LDLIBS)
LOCAL_SRC_FILES = $(wrapagentproperties-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(wrapagentproperties-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(wrapagentproperties-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(wrapagentproperties-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(wrapagentproperties-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libwrapagentpropertiesd shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libwrapagentpropertiesd
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(wrapagentproperties-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(wrapagentproperties-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(wrapagentproperties-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(wrapagentproperties-defaults_LDLIBS)
LOCAL_SRC_FILES = $(wrapagentproperties-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(wrapagentproperties-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(wrapagentproperties-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(wrapagentproperties-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(wrapagentproperties-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libwrapagentpropertiesd static library
include $(CLEAR_VARS)
LOCAL_MODULE := libwrapagentpropertiesd
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(wrapagentproperties-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(wrapagentproperties-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(wrapagentproperties-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(wrapagentproperties-defaults_LDLIBS)
LOCAL_SRC_FILES = $(wrapagentproperties-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(wrapagentproperties-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(wrapagentproperties-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(wrapagentproperties-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(wrapagentproperties-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


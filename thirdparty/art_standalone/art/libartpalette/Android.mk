LOCAL_PATH := $(call my-dir)
libartpalette_defaults_EXPORT_INCLUDE_DIRS = $(LOCAL_PATH)/include

include art/build/Android.common_build.mk
# link libartpalette-system shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libartpalette-system
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libartpalette_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libartpalette_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libartpalette_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libartpalette_defaults_LDLIBS)
LOCAL_SRC_FILES =  \
        $(libartpalette_defaults_SRCS) \
        system/palette_fake.cc
LOCAL_SHARED_LIBRARIES =  \
        libbase \
        $(libartpalette_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libartpalette_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libartpalette_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libartpalette_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libartpalette-system static library
include $(CLEAR_VARS)
LOCAL_MODULE := libartpalette-system
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libartpalette_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libartpalette_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libartpalette_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libartpalette_defaults_LDLIBS)
LOCAL_SRC_FILES =  \
        $(libartpalette_defaults_SRCS) \
        system/palette_fake.cc
LOCAL_SHARED_LIBRARIES =  \
        libbase \
        $(libartpalette_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libartpalette_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libartpalette_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libartpalette_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libartpalette shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libartpalette
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libartpalette_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libartpalette_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libartpalette_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libartpalette_defaults_LDLIBS)
LOCAL_SRC_FILES =  \
        $(libartpalette_defaults_SRCS) \
        system/palette_fake.cc
LOCAL_SHARED_LIBRARIES =  \
        libbase \
        liblog \
        $(libartpalette_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libartpalette_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libartpalette_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libartpalette_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libartpalette static library
include $(CLEAR_VARS)
LOCAL_MODULE := libartpalette
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libartpalette_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libartpalette_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libartpalette_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libartpalette_defaults_LDLIBS)
LOCAL_SRC_FILES =  \
        $(libartpalette_defaults_SRCS) \
        system/palette_fake.cc
LOCAL_SHARED_LIBRARIES = $(libartpalette_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libartpalette_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libartpalette_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libartpalette_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


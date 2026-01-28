LOCAL_PATH := $(call my-dir)
libelffile-defaults_SHARED_LIBS =  \
        libartbase \
        libbase
libelffile-defaults_SRCS     =  \
        elf/xz_utils.cc \
        stream/buffered_output_stream.cc \
        stream/file_output_stream.cc \
        stream/output_stream.cc \
        stream/vector_output_stream.cc
libelffile-defaults_EXPORT_INCLUDE_DIRS = $(LOCAL_PATH)/.

include art/build/Android.common_build.mk
# link libelffile static library
include $(CLEAR_VARS)
LOCAL_MODULE := libelffile
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libelffile-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libelffile-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libelffile-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libelffile-defaults_LDLIBS)
LOCAL_SRC_FILES = $(libelffile-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(libelffile-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libelffile-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libelffile-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libelffile-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libelffiled static library
include $(CLEAR_VARS)
LOCAL_MODULE := libelffiled
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libelffile-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libelffile-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libelffile-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libelffile-defaults_LDLIBS)
LOCAL_SRC_FILES = $(libelffile-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(libelffile-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libelffile-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libelffile-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libelffile-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


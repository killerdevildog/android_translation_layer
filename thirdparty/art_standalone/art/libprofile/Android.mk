LOCAL_PATH := $(call my-dir)
libprofile_defaults_CFLAGS   = -DBUILDING_LIBART=1
libprofile_defaults_LDLIBS   = -lz
libprofile_defaults_SHARED_LIBS =  \
        libartbase \
        libartpalette \
        libdexfile \
        libziparchive \
        libbase
libprofile_defaults_SRCS     = profile/profile_compilation_info.cc
libprofile_defaults_EXPORT_INCLUDE_DIRS = $(LOCAL_PATH)/.

libprofile_static_base_defaults_LDLIBS   = -lz

libprofile_static_defaults_CXXFLAGS =  \
        $(libprofile_static_base_defaults_CXXFLAGS) \
        $(libartbase_static_defaults_CXXFLAGS) \
        $(libdexfile_static_defaults_CXXFLAGS)
libprofile_static_defaults_CFLAGS   =  \
        $(libprofile_static_base_defaults_CFLAGS) \
        $(libartbase_static_defaults_CFLAGS) \
        $(libdexfile_static_defaults_CFLAGS)
libprofile_static_defaults_LDFLAGS  =  \
        $(libprofile_static_base_defaults_LDFLAGS) \
        $(libartbase_static_defaults_LDFLAGS) \
        $(libdexfile_static_defaults_LDFLAGS)
libprofile_static_defaults_LDLIBS   =  \
        $(libprofile_static_base_defaults_LDLIBS) \
        $(libartbase_static_defaults_LDLIBS) \
        $(libdexfile_static_defaults_LDLIBS)
libprofile_static_defaults_SRCS     =  \
        $(libprofile_static_base_defaults_SRCS) \
        $(libartbase_static_defaults_SRCS) \
        $(libdexfile_static_defaults_SRCS)
libprofile_static_defaults_INCLUDE_DIRS =  \
        $(libprofile_static_base_defaults_INCLUDE_DIRS) \
        $(libartbase_static_defaults_INCLUDE_DIRS) \
        $(libdexfile_static_defaults_INCLUDE_DIRS)
libprofile_static_defaults_EXPORT_INCLUDE_DIRS =  \
        $(libprofile_static_base_defaults_EXPORT_INCLUDE_DIRS) \
        $(libartbase_static_defaults_EXPORT_INCLUDE_DIRS) \
        $(libdexfile_static_defaults_EXPORT_INCLUDE_DIRS)
libprofile_static_defaults_GENERATED_SOURCES =  \
        $(libprofile_static_base_defaults_GENERATED_SOURCES) \
        $(libartbase_static_defaults_GENERATED_SOURCES) \
        $(libdexfile_static_defaults_GENERATED_SOURCES)

libprofiled_static_defaults_CXXFLAGS =  \
        $(libprofile_static_base_defaults_CXXFLAGS) \
        $(libartbased_static_defaults_CXXFLAGS) \
        $(libdexfiled_static_defaults_CXXFLAGS)
libprofiled_static_defaults_CFLAGS   =  \
        $(libprofile_static_base_defaults_CFLAGS) \
        $(libartbased_static_defaults_CFLAGS) \
        $(libdexfiled_static_defaults_CFLAGS)
libprofiled_static_defaults_LDFLAGS  =  \
        $(libprofile_static_base_defaults_LDFLAGS) \
        $(libartbased_static_defaults_LDFLAGS) \
        $(libdexfiled_static_defaults_LDFLAGS)
libprofiled_static_defaults_LDLIBS   =  \
        $(libprofile_static_base_defaults_LDLIBS) \
        $(libartbased_static_defaults_LDLIBS) \
        $(libdexfiled_static_defaults_LDLIBS)
libprofiled_static_defaults_SRCS     =  \
        $(libprofile_static_base_defaults_SRCS) \
        $(libartbased_static_defaults_SRCS) \
        $(libdexfiled_static_defaults_SRCS)
libprofiled_static_defaults_INCLUDE_DIRS =  \
        $(libprofile_static_base_defaults_INCLUDE_DIRS) \
        $(libartbased_static_defaults_INCLUDE_DIRS) \
        $(libdexfiled_static_defaults_INCLUDE_DIRS)
libprofiled_static_defaults_EXPORT_INCLUDE_DIRS =  \
        $(libprofile_static_base_defaults_EXPORT_INCLUDE_DIRS) \
        $(libartbased_static_defaults_EXPORT_INCLUDE_DIRS) \
        $(libdexfiled_static_defaults_EXPORT_INCLUDE_DIRS)
libprofiled_static_defaults_GENERATED_SOURCES =  \
        $(libprofile_static_base_defaults_GENERATED_SOURCES) \
        $(libartbased_static_defaults_GENERATED_SOURCES) \
        $(libdexfiled_static_defaults_GENERATED_SOURCES)

include art/build/Android.common_build.mk
# link libprofile shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libprofile
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libprofile_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libprofile_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libprofile_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libprofile_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libprofile_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libbase \
        libziparchive \
        $(libprofile_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libprofile_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libprofile_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libprofile_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libprofile static library
include $(CLEAR_VARS)
LOCAL_MODULE := libprofile
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libprofile_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libprofile_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libprofile_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libprofile_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libprofile_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libbase \
        libziparchive \
        $(libprofile_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libprofile_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libprofile_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libprofile_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libprofiled shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libprofiled
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libprofile_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libprofile_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libprofile_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libprofile_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libprofile_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libbase \
        libziparchive \
        $(libprofile_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libprofile_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libprofile_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libprofile_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libprofiled static library
include $(CLEAR_VARS)
LOCAL_MODULE := libprofiled
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libprofile_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libprofile_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libprofile_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libprofile_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libprofile_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libbase \
        libziparchive \
        $(libprofile_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libprofile_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libprofile_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libprofile_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


LOCAL_PATH := $(call my-dir)
profman-defaults_SHARED_LIBS = libbase
profman-defaults_SRCS     =  \
        boot_image_profile.cc \
        profman.cc \
        profile_assistant.cc

include art/build/Android.common_build.mk
# link profman binary
include $(CLEAR_VARS)
LOCAL_MODULE := profman
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(profman-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(profman-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(profman-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(profman-defaults_LDLIBS)
LOCAL_SRC_FILES = $(profman-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libprofile \
        libdexfile \
        libartbase \
        $(profman-defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(profman-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(profman-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)

include art/build/Android.common_build.mk
# link profmand binary
include $(CLEAR_VARS)
LOCAL_MODULE := profmand
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(profman-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(profman-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(profman-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(profman-defaults_LDLIBS)
LOCAL_SRC_FILES = $(profman-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libprofiled \
        libdexfiled \
        libartbased \
        $(profman-defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(profman-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(profman-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)

include art/build/Android.common_build.mk
# link profmans binary
include $(CLEAR_VARS)
LOCAL_MODULE := profmans
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS =  \
        $(profman-defaults_CXXFLAGS) \
        $(libprofile_static_defaults_CXXFLAGS) \
        $(libdexfile_static_defaults_CXXFLAGS) \
        $(libartbase_static_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(profman-defaults_CFLAGS) \
        $(libprofile_static_defaults_CFLAGS) \
        $(libdexfile_static_defaults_CFLAGS) \
        $(libartbase_static_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  =  \
        $(profman-defaults_LDFLAGS) \
        $(libprofile_static_defaults_LDFLAGS) \
        $(libdexfile_static_defaults_LDFLAGS) \
        $(libartbase_static_defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(profman-defaults_LDLIBS) \
        $(libprofile_static_defaults_LDLIBS) \
        $(libdexfile_static_defaults_LDLIBS) \
        $(libartbase_static_defaults_LDLIBS)
LOCAL_SRC_FILES =  \
        $(profman-defaults_SRCS) \
        $(libprofile_static_defaults_SRCS) \
        $(libdexfile_static_defaults_SRCS) \
        $(libartbase_static_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        $(profman-defaults_SHARED_LIBS) \
        $(libprofile_static_defaults_SHARED_LIBS) \
        $(libdexfile_static_defaults_SHARED_LIBS) \
        $(libartbase_static_defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(profman-defaults_INCLUDE_DIRS) \
        $(libprofile_static_defaults_INCLUDE_DIRS) \
        $(libdexfile_static_defaults_INCLUDE_DIRS) \
        $(libartbase_static_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(profman-defaults_GENERATED_SOURCES))
$(eval $(libprofile_static_defaults_GENERATED_SOURCES))
$(eval $(libdexfile_static_defaults_GENERATED_SOURCES))
$(eval $(libartbase_static_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)


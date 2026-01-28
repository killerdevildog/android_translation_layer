LOCAL_PATH := $(call my-dir)
art_verifier-defaults_CXXFLAGS = $(libart_static_defaults_CXXFLAGS)
art_verifier-defaults_CFLAGS   = $(libart_static_defaults_CFLAGS)
art_verifier-defaults_LDFLAGS  = $(libart_static_defaults_LDFLAGS)
art_verifier-defaults_LDLIBS   = $(libart_static_defaults_LDLIBS)
art_verifier-defaults_SRCS     =  \
        $(libart_static_defaults_SRCS) \
        art_verifier.cc
art_verifier-defaults_INCLUDE_DIRS = $(libart_static_defaults_INCLUDE_DIRS)
art_verifier-defaults_EXPORT_INCLUDE_DIRS = $(libart_static_defaults_EXPORT_INCLUDE_DIRS)
art_verifier-defaults_GENERATED_SOURCES = $(libart_static_defaults_GENERATED_SOURCES)

include art/build/Android.common_build.mk
# link art_verifier binary
include $(CLEAR_VARS)
LOCAL_MODULE := art_verifier
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(art_verifier-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(art_verifier-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(art_verifier-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(art_verifier-defaults_LDLIBS)
LOCAL_SRC_FILES = $(art_verifier-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(art_verifier-defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(art_verifier-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(art_verifier-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)


LOCAL_PATH := $(call my-dir)
dexanalyze-defaults_SRCS     =  \
        dexanalyze.cc \
        dexanalyze_bytecode.cc \
        dexanalyze_experiments.cc \
        dexanalyze_strings.cc

include art/build/Android.common_build.mk
# link dexanalyze binary
include $(CLEAR_VARS)
LOCAL_MODULE := dexanalyze
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(dexanalyze-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dexanalyze-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(dexanalyze-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(dexanalyze-defaults_LDLIBS)
LOCAL_SRC_FILES = $(dexanalyze-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libdexfile \
        libartbase \
        libbase \
        $(dexanalyze-defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(dexanalyze-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dexanalyze-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)


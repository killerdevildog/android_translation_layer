LOCAL_PATH := $(call my-dir)
dexoptanalyzer-defaults_SHARED_LIBS = libbase
dexoptanalyzer-defaults_SRCS     = dexoptanalyzer.cc

include art/build/Android.common_build.mk
# link dexoptanalyzer binary
include $(CLEAR_VARS)
LOCAL_MODULE := dexoptanalyzer
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(dexoptanalyzer-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dexoptanalyzer-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(dexoptanalyzer-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(dexoptanalyzer-defaults_LDLIBS)
LOCAL_SRC_FILES = $(dexoptanalyzer-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libartbase \
        $(dexoptanalyzer-defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(dexoptanalyzer-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dexoptanalyzer-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)

include art/build/Android.common_build.mk
# link dexoptanalyzerd binary
include $(CLEAR_VARS)
LOCAL_MODULE := dexoptanalyzerd
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(dexoptanalyzer-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dexoptanalyzer-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(dexoptanalyzer-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(dexoptanalyzer-defaults_LDLIBS)
LOCAL_SRC_FILES = $(dexoptanalyzer-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libartbased \
        $(dexoptanalyzer-defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(dexoptanalyzer-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dexoptanalyzer-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)


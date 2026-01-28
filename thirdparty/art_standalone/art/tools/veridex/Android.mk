LOCAL_PATH := $(call my-dir)
# link veridex binary
include $(CLEAR_VARS)
LOCAL_MODULE := veridex
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   = -Wall
LOCAL_SRC_FILES =  \
        flow_analysis.cc \
        hidden_api.cc \
        hidden_api_finder.cc \
        precise_hidden_api_finder.cc \
        resolver.cc \
        veridex.cc
LOCAL_STATIC_LIBRARIES =  \
        libdexfile \
        libartbase \
        libbase \
        liblog \
        libz \
        libziparchive
include $(BUILD_HOST_EXECUTABLE)


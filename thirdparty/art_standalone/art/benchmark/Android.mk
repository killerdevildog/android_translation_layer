LOCAL_PATH := $(call my-dir)
include art/build/Android.common_build.mk
# link libartbenchmark shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libartbenchmark
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   =  \
        $(ART_HOST_CFLAGS) \
        -Wno-frame-larger-than=
LOCAL_SRC_FILES =  \
        jni_loader.cc \
        jobject-benchmark/jobject_benchmark.cc \
        jni-perf/perf_jni.cc \
        micro-native/micro_native.cc \
        scoped-primitive-array/scoped_primitive_array.cc
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libbacktrace \
        libbase \
        libnativehelper
LOCAL_C_INCLUDES = $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
include $(BUILD_HOST_SHARED_LIBRARY)

# link libartbenchmark static library
include $(CLEAR_VARS)
LOCAL_MODULE := libartbenchmark
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   =  \
        $(ART_HOST_CFLAGS) \
        -Wno-frame-larger-than=
LOCAL_SRC_FILES =  \
        jni_loader.cc \
        jobject-benchmark/jobject_benchmark.cc \
        jni-perf/perf_jni.cc \
        micro-native/micro_native.cc \
        scoped-primitive-array/scoped_primitive_array.cc
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libbacktrace \
        libbase \
        libnativehelper
LOCAL_C_INCLUDES = $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libartbenchmark-micronative-host shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libartbenchmark-micronative-host
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   =  \
        $(ART_HOST_CFLAGS) \
        -Wno-frame-larger-than=
LOCAL_SRC_FILES =  \
        jni_loader.cc \
        micro-native/micro_native.cc
LOCAL_C_INCLUDES = $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
include $(BUILD_HOST_SHARED_LIBRARY)

# link libartbenchmark-micronative-host static library
include $(CLEAR_VARS)
LOCAL_MODULE := libartbenchmark-micronative-host
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   =  \
        $(ART_HOST_CFLAGS) \
        -Wno-frame-larger-than=
LOCAL_SRC_FILES =  \
        jni_loader.cc \
        micro-native/micro_native.cc
LOCAL_C_INCLUDES = $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
include $(BUILD_HOST_STATIC_LIBRARY)


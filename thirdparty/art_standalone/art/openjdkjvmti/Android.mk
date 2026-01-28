LOCAL_PATH := $(call my-dir)
libopenjdkjvmti_defaults_SHARED_LIBS = libbase
libopenjdkjvmti_defaults_SRCS     =  \
        deopt_manager.cc \
        events.cc \
        fixed_up_dex_file.cc \
        object_tagging.cc \
        OpenjdkJvmTi.cc \
        ti_allocator.cc \
        ti_breakpoint.cc \
        ti_class.cc \
        ti_class_definition.cc \
        ti_class_loader.cc \
        ti_ddms.cc \
        ti_dump.cc \
        ti_extension.cc \
        ti_field.cc \
        ti_heap.cc \
        ti_jni.cc \
        ti_logging.cc \
        ti_method.cc \
        ti_monitor.cc \
        ti_object.cc \
        ti_phase.cc \
        ti_properties.cc \
        ti_search.cc \
        ti_stack.cc \
        ti_redefine.cc \
        ti_thread.cc \
        ti_threadgroup.cc \
        ti_timers.cc \
        transform.cc

include art/build/Android.common_build.mk
# link libopenjdkjvmti shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libopenjdkjvmti
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libopenjdkjvmti_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libopenjdkjvmti_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libopenjdkjvmti_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libopenjdkjvmti_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libopenjdkjvmti_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libart-compiler \
        libart-dexlayout \
        libdexfile \
        libartbase \
        $(libopenjdkjvmti_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libopenjdkjvmti_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libopenjdkjvmti_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libopenjdkjvmti_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libopenjdkjvmti static library
include $(CLEAR_VARS)
LOCAL_MODULE := libopenjdkjvmti
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libopenjdkjvmti_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libopenjdkjvmti_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libopenjdkjvmti_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libopenjdkjvmti_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libopenjdkjvmti_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libart-compiler \
        libart-dexlayout \
        libdexfile \
        libartbase \
        $(libopenjdkjvmti_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libopenjdkjvmti_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libopenjdkjvmti_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libopenjdkjvmti_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libopenjdkjvmtid shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libopenjdkjvmtid
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libopenjdkjvmti_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libopenjdkjvmti_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libopenjdkjvmti_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libopenjdkjvmti_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libopenjdkjvmti_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libartd-compiler \
        libartd-dexlayout \
        libdexfiled \
        libartbased \
        $(libopenjdkjvmti_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libopenjdkjvmti_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libopenjdkjvmti_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libopenjdkjvmti_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libopenjdkjvmtid static library
include $(CLEAR_VARS)
LOCAL_MODULE := libopenjdkjvmtid
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libopenjdkjvmti_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libopenjdkjvmti_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libopenjdkjvmti_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libopenjdkjvmti_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libopenjdkjvmti_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libartd-compiler \
        libartd-dexlayout \
        libdexfiled \
        libartbased \
        $(libopenjdkjvmti_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libopenjdkjvmti_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libopenjdkjvmti_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libopenjdkjvmti_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


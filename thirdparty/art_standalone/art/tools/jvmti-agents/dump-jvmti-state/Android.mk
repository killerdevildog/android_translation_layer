LOCAL_PATH := $(call my-dir)
dumpjvmti-defaults_SHARED_LIBS = libbase
dumpjvmti-defaults_SRCS     = dump-jvmti.cc

include art/build/Android.common_build.mk
# link libdumpjvmti shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libdumpjvmti
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(dumpjvmti-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dumpjvmti-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(dumpjvmti-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(dumpjvmti-defaults_LDLIBS)
LOCAL_SRC_FILES = $(dumpjvmti-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(dumpjvmti-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(dumpjvmti-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(dumpjvmti-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dumpjvmti-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libdumpjvmti static library
include $(CLEAR_VARS)
LOCAL_MODULE := libdumpjvmti
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(dumpjvmti-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dumpjvmti-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(dumpjvmti-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(dumpjvmti-defaults_LDLIBS)
LOCAL_SRC_FILES = $(dumpjvmti-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(dumpjvmti-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(dumpjvmti-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(dumpjvmti-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dumpjvmti-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libdumpjvmtid shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libdumpjvmtid
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(dumpjvmti-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dumpjvmti-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(dumpjvmti-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(dumpjvmti-defaults_LDLIBS)
LOCAL_SRC_FILES = $(dumpjvmti-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(dumpjvmti-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(dumpjvmti-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(dumpjvmti-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dumpjvmti-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libdumpjvmtid static library
include $(CLEAR_VARS)
LOCAL_MODULE := libdumpjvmtid
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(dumpjvmti-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dumpjvmti-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(dumpjvmti-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(dumpjvmti-defaults_LDLIBS)
LOCAL_SRC_FILES = $(dumpjvmti-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(dumpjvmti-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(dumpjvmti-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(dumpjvmti-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dumpjvmti-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


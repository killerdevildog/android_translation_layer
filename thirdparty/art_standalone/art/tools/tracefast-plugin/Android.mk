LOCAL_PATH := $(call my-dir)
tracefast-defaults_SHARED_LIBS = libbase
tracefast-defaults_SRCS     = tracefast.cc

tracefast-interpreter-defaults_CXXFLAGS = $(tracefast-defaults_CXXFLAGS)
tracefast-interpreter-defaults_CFLAGS   =  \
        $(tracefast-defaults_CFLAGS) \
        -DTRACEFAST_INTERPRETER=1
tracefast-interpreter-defaults_LDFLAGS  = $(tracefast-defaults_LDFLAGS)
tracefast-interpreter-defaults_LDLIBS   = $(tracefast-defaults_LDLIBS)
tracefast-interpreter-defaults_SRCS     = $(tracefast-defaults_SRCS)
tracefast-interpreter-defaults_INCLUDE_DIRS = $(tracefast-defaults_INCLUDE_DIRS)
tracefast-interpreter-defaults_EXPORT_INCLUDE_DIRS = $(tracefast-defaults_EXPORT_INCLUDE_DIRS)
tracefast-interpreter-defaults_GENERATED_SOURCES = $(tracefast-defaults_GENERATED_SOURCES)

tracefast-trampoline-defaults_CXXFLAGS = $(tracefast-defaults_CXXFLAGS)
tracefast-trampoline-defaults_CFLAGS   =  \
        $(tracefast-defaults_CFLAGS) \
        -DTRACEFAST_TRAMPOLINE=1
tracefast-trampoline-defaults_LDFLAGS  = $(tracefast-defaults_LDFLAGS)
tracefast-trampoline-defaults_LDLIBS   = $(tracefast-defaults_LDLIBS)
tracefast-trampoline-defaults_SRCS     = $(tracefast-defaults_SRCS)
tracefast-trampoline-defaults_INCLUDE_DIRS = $(tracefast-defaults_INCLUDE_DIRS)
tracefast-trampoline-defaults_EXPORT_INCLUDE_DIRS = $(tracefast-defaults_EXPORT_INCLUDE_DIRS)
tracefast-trampoline-defaults_GENERATED_SOURCES = $(tracefast-defaults_GENERATED_SOURCES)

include art/build/Android.common_build.mk
# link libtracefast-interpreter shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libtracefast-interpreter
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(tracefast-interpreter-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(tracefast-interpreter-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(tracefast-interpreter-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(tracefast-interpreter-defaults_LDLIBS)
LOCAL_SRC_FILES = $(tracefast-interpreter-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libartbase \
        $(tracefast-interpreter-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(tracefast-interpreter-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(tracefast-interpreter-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(tracefast-interpreter-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libtracefast-interpreter static library
include $(CLEAR_VARS)
LOCAL_MODULE := libtracefast-interpreter
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(tracefast-interpreter-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(tracefast-interpreter-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(tracefast-interpreter-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(tracefast-interpreter-defaults_LDLIBS)
LOCAL_SRC_FILES = $(tracefast-interpreter-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libartbase \
        $(tracefast-interpreter-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(tracefast-interpreter-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(tracefast-interpreter-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(tracefast-interpreter-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libtracefast-interpreterd shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libtracefast-interpreterd
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(tracefast-interpreter-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(tracefast-interpreter-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(tracefast-interpreter-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(tracefast-interpreter-defaults_LDLIBS)
LOCAL_SRC_FILES = $(tracefast-interpreter-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libartbased \
        $(tracefast-interpreter-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(tracefast-interpreter-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(tracefast-interpreter-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(tracefast-interpreter-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libtracefast-interpreterd static library
include $(CLEAR_VARS)
LOCAL_MODULE := libtracefast-interpreterd
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(tracefast-interpreter-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(tracefast-interpreter-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(tracefast-interpreter-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(tracefast-interpreter-defaults_LDLIBS)
LOCAL_SRC_FILES = $(tracefast-interpreter-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libartbased \
        $(tracefast-interpreter-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(tracefast-interpreter-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(tracefast-interpreter-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(tracefast-interpreter-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libtracefast-trampoline shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libtracefast-trampoline
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(tracefast-trampoline-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(tracefast-trampoline-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(tracefast-trampoline-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(tracefast-trampoline-defaults_LDLIBS)
LOCAL_SRC_FILES = $(tracefast-trampoline-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libartbase \
        $(tracefast-trampoline-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(tracefast-trampoline-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(tracefast-trampoline-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(tracefast-trampoline-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libtracefast-trampoline static library
include $(CLEAR_VARS)
LOCAL_MODULE := libtracefast-trampoline
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(tracefast-trampoline-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(tracefast-trampoline-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(tracefast-trampoline-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(tracefast-trampoline-defaults_LDLIBS)
LOCAL_SRC_FILES = $(tracefast-trampoline-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libartbase \
        $(tracefast-trampoline-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(tracefast-trampoline-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(tracefast-trampoline-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(tracefast-trampoline-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libtracefast-trampolined shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libtracefast-trampolined
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(tracefast-trampoline-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(tracefast-trampoline-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(tracefast-trampoline-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(tracefast-trampoline-defaults_LDLIBS)
LOCAL_SRC_FILES = $(tracefast-trampoline-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libartbased \
        $(tracefast-trampoline-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(tracefast-trampoline-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(tracefast-trampoline-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(tracefast-trampoline-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libtracefast-trampolined static library
include $(CLEAR_VARS)
LOCAL_MODULE := libtracefast-trampolined
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(tracefast-trampoline-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(tracefast-trampoline-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(tracefast-trampoline-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(tracefast-trampoline-defaults_LDLIBS)
LOCAL_SRC_FILES = $(tracefast-trampoline-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libartbased \
        $(tracefast-trampoline-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(tracefast-trampoline-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(tracefast-trampoline-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(tracefast-trampoline-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


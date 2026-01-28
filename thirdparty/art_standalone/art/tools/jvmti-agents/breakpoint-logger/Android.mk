LOCAL_PATH := $(call my-dir)
breakpointlogger-defaults_SHARED_LIBS = libbase
breakpointlogger-defaults_SRCS     = breakpoint_logger.cc

include art/build/Android.common_build.mk
# link libbreakpointlogger shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libbreakpointlogger
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(breakpointlogger-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(breakpointlogger-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(breakpointlogger-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(breakpointlogger-defaults_LDLIBS)
LOCAL_SRC_FILES = $(breakpointlogger-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(breakpointlogger-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(breakpointlogger-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(breakpointlogger-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(breakpointlogger-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libbreakpointlogger static library
include $(CLEAR_VARS)
LOCAL_MODULE := libbreakpointlogger
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(breakpointlogger-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(breakpointlogger-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(breakpointlogger-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(breakpointlogger-defaults_LDLIBS)
LOCAL_SRC_FILES = $(breakpointlogger-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(breakpointlogger-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(breakpointlogger-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(breakpointlogger-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(breakpointlogger-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libbreakpointloggerd shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libbreakpointloggerd
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(breakpointlogger-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(breakpointlogger-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(breakpointlogger-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(breakpointlogger-defaults_LDLIBS)
LOCAL_SRC_FILES = $(breakpointlogger-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(breakpointlogger-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(breakpointlogger-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(breakpointlogger-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(breakpointlogger-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libbreakpointloggerd static library
include $(CLEAR_VARS)
LOCAL_MODULE := libbreakpointloggerd
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(breakpointlogger-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(breakpointlogger-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(breakpointlogger-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(breakpointlogger-defaults_LDLIBS)
LOCAL_SRC_FILES = $(breakpointlogger-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(breakpointlogger-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(breakpointlogger-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(breakpointlogger-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(breakpointlogger-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


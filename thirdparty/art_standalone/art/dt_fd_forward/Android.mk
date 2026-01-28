LOCAL_PATH := $(call my-dir)
dt_fd_forward-defaults_SHARED_LIBS = libbase
dt_fd_forward-defaults_SRCS     = dt_fd_forward.cc

include art/build/Android.common_build.mk
# link libdt_fd_forward shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libdt_fd_forward
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(dt_fd_forward-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dt_fd_forward-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(dt_fd_forward-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(dt_fd_forward-defaults_LDLIBS)
LOCAL_SRC_FILES = $(dt_fd_forward-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(dt_fd_forward-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(dt_fd_forward-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(dt_fd_forward-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dt_fd_forward-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libdt_fd_forward static library
include $(CLEAR_VARS)
LOCAL_MODULE := libdt_fd_forward
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(dt_fd_forward-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dt_fd_forward-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(dt_fd_forward-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(dt_fd_forward-defaults_LDLIBS)
LOCAL_SRC_FILES = $(dt_fd_forward-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(dt_fd_forward-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(dt_fd_forward-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(dt_fd_forward-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dt_fd_forward-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libdt_fd_forwardd shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libdt_fd_forwardd
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(dt_fd_forward-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dt_fd_forward-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(dt_fd_forward-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(dt_fd_forward-defaults_LDLIBS)
LOCAL_SRC_FILES = $(dt_fd_forward-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(dt_fd_forward-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(dt_fd_forward-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(dt_fd_forward-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dt_fd_forward-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libdt_fd_forwardd static library
include $(CLEAR_VARS)
LOCAL_MODULE := libdt_fd_forwardd
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(dt_fd_forward-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dt_fd_forward-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(dt_fd_forward-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(dt_fd_forward-defaults_LDLIBS)
LOCAL_SRC_FILES = $(dt_fd_forward-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(dt_fd_forward-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(dt_fd_forward-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(dt_fd_forward-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dt_fd_forward-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


LOCAL_PATH := $(call my-dir)
libart_simulator_defaults_CFLAGS   = -DVIXL_INCLUDE_SIMULATOR_AARCH64
libart_simulator_defaults_SHARED_LIBS =  \
        libbase \
        liblog
libart_simulator_defaults_SRCS     =  \
        code_simulator.cc \
        code_simulator_arm64.cc

include art/build/Android.common_build.mk
# link libart-simulator shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libart-simulator
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart_simulator_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart_simulator_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart_simulator_defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(libart_simulator_defaults_LDLIBS) \
        -lvixl
LOCAL_SRC_FILES = $(libart_simulator_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libartbase \
        $(libart_simulator_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart_simulator_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart_simulator_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart_simulator_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libart-simulator static library
include $(CLEAR_VARS)
LOCAL_MODULE := libart-simulator
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart_simulator_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart_simulator_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart_simulator_defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(libart_simulator_defaults_LDLIBS) \
        -lvixl
LOCAL_SRC_FILES = $(libart_simulator_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libartbase \
        $(libart_simulator_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart_simulator_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart_simulator_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart_simulator_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libartd-simulator shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libartd-simulator
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart_simulator_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart_simulator_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart_simulator_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libart_simulator_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libart_simulator_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libartbased \
        libvixld \
        $(libart_simulator_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart_simulator_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart_simulator_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart_simulator_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libartd-simulator static library
include $(CLEAR_VARS)
LOCAL_MODULE := libartd-simulator
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart_simulator_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart_simulator_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart_simulator_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libart_simulator_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libart_simulator_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libartbased \
        libvixld \
        $(libart_simulator_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart_simulator_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart_simulator_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart_simulator_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

libart_simulator_container_defaults_SHARED_LIBS = libbase
libart_simulator_container_defaults_SRCS     = code_simulator_container.cc
libart_simulator_container_defaults_EXPORT_INCLUDE_DIRS = $(LOCAL_PATH)/.

include art/build/Android.common_build.mk
# link libart-simulator-container shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libart-simulator-container
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart_simulator_container_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart_simulator_container_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart_simulator_container_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libart_simulator_container_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libart_simulator_container_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartbase \
        libart \
        $(libart_simulator_container_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart_simulator_container_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart_simulator_container_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart_simulator_container_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libart-simulator-container static library
include $(CLEAR_VARS)
LOCAL_MODULE := libart-simulator-container
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart_simulator_container_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart_simulator_container_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart_simulator_container_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libart_simulator_container_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libart_simulator_container_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartbase \
        libart \
        $(libart_simulator_container_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart_simulator_container_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart_simulator_container_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart_simulator_container_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libartd-simulator-container shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libartd-simulator-container
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart_simulator_container_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart_simulator_container_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart_simulator_container_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libart_simulator_container_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libart_simulator_container_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartbased \
        libartd \
        $(libart_simulator_container_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart_simulator_container_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart_simulator_container_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart_simulator_container_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libartd-simulator-container static library
include $(CLEAR_VARS)
LOCAL_MODULE := libartd-simulator-container
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart_simulator_container_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart_simulator_container_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart_simulator_container_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libart_simulator_container_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libart_simulator_container_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartbased \
        libartd \
        $(libart_simulator_container_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart_simulator_container_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart_simulator_container_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart_simulator_container_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


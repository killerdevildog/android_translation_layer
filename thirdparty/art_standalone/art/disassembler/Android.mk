LOCAL_PATH := $(call my-dir)
libart-disassembler-defaults_arm_CODEGEN_SRCS     = disassembler_arm.cc
libart-disassembler-defaults_arm64_CODEGEN_SRCS     =  \
        disassembler_arm64.cc \
        $(libart-disassembler-defaults_arm_CODEGEN_SRCS)
libart-disassembler-defaults_LDLIBS   = $(libart-disassembler-defaults_$(HOST_ARCH)_CODEGEN_HOST_LDLIBS)
libart-disassembler-defaults_SHARED_LIBS = libbase
libart-disassembler-defaults_SRCS     =  \
        $(libart-disassembler-defaults_$(HOST_ARCH)_CODEGEN_SRCS) \
        disassembler.cc \
        disassembler_mips.cc \
        disassembler_x86.cc
libart-disassembler-defaults_INCLUDE_DIRS = art/runtime
libart-disassembler-defaults_EXPORT_INCLUDE_DIRS = $(LOCAL_PATH)/.

include art/build/Android.common_build.mk
# link libart-disassembler shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libart-disassembler
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart-disassembler-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart-disassembler-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart-disassembler-defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(libart-disassembler-defaults_LDLIBS) \
        -lvixl
LOCAL_SRC_FILES = $(libart-disassembler-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(libart-disassembler-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart-disassembler-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart-disassembler-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart-disassembler-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libart-disassembler static library
include $(CLEAR_VARS)
LOCAL_MODULE := libart-disassembler
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart-disassembler-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart-disassembler-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart-disassembler-defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(libart-disassembler-defaults_LDLIBS) \
        -lvixl
LOCAL_SRC_FILES = $(libart-disassembler-defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(libart-disassembler-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart-disassembler-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart-disassembler-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart-disassembler-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libartd-disassembler shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libartd-disassembler
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart-disassembler-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart-disassembler-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart-disassembler-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libart-disassembler-defaults_LDLIBS)
LOCAL_SRC_FILES = $(libart-disassembler-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libvixld \
        $(libart-disassembler-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart-disassembler-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart-disassembler-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart-disassembler-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libartd-disassembler static library
include $(CLEAR_VARS)
LOCAL_MODULE := libartd-disassembler
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart-disassembler-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart-disassembler-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart-disassembler-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libart-disassembler-defaults_LDLIBS)
LOCAL_SRC_FILES = $(libart-disassembler-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libvixld \
        $(libart-disassembler-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart-disassembler-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart-disassembler-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart-disassembler-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


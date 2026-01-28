LOCAL_PATH := $(call my-dir)
oatdump-defaults_SRCS     = oatdump.cc

include art/build/Android.common_build.mk
# link oatdump binary
include $(CLEAR_VARS)
LOCAL_MODULE := oatdump
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(oatdump-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(oatdump-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(oatdump-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(oatdump-defaults_LDLIBS)
LOCAL_SRC_FILES = $(oatdump-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libart-compiler \
        libart-dexlayout \
        libart-disassembler \
        libdexfile \
        libartbase \
        libprofile \
        libbase \
        $(oatdump-defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(oatdump-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(oatdump-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)

include art/build/Android.common_build.mk
# link oatdumpd binary
include $(CLEAR_VARS)
LOCAL_MODULE := oatdumpd
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(oatdump-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(oatdump-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(oatdump-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(oatdump-defaults_LDLIBS)
LOCAL_SRC_FILES = $(oatdump-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd \
        libartd-compiler \
        libartd-dexlayout \
        libartd-disassembler \
        libdexfiled \
        libartbased \
        libprofiled \
        libbase \
        $(oatdump-defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(oatdump-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(oatdump-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)

oatdumps-defaults_CXXFLAGS = $(oatdump-defaults_CXXFLAGS)
oatdumps-defaults_CFLAGS   = $(oatdump-defaults_CFLAGS)
oatdumps-defaults_LDFLAGS  =  \
        $(oatdump-defaults_LDFLAGS) \
        -z muldefs
oatdumps-defaults_LDLIBS   = $(oatdump-defaults_LDLIBS)
oatdumps-defaults_SRCS     = $(oatdump-defaults_SRCS)
oatdumps-defaults_INCLUDE_DIRS = $(oatdump-defaults_INCLUDE_DIRS)
oatdumps-defaults_EXPORT_INCLUDE_DIRS = $(oatdump-defaults_EXPORT_INCLUDE_DIRS)
oatdumps-defaults_GENERATED_SOURCES = $(oatdump-defaults_GENERATED_SOURCES)

include art/build/Android.common_build.mk
# link oatdumps binary
include $(CLEAR_VARS)
LOCAL_MODULE := oatdumps
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS =  \
        $(libartbase_static_defaults_CXXFLAGS) \
        $(libdexfile_static_defaults_CXXFLAGS) \
        $(libprofile_static_defaults_CXXFLAGS) \
        $(libart-compiler_static_defaults_CXXFLAGS) \
        $(libart-dexlayout_static_defaults_CXXFLAGS) \
        $(oatdumps-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libartbase_static_defaults_CFLAGS) \
        $(libdexfile_static_defaults_CFLAGS) \
        $(libprofile_static_defaults_CFLAGS) \
        $(libart-compiler_static_defaults_CFLAGS) \
        $(libart-dexlayout_static_defaults_CFLAGS) \
        $(oatdumps-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  =  \
        $(libartbase_static_defaults_LDFLAGS) \
        $(libdexfile_static_defaults_LDFLAGS) \
        $(libprofile_static_defaults_LDFLAGS) \
        $(libart-compiler_static_defaults_LDFLAGS) \
        $(libart-dexlayout_static_defaults_LDFLAGS) \
        $(oatdumps-defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(libartbase_static_defaults_LDLIBS) \
        $(libdexfile_static_defaults_LDLIBS) \
        $(libprofile_static_defaults_LDLIBS) \
        $(libart-compiler_static_defaults_LDLIBS) \
        $(libart-dexlayout_static_defaults_LDLIBS) \
        $(oatdumps-defaults_LDLIBS)
LOCAL_SRC_FILES =  \
        $(libartbase_static_defaults_SRCS) \
        $(libdexfile_static_defaults_SRCS) \
        $(libprofile_static_defaults_SRCS) \
        $(libart-compiler_static_defaults_SRCS) \
        $(libart-dexlayout_static_defaults_SRCS) \
        $(oatdumps-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        $(libartbase_static_defaults_SHARED_LIBS) \
        $(libdexfile_static_defaults_SHARED_LIBS) \
        $(libprofile_static_defaults_SHARED_LIBS) \
        $(libart-compiler_static_defaults_SHARED_LIBS) \
        $(libart-dexlayout_static_defaults_SHARED_LIBS) \
        $(oatdumps-defaults_SHARED_LIBS)
LOCAL_STATIC_LIBRARIES =  \
        libart-disassembler \
        libvixl
LOCAL_C_INCLUDES =  \
        $(libartbase_static_defaults_INCLUDE_DIRS) \
        $(libdexfile_static_defaults_INCLUDE_DIRS) \
        $(libprofile_static_defaults_INCLUDE_DIRS) \
        $(libart-compiler_static_defaults_INCLUDE_DIRS) \
        $(libart-dexlayout_static_defaults_INCLUDE_DIRS) \
        $(oatdumps-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libartbase_static_defaults_GENERATED_SOURCES))
$(eval $(libdexfile_static_defaults_GENERATED_SOURCES))
$(eval $(libprofile_static_defaults_GENERATED_SOURCES))
$(eval $(libart-compiler_static_defaults_GENERATED_SOURCES))
$(eval $(libart-dexlayout_static_defaults_GENERATED_SOURCES))
$(eval $(oatdumps-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)

include art/build/Android.common_build.mk
# link oatdumpds binary
include $(CLEAR_VARS)
LOCAL_MODULE := oatdumpds
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS =  \
        $(libartbased_static_defaults_CXXFLAGS) \
        $(libdexfiled_static_defaults_CXXFLAGS) \
        $(libprofiled_static_defaults_CXXFLAGS) \
        $(libartd-compiler_static_defaults_CXXFLAGS) \
        $(libartd-dexlayout_static_defaults_CXXFLAGS) \
        $(oatdumps-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libartbased_static_defaults_CFLAGS) \
        $(libdexfiled_static_defaults_CFLAGS) \
        $(libprofiled_static_defaults_CFLAGS) \
        $(libartd-compiler_static_defaults_CFLAGS) \
        $(libartd-dexlayout_static_defaults_CFLAGS) \
        $(oatdumps-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  =  \
        $(libartbased_static_defaults_LDFLAGS) \
        $(libdexfiled_static_defaults_LDFLAGS) \
        $(libprofiled_static_defaults_LDFLAGS) \
        $(libartd-compiler_static_defaults_LDFLAGS) \
        $(libartd-dexlayout_static_defaults_LDFLAGS) \
        $(oatdumps-defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(libartbased_static_defaults_LDLIBS) \
        $(libdexfiled_static_defaults_LDLIBS) \
        $(libprofiled_static_defaults_LDLIBS) \
        $(libartd-compiler_static_defaults_LDLIBS) \
        $(libartd-dexlayout_static_defaults_LDLIBS) \
        $(oatdumps-defaults_LDLIBS)
LOCAL_SRC_FILES =  \
        $(libartbased_static_defaults_SRCS) \
        $(libdexfiled_static_defaults_SRCS) \
        $(libprofiled_static_defaults_SRCS) \
        $(libartd-compiler_static_defaults_SRCS) \
        $(libartd-dexlayout_static_defaults_SRCS) \
        $(oatdumps-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        $(libartbased_static_defaults_SHARED_LIBS) \
        $(libdexfiled_static_defaults_SHARED_LIBS) \
        $(libprofiled_static_defaults_SHARED_LIBS) \
        $(libartd-compiler_static_defaults_SHARED_LIBS) \
        $(libartd-dexlayout_static_defaults_SHARED_LIBS) \
        $(oatdumps-defaults_SHARED_LIBS)
LOCAL_STATIC_LIBRARIES =  \
        libartd-disassembler \
        libvixld
LOCAL_C_INCLUDES =  \
        $(libartbased_static_defaults_INCLUDE_DIRS) \
        $(libdexfiled_static_defaults_INCLUDE_DIRS) \
        $(libprofiled_static_defaults_INCLUDE_DIRS) \
        $(libartd-compiler_static_defaults_INCLUDE_DIRS) \
        $(libartd-dexlayout_static_defaults_INCLUDE_DIRS) \
        $(oatdumps-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libartbased_static_defaults_GENERATED_SOURCES))
$(eval $(libdexfiled_static_defaults_GENERATED_SOURCES))
$(eval $(libprofiled_static_defaults_GENERATED_SOURCES))
$(eval $(libartd-compiler_static_defaults_GENERATED_SOURCES))
$(eval $(libartd-dexlayout_static_defaults_GENERATED_SOURCES))
$(eval $(oatdumps-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)


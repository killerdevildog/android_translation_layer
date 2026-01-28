LOCAL_PATH := $(call my-dir)
libart-dexlayout-defaults_LDLIBS   = -lz
libart-dexlayout-defaults_SHARED_LIBS =  \
        libartbase \
        libartpalette \
        libdexfile \
        libprofile \
        libbase
libart-dexlayout-defaults_SRCS     =  \
        compact_dex_writer.cc \
        dexlayout.cc \
        dex_ir.cc \
        dex_ir_builder.cc \
        dex_verify.cc \
        dex_visualize.cc \
        dex_writer.cc
libart-dexlayout-defaults_EXPORT_INCLUDE_DIRS = $(LOCAL_PATH)/.

libart-dexlayout_static_base_defaults_LDLIBS   = -lz

include art/build/Android.common_build.mk
# link libart-dexlayout shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libart-dexlayout
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS =  \
        $(libart-dexlayout-defaults_CXXFLAGS) \
        $(dex2oat-pgo-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart-dexlayout-defaults_CFLAGS) \
        $(dex2oat-pgo-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  =  \
        $(libart-dexlayout-defaults_LDFLAGS) \
        $(dex2oat-pgo-defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(libart-dexlayout-defaults_LDLIBS) \
        $(dex2oat-pgo-defaults_LDLIBS)
LOCAL_SRC_FILES =  \
        $(libart-dexlayout-defaults_SRCS) \
        $(dex2oat-pgo-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        $(libart-dexlayout-defaults_SHARED_LIBS) \
        $(dex2oat-pgo-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS =  \
        $(libart-dexlayout-defaults_EXPORT_INCLUDE_DIRS) \
        $(dex2oat-pgo-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart-dexlayout-defaults_INCLUDE_DIRS) \
        $(dex2oat-pgo-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart-dexlayout-defaults_GENERATED_SOURCES))
$(eval $(dex2oat-pgo-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libart-dexlayout static library
include $(CLEAR_VARS)
LOCAL_MODULE := libart-dexlayout
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS =  \
        $(libart-dexlayout-defaults_CXXFLAGS) \
        $(dex2oat-pgo-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart-dexlayout-defaults_CFLAGS) \
        $(dex2oat-pgo-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  =  \
        $(libart-dexlayout-defaults_LDFLAGS) \
        $(dex2oat-pgo-defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(libart-dexlayout-defaults_LDLIBS) \
        $(dex2oat-pgo-defaults_LDLIBS)
LOCAL_SRC_FILES =  \
        $(libart-dexlayout-defaults_SRCS) \
        $(dex2oat-pgo-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        $(libart-dexlayout-defaults_SHARED_LIBS) \
        $(dex2oat-pgo-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS =  \
        $(libart-dexlayout-defaults_EXPORT_INCLUDE_DIRS) \
        $(dex2oat-pgo-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart-dexlayout-defaults_INCLUDE_DIRS) \
        $(dex2oat-pgo-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart-dexlayout-defaults_GENERATED_SOURCES))
$(eval $(dex2oat-pgo-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

libart-dexlayout_static_defaults_CXXFLAGS = $(libart-dexlayout_static_base_defaults_CXXFLAGS)
libart-dexlayout_static_defaults_CFLAGS   = $(libart-dexlayout_static_base_defaults_CFLAGS)
libart-dexlayout_static_defaults_LDFLAGS  = $(libart-dexlayout_static_base_defaults_LDFLAGS)
libart-dexlayout_static_defaults_LDLIBS   = $(libart-dexlayout_static_base_defaults_LDLIBS)
libart-dexlayout_static_defaults_SRCS     = $(libart-dexlayout_static_base_defaults_SRCS)
libart-dexlayout_static_defaults_INCLUDE_DIRS = $(libart-dexlayout_static_base_defaults_INCLUDE_DIRS)
libart-dexlayout_static_defaults_EXPORT_INCLUDE_DIRS = $(libart-dexlayout_static_base_defaults_EXPORT_INCLUDE_DIRS)
libart-dexlayout_static_defaults_GENERATED_SOURCES = $(libart-dexlayout_static_base_defaults_GENERATED_SOURCES)

include art/build/Android.common_build.mk
# link libartd-dexlayout shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libartd-dexlayout
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart-dexlayout-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart-dexlayout-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart-dexlayout-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libart-dexlayout-defaults_LDLIBS)
LOCAL_SRC_FILES = $(libart-dexlayout-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libdexfiled \
        libartbased \
        libprofiled \
        $(libart-dexlayout-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart-dexlayout-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart-dexlayout-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart-dexlayout-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libartd-dexlayout static library
include $(CLEAR_VARS)
LOCAL_MODULE := libartd-dexlayout
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart-dexlayout-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart-dexlayout-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart-dexlayout-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libart-dexlayout-defaults_LDLIBS)
LOCAL_SRC_FILES = $(libart-dexlayout-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libdexfiled \
        libartbased \
        libprofiled \
        $(libart-dexlayout-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart-dexlayout-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart-dexlayout-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart-dexlayout-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

libartd-dexlayout_static_defaults_CXXFLAGS = $(libart-dexlayout_static_base_defaults_CXXFLAGS)
libartd-dexlayout_static_defaults_CFLAGS   = $(libart-dexlayout_static_base_defaults_CFLAGS)
libartd-dexlayout_static_defaults_LDFLAGS  = $(libart-dexlayout_static_base_defaults_LDFLAGS)
libartd-dexlayout_static_defaults_LDLIBS   = $(libart-dexlayout_static_base_defaults_LDLIBS)
libartd-dexlayout_static_defaults_SRCS     = $(libart-dexlayout_static_base_defaults_SRCS)
libartd-dexlayout_static_defaults_INCLUDE_DIRS = $(libart-dexlayout_static_base_defaults_INCLUDE_DIRS)
libartd-dexlayout_static_defaults_EXPORT_INCLUDE_DIRS = $(libart-dexlayout_static_base_defaults_EXPORT_INCLUDE_DIRS)
libartd-dexlayout_static_defaults_GENERATED_SOURCES = $(libart-dexlayout_static_base_defaults_GENERATED_SOURCES)

dexlayout-defaults_SHARED_LIBS = libbase

include art/build/Android.common_build.mk
# link dexlayout binary
include $(CLEAR_VARS)
LOCAL_MODULE := dexlayout
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(dexlayout-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dexlayout-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(dexlayout-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(dexlayout-defaults_LDLIBS)
LOCAL_SRC_FILES =  \
        $(dexlayout-defaults_SRCS) \
        dexlayout_main.cc
LOCAL_SHARED_LIBRARIES =  \
        libdexfile \
        libprofile \
        libartbase \
        libart-dexlayout \
        $(dexlayout-defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(dexlayout-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dexlayout-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)

include art/build/Android.common_build.mk
# link dexlayouts binary
include $(CLEAR_VARS)
LOCAL_MODULE := dexlayouts
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS =  \
        $(dexlayout-defaults_CXXFLAGS) \
        $(libart-dexlayout_static_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dexlayout-defaults_CFLAGS) \
        $(libart-dexlayout_static_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  =  \
        $(dexlayout-defaults_LDFLAGS) \
        $(libart-dexlayout_static_defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(dexlayout-defaults_LDLIBS) \
        $(libart-dexlayout_static_defaults_LDLIBS)
LOCAL_SRC_FILES =  \
        $(dexlayout-defaults_SRCS) \
        $(libart-dexlayout_static_defaults_SRCS) \
        dexlayout_main.cc
LOCAL_SHARED_LIBRARIES =  \
        $(dexlayout-defaults_SHARED_LIBS) \
        $(libart-dexlayout_static_defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(dexlayout-defaults_INCLUDE_DIRS) \
        $(libart-dexlayout_static_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dexlayout-defaults_GENERATED_SOURCES))
$(eval $(libart-dexlayout_static_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)

include art/build/Android.common_build.mk
# link dexlayoutd binary
include $(CLEAR_VARS)
LOCAL_MODULE := dexlayoutd
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(dexlayout-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dexlayout-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(dexlayout-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(dexlayout-defaults_LDLIBS)
LOCAL_SRC_FILES =  \
        $(dexlayout-defaults_SRCS) \
        dexlayout_main.cc
LOCAL_SHARED_LIBRARIES =  \
        libdexfiled \
        libprofiled \
        libartbased \
        libartd-dexlayout \
        $(dexlayout-defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(dexlayout-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dexlayout-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)

include art/build/Android.common_build.mk
# link dexdiag binary
include $(CLEAR_VARS)
LOCAL_MODULE := dexdiag
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   =  \
        $(ART_HOST_CFLAGS) \
        -Wall
LOCAL_SRC_FILES = dexdiag.cc
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libdexfile \
        libartbase \
        libart-dexlayout \
        libbase
LOCAL_C_INCLUDES = $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
include $(BUILD_HOST_EXECUTABLE)


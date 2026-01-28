LOCAL_PATH := $(call my-dir)
dexfile_operator_srcs_SRCS     =  \
        dex/dex_file.h \
        dex/dex_file_layout.h \
        dex/dex_instruction.h \
        dex/dex_instruction_utils.h \
        dex/invoke_type.h \
        dex/method_reference.h
define dexfile_operator_srcs_GENERATED_SOURCES
GENERATED_SRC_DIR = $(call local-generated-sources-dir)
dexfile_operator_srcs_GEN = $$(addprefix $$(GENERATED_SRC_DIR)/,$(addsuffix _operator_out.cc,$(dexfile_operator_srcs_SRCS)))

$$(dexfile_operator_srcs_GEN): PRIVATE_CUSTOM_TOOL = art/tools/generate_operator_out.py art/libdexfile $$< > $$@
$$(dexfile_operator_srcs_GEN): $$(GENERATED_SRC_DIR)/%_operator_out.cc : $(LOCAL_PATH)/%
	$$(transform-generated-source)
LOCAL_GENERATED_SOURCES += $$(dexfile_operator_srcs_GEN)

endef

libdexfile_defaults_LDLIBS   = -lz
libdexfile_defaults_SHARED_LIBS =  \
        libutils \
        libziparchive \
        libartbase \
        libartpalette \
        liblog \
        libbase
libdexfile_defaults_SRCS     =  \
        dex/art_dex_file_loader.cc \
        dex/compact_dex_file.cc \
        dex/compact_offset_table.cc \
        dex/descriptors_names.cc \
        dex/dex_file.cc \
        dex/dex_file_exception_helpers.cc \
        dex/dex_file_layout.cc \
        dex/dex_file_loader.cc \
        dex/dex_file_tracking_registrar.cc \
        dex/dex_file_verifier.cc \
        dex/dex_instruction.cc \
        dex/modifiers.cc \
        dex/primitive.cc \
        dex/signature.cc \
        dex/standard_dex_file.cc \
        dex/type_lookup_table.cc \
        dex/utf.cc
libdexfile_defaults_EXPORT_INCLUDE_DIRS = $(LOCAL_PATH)/.
libdexfile_defaults_GENERATED_SOURCES = $(dexfile_operator_srcs_GENERATED_SOURCES)

libdexfile_static_base_defaults_LDLIBS   = -lz

libdexfile_static_defaults_CXXFLAGS =  \
        $(libartbase_static_defaults_CXXFLAGS) \
        $(libdexfile_static_base_defaults_CXXFLAGS)
libdexfile_static_defaults_CFLAGS   =  \
        $(libartbase_static_defaults_CFLAGS) \
        $(libdexfile_static_base_defaults_CFLAGS)
libdexfile_static_defaults_LDFLAGS  =  \
        $(libartbase_static_defaults_LDFLAGS) \
        $(libdexfile_static_base_defaults_LDFLAGS)
libdexfile_static_defaults_LDLIBS   =  \
        $(libartbase_static_defaults_LDLIBS) \
        $(libdexfile_static_base_defaults_LDLIBS)
libdexfile_static_defaults_SRCS     =  \
        $(libartbase_static_defaults_SRCS) \
        $(libdexfile_static_base_defaults_SRCS)
libdexfile_static_defaults_INCLUDE_DIRS =  \
        $(libartbase_static_defaults_INCLUDE_DIRS) \
        $(libdexfile_static_base_defaults_INCLUDE_DIRS)
libdexfile_static_defaults_EXPORT_INCLUDE_DIRS =  \
        $(libartbase_static_defaults_EXPORT_INCLUDE_DIRS) \
        $(libdexfile_static_base_defaults_EXPORT_INCLUDE_DIRS)
libdexfile_static_defaults_GENERATED_SOURCES =  \
        $(libartbase_static_defaults_GENERATED_SOURCES) \
        $(libdexfile_static_base_defaults_GENERATED_SOURCES)

libdexfiled_static_defaults_CXXFLAGS =  \
        $(libartbased_static_defaults_CXXFLAGS) \
        $(libdexfile_static_base_defaults_CXXFLAGS)
libdexfiled_static_defaults_CFLAGS   =  \
        $(libartbased_static_defaults_CFLAGS) \
        $(libdexfile_static_base_defaults_CFLAGS)
libdexfiled_static_defaults_LDFLAGS  =  \
        $(libartbased_static_defaults_LDFLAGS) \
        $(libdexfile_static_base_defaults_LDFLAGS)
libdexfiled_static_defaults_LDLIBS   =  \
        $(libartbased_static_defaults_LDLIBS) \
        $(libdexfile_static_base_defaults_LDLIBS)
libdexfiled_static_defaults_SRCS     =  \
        $(libartbased_static_defaults_SRCS) \
        $(libdexfile_static_base_defaults_SRCS)
libdexfiled_static_defaults_INCLUDE_DIRS =  \
        $(libartbased_static_defaults_INCLUDE_DIRS) \
        $(libdexfile_static_base_defaults_INCLUDE_DIRS)
libdexfiled_static_defaults_EXPORT_INCLUDE_DIRS =  \
        $(libartbased_static_defaults_EXPORT_INCLUDE_DIRS) \
        $(libdexfile_static_base_defaults_EXPORT_INCLUDE_DIRS)
libdexfiled_static_defaults_GENERATED_SOURCES =  \
        $(libartbased_static_defaults_GENERATED_SOURCES) \
        $(libdexfile_static_base_defaults_GENERATED_SOURCES)

include art/build/Android.common_build.mk
# link libdexfile shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libdexfile
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libdexfile_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libdexfile_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libdexfile_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libdexfile_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libdexfile_defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(libdexfile_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libdexfile_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libdexfile_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libdexfile_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libdexfile static library
include $(CLEAR_VARS)
LOCAL_MODULE := libdexfile
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libdexfile_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libdexfile_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libdexfile_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libdexfile_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libdexfile_defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(libdexfile_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libdexfile_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libdexfile_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libdexfile_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

include art/build/Android.common_build.mk
# link libdexfiled shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libdexfiled
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libdexfile_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libdexfile_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libdexfile_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libdexfile_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libdexfile_defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(libdexfile_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libdexfile_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libdexfile_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libdexfile_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libdexfiled static library
include $(CLEAR_VARS)
LOCAL_MODULE := libdexfiled
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libdexfile_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libdexfile_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libdexfile_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libdexfile_defaults_LDLIBS)
LOCAL_SRC_FILES = $(libdexfile_defaults_SRCS)
LOCAL_SHARED_LIBRARIES = $(libdexfile_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libdexfile_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libdexfile_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libdexfile_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

# link libdexfile_external shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libdexfile_external
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_SRC_FILES = external/dex_file_ext.cc
LOCAL_SHARED_LIBRARIES =  \
        libbase \
        libdexfile
include $(BUILD_HOST_SHARED_LIBRARY)

# link libdexfile_external static library
include $(CLEAR_VARS)
LOCAL_MODULE := libdexfile_external
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_SRC_FILES = external/dex_file_ext.cc
LOCAL_SHARED_LIBRARIES =  \
        libbase \
        libdexfile
include $(BUILD_HOST_STATIC_LIBRARY)

# link libdexfile_support shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libdexfile_support
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_SRC_FILES = external/dex_file_supp.cc
LOCAL_SHARED_LIBRARIES = liblog
include $(BUILD_HOST_SHARED_LIBRARY)

# link libdexfile_support static library
include $(CLEAR_VARS)
LOCAL_MODULE := libdexfile_support
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_SRC_FILES = external/dex_file_supp.cc
LOCAL_SHARED_LIBRARIES = liblog
include $(BUILD_HOST_STATIC_LIBRARY)

# link libdexfile_support_static static library
include $(CLEAR_VARS)
LOCAL_MODULE := libdexfile_support_static
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libdexfile_static_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libdexfile_static_defaults_CFLAGS) \
        -DSTATIC_LIB
LOCAL_LDFLAGS  = $(libdexfile_static_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libdexfile_static_defaults_LDLIBS)
LOCAL_SRC_FILES =  \
        $(libdexfile_static_defaults_SRCS) \
        external/dex_file_supp.cc
LOCAL_SHARED_LIBRARIES = $(libdexfile_static_defaults_SHARED_LIBS)
LOCAL_WHOLE_STATIC_LIBRARIES =  \
        libbase \
        libdexfile \
        libdexfile_external \
        liblog \
        libz \
        libziparchive
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libdexfile_static_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES = $(libdexfile_static_defaults_INCLUDE_DIRS)
$(eval $(libdexfile_static_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)


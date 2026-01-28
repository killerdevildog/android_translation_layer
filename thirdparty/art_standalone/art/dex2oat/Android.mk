LOCAL_PATH := $(call my-dir)
art_dex2oat_operator_srcs_SRCS     =  \
        dex/dex_to_dex_compiler.h \
        driver/compiler_driver.h \
        linker/image_writer.h
define art_dex2oat_operator_srcs_GENERATED_SOURCES
GENERATED_SRC_DIR = $(call local-generated-sources-dir)
art_dex2oat_operator_srcs_GEN = $$(addprefix $$(GENERATED_SRC_DIR)/,$(addsuffix _operator_out.cc,$(art_dex2oat_operator_srcs_SRCS)))

$$(art_dex2oat_operator_srcs_GEN): PRIVATE_CUSTOM_TOOL = art/tools/generate_operator_out.py art/dex2oat $$< > $$@
$$(art_dex2oat_operator_srcs_GEN): $$(GENERATED_SRC_DIR)/%_operator_out.cc : $(LOCAL_PATH)/%
	$$(transform-generated-source)
LOCAL_GENERATED_SOURCES += $$(art_dex2oat_operator_srcs_GEN)

endef

libart-dex2oat-defaults_arm_CODEGEN_SRCS     =  \
        linker/arm/relative_patcher_arm_base.cc \
        linker/arm/relative_patcher_thumb2.cc
libart-dex2oat-defaults_arm64_CODEGEN_SRCS     =  \
        linker/arm64/relative_patcher_arm64.cc \
        $(libart-dex2oat-defaults_arm_CODEGEN_SRCS)
libart-dex2oat-defaults_mips_CODEGEN_SRCS     = linker/mips/relative_patcher_mips.cc
libart-dex2oat-defaults_mips64_CODEGEN_SRCS     = linker/mips64/relative_patcher_mips64.cc
libart-dex2oat-defaults_x86_CODEGEN_SRCS     =  \
        linker/x86/relative_patcher_x86.cc \
        linker/x86/relative_patcher_x86_base.cc
libart-dex2oat-defaults_x86_64_CODEGEN_SRCS     =  \
        linker/x86_64/relative_patcher_x86_64.cc \
        $(libart-dex2oat-defaults_x86_CODEGEN_SRCS)
define cpp-define-generator-asm-support_GENERATED_SOURCES
include art/build/Android.common_build.mk
GENERATED_SRC_DIR = $(call local-generated-sources-dir)

$$(GENERATED_SRC_DIR)/asm_defines.S: PRIVATE_CUSTOM_TOOL = $(HOST_CXX) -S $$< -o $$@ $(HOST_GLOBAL_CPPFLAGS) $(HOST_GLOBAL_CFLAGS) $(ART_HOST_CFLAGS) $(addprefix -I , $(ART_C_INCLUDES) libbase/include libnativehelper/include/nativehelper)
$$(GENERATED_SRC_DIR)/asm_defines.S : art/tools/cpp-define-generator/asm_defines.cc
	$$(transform-generated-source)

$$(GENERATED_SRC_DIR)/asm_defines.h: PRIVATE_CUSTOM_TOOL = art/tools/cpp-define-generator/make_header.py $$< > $$@
$$(GENERATED_SRC_DIR)/asm_defines.h : $$(GENERATED_SRC_DIR)/asm_defines.S
	$$(transform-generated-source)
LOCAL_GENERATED_SOURCES += $$(GENERATED_SRC_DIR)/asm_defines.h
LOCAL_ADDITIONAL_DEPENDENCIES += $$(GENERATED_SRC_DIR)/asm_defines.h
endef
libart-dex2oat-defaults_LDLIBS   =  \
        $(libart-dex2oat-defaults_$(HOST_ARCH)_CODEGEN_HOST_LDLIBS) \
        -lz \
        -lcrypto
libart-dex2oat-defaults_SHARED_LIBS = libbase
libart-dex2oat-defaults_SRCS     =  \
        $(libart-dex2oat-defaults_$(HOST_ARCH)_CODEGEN_SRCS) \
        dex/dex_to_dex_compiler.cc \
        dex/quick_compiler_callbacks.cc \
        driver/compiler_driver.cc \
        linker/elf_writer.cc \
        linker/elf_writer_quick.cc \
        linker/image_writer.cc \
        linker/multi_oat_relative_patcher.cc \
        linker/oat_writer.cc \
        linker/relative_patcher.cc
libart-dex2oat-defaults_INCLUDE_DIRS = $(LOCAL_PATH)/generated
libart-dex2oat-defaults_EXPORT_INCLUDE_DIRS = $(LOCAL_PATH)/.
libart-dex2oat-defaults_GENERATED_SOURCES =  \
        $(art_dex2oat_operator_srcs_GENERATED_SOURCES) \
        $(cpp-define-generator-asm-support_GENERATED_SOURCES)

libart-dex2oat_static_base_defaults_LDLIBS   = -lz

include art/build/Android.common_build.mk
# link libart-dex2oat static library
include $(CLEAR_VARS)
LOCAL_MODULE := libart-dex2oat
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart-dex2oat-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart-dex2oat-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart-dex2oat-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libart-dex2oat-defaults_LDLIBS)
LOCAL_SRC_FILES = $(libart-dex2oat-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart-compiler \
        libart-dexlayout \
        libart \
        libartpalette \
        libprofile \
        $(libart-dex2oat-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart-dex2oat-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart-dex2oat-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart-dex2oat-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

libart-dex2oat_static_defaults_CXXFLAGS = $(libart-dex2oat_static_base_defaults_CXXFLAGS)
libart-dex2oat_static_defaults_CFLAGS   = $(libart-dex2oat_static_base_defaults_CFLAGS)
libart-dex2oat_static_defaults_LDFLAGS  = $(libart-dex2oat_static_base_defaults_LDFLAGS)
libart-dex2oat_static_defaults_LDLIBS   = $(libart-dex2oat_static_base_defaults_LDLIBS)
libart-dex2oat_static_defaults_SRCS     = $(libart-dex2oat_static_base_defaults_SRCS)
libart-dex2oat_static_defaults_INCLUDE_DIRS = $(libart-dex2oat_static_base_defaults_INCLUDE_DIRS)
libart-dex2oat_static_defaults_EXPORT_INCLUDE_DIRS = $(libart-dex2oat_static_base_defaults_EXPORT_INCLUDE_DIRS)
libart-dex2oat_static_defaults_GENERATED_SOURCES = $(libart-dex2oat_static_base_defaults_GENERATED_SOURCES)

include art/build/Android.common_build.mk
# link libartd-dex2oat static library
include $(CLEAR_VARS)
LOCAL_MODULE := libartd-dex2oat
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart-dex2oat-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart-dex2oat-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart-dex2oat-defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libart-dex2oat-defaults_LDLIBS)
LOCAL_SRC_FILES = $(libart-dex2oat-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartd-compiler \
        libartd-dexlayout \
        libartd \
        libartpalette \
        libprofiled \
        $(libart-dex2oat-defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart-dex2oat-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart-dex2oat-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart-dex2oat-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

libartd-dex2oat_static_defaults_CXXFLAGS = $(libart-dex2oat_static_base_defaults_CXXFLAGS)
libartd-dex2oat_static_defaults_CFLAGS   = $(libart-dex2oat_static_base_defaults_CFLAGS)
libartd-dex2oat_static_defaults_LDFLAGS  = $(libart-dex2oat_static_base_defaults_LDFLAGS)
libartd-dex2oat_static_defaults_LDLIBS   = $(libart-dex2oat_static_base_defaults_LDLIBS)
libartd-dex2oat_static_defaults_SRCS     = $(libart-dex2oat_static_base_defaults_SRCS)
libartd-dex2oat_static_defaults_INCLUDE_DIRS = $(libart-dex2oat_static_base_defaults_INCLUDE_DIRS)
libartd-dex2oat_static_defaults_EXPORT_INCLUDE_DIRS = $(libart-dex2oat_static_base_defaults_EXPORT_INCLUDE_DIRS)
libartd-dex2oat_static_defaults_GENERATED_SOURCES = $(libart-dex2oat_static_base_defaults_GENERATED_SOURCES)

dex2oat-defaults_arm_HOST_LDLIBS = -latomic
dex2oat-defaults_arm64_HOST_LDLIBS = -latomic
dex2oat-defaults_LDLIBS   =  \
        $(dex2oat-defaults_$(HOST_ARCH)_HOST_LDLIBS) \
        -lcrypto \
        -llz4
dex2oat-defaults_SRCS     =  \
        $(dex2oat-defaults_$(HOST_ARCH)_SRCS) \
        dex2oat_options.cc \
        dex2oat.cc
dex2oat-defaults_GENERATED_SOURCES = $(dex2oat-defaults_$(HOST_ARCH)_GENERATED_SOURCES)


include art/build/Android.common_build.mk
# link dex2oat binary
include $(CLEAR_VARS)
LOCAL_MODULE := dex2oat
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS =  \
        $(dex2oat-defaults_CXXFLAGS) \
        $(dex2oat-pgo-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dex2oat-defaults_CFLAGS) \
        $(dex2oat-pgo-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  =  \
        $(dex2oat-defaults_LDFLAGS) \
        $(dex2oat-pgo-defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(dex2oat-defaults_LDLIBS) \
        $(dex2oat-pgo-defaults_LDLIBS) \
        -lz
LOCAL_SRC_FILES =  \
        $(dex2oat-defaults_SRCS) \
        $(dex2oat-pgo-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libprofile \
        libart-compiler \
        libart-dexlayout \
        libart \
        libdexfile \
        libartbase \
        libartpalette \
        libbase \
        libsigchain \
        $(dex2oat-defaults_SHARED_LIBS) \
        $(dex2oat-pgo-defaults_SHARED_LIBS)
LOCAL_STATIC_LIBRARIES = libart-dex2oat
LOCAL_C_INCLUDES =  \
        $(dex2oat-defaults_INCLUDE_DIRS) \
        $(dex2oat-pgo-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dex2oat-defaults_GENERATED_SOURCES))
$(eval $(dex2oat-pgo-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)

include art/build/Android.common_build.mk
# link dex2oatd binary
include $(CLEAR_VARS)
LOCAL_MODULE := dex2oatd
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(dex2oat-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dex2oat-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(dex2oat-defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(dex2oat-defaults_LDLIBS) \
        -lz
LOCAL_SRC_FILES = $(dex2oat-defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libprofiled \
        libartd-compiler \
        libartd-dexlayout \
        libartd \
        libdexfiled \
        libartbased \
        libartpalette \
        libbase \
        libsigchain \
        $(dex2oat-defaults_SHARED_LIBS)
LOCAL_STATIC_LIBRARIES = libartd-dex2oat
LOCAL_C_INCLUDES =  \
        $(dex2oat-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dex2oat-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)

dex2oats-defaults_CXXFLAGS = $(dex2oat-defaults_CXXFLAGS)
dex2oats-defaults_CFLAGS   = $(dex2oat-defaults_CFLAGS)
dex2oats-defaults_LDFLAGS  =  \
        $(dex2oat-defaults_LDFLAGS) \
        -z muldefs
dex2oats-defaults_LDLIBS   =  \
        $(dex2oat-defaults_LDLIBS) \
        -lz
dex2oats-defaults_SRCS     = $(dex2oat-defaults_SRCS)
dex2oats-defaults_INCLUDE_DIRS = $(dex2oat-defaults_INCLUDE_DIRS)
dex2oats-defaults_EXPORT_INCLUDE_DIRS = $(dex2oat-defaults_EXPORT_INCLUDE_DIRS)
dex2oats-defaults_GENERATED_SOURCES = $(dex2oat-defaults_GENERATED_SOURCES)

include art/build/Android.common_build.mk
# link dex2oats binary
include $(CLEAR_VARS)
LOCAL_MODULE := dex2oats
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS =  \
        $(dex2oats-defaults_CXXFLAGS) \
        $(libart-compiler_static_defaults_CXXFLAGS) \
        $(libart-dex2oat_static_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dex2oats-defaults_CFLAGS) \
        $(libart-compiler_static_defaults_CFLAGS) \
        $(libart-dex2oat_static_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  =  \
        $(dex2oats-defaults_LDFLAGS) \
        $(libart-compiler_static_defaults_LDFLAGS) \
        $(libart-dex2oat_static_defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(dex2oats-defaults_LDLIBS) \
        $(libart-compiler_static_defaults_LDLIBS) \
        $(libart-dex2oat_static_defaults_LDLIBS)
LOCAL_SRC_FILES =  \
        $(dex2oats-defaults_SRCS) \
        $(libart-compiler_static_defaults_SRCS) \
        $(libart-dex2oat_static_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        $(dex2oats-defaults_SHARED_LIBS) \
        $(libart-compiler_static_defaults_SHARED_LIBS) \
        $(libart-dex2oat_static_defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(dex2oats-defaults_INCLUDE_DIRS) \
        $(libart-compiler_static_defaults_INCLUDE_DIRS) \
        $(libart-dex2oat_static_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dex2oats-defaults_GENERATED_SOURCES))
$(eval $(libart-compiler_static_defaults_GENERATED_SOURCES))
$(eval $(libart-dex2oat_static_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)

include art/build/Android.common_build.mk
# link dex2oatds binary
include $(CLEAR_VARS)
LOCAL_MODULE := dex2oatds
LOCAL_MODULE_CLASS := EXECUTABLES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS =  \
        $(dex2oats-defaults_CXXFLAGS) \
        $(libartd-compiler_static_defaults_CXXFLAGS) \
        $(libartd-dex2oat_static_defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(dex2oats-defaults_CFLAGS) \
        $(libartd-compiler_static_defaults_CFLAGS) \
        $(libartd-dex2oat_static_defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  =  \
        $(dex2oats-defaults_LDFLAGS) \
        $(libartd-compiler_static_defaults_LDFLAGS) \
        $(libartd-dex2oat_static_defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(dex2oats-defaults_LDLIBS) \
        $(libartd-compiler_static_defaults_LDLIBS) \
        $(libartd-dex2oat_static_defaults_LDLIBS)
LOCAL_SRC_FILES =  \
        $(dex2oats-defaults_SRCS) \
        $(libartd-compiler_static_defaults_SRCS) \
        $(libartd-dex2oat_static_defaults_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        $(dex2oats-defaults_SHARED_LIBS) \
        $(libartd-compiler_static_defaults_SHARED_LIBS) \
        $(libartd-dex2oat_static_defaults_SHARED_LIBS)
LOCAL_C_INCLUDES =  \
        $(dex2oats-defaults_INCLUDE_DIRS) \
        $(libartd-compiler_static_defaults_INCLUDE_DIRS) \
        $(libartd-dex2oat_static_defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(dex2oats-defaults_GENERATED_SOURCES))
$(eval $(libartd-compiler_static_defaults_GENERATED_SOURCES))
$(eval $(libartd-dex2oat_static_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_EXECUTABLE)

include art/build/Android.common_build.mk
# link libart-dex2oat-gtest static library
include $(CLEAR_VARS)
LOCAL_MODULE := libart-dex2oat-gtest
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   = $(ART_HOST_CFLAGS)
LOCAL_SRC_FILES = common_compiler_driver_test.cc
LOCAL_SHARED_LIBRARIES =  \
        libartd-compiler \
        libartd-disassembler \
        libart-compiler-gtest \
        libart-runtime-gtest \
        libbase
LOCAL_STATIC_LIBRARIES = libartd-dex2oat
LOCAL_C_INCLUDES = $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
include $(BUILD_HOST_STATIC_LIBRARY)


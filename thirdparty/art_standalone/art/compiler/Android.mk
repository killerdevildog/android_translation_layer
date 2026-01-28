LOCAL_PATH := $(call my-dir)
art_compiler_operator_srcs_SRCS     =  \
        driver/compiler_options.h \
        linker/linker_patch.h \
        optimizing/locations.h \
        optimizing/optimizing_compiler_stats.h \
        utils/arm/constants_arm.h \
        utils/mips/assembler_mips.h \
        utils/mips64/assembler_mips64.h
define art_compiler_operator_srcs_GENERATED_SOURCES
GENERATED_SRC_DIR = $(call local-generated-sources-dir)
art_compiler_operator_srcs_GEN = $$(addprefix $$(GENERATED_SRC_DIR)/,$(addsuffix _operator_out.cc,$(art_compiler_operator_srcs_SRCS)))

$$(art_compiler_operator_srcs_GEN): PRIVATE_CUSTOM_TOOL = art/tools/generate_operator_out.py art/compiler $$< > $$@
$$(art_compiler_operator_srcs_GEN): $$(GENERATED_SRC_DIR)/%_operator_out.cc : $(LOCAL_PATH)/%
	$$(transform-generated-source)
LOCAL_GENERATED_SOURCES += $$(art_compiler_operator_srcs_GEN)

endef

libart-compiler-defaults_arm_CODEGEN_SRCS     =  \
        jni/quick/arm/calling_convention_arm.cc \
        optimizing/code_generator_arm_vixl.cc \
        optimizing/code_generator_vector_arm_vixl.cc \
        optimizing/instruction_simplifier_arm.cc \
        optimizing/instruction_simplifier_shared.cc \
        optimizing/intrinsics_arm_vixl.cc \
        optimizing/nodes_shared.cc \
        optimizing/scheduler_arm.cc \
        utils/arm/assembler_arm_vixl.cc \
        utils/arm/constants_arm.cc \
        utils/arm/jni_macro_assembler_arm_vixl.cc \
        utils/arm/managed_register_arm.cc
libart-compiler-defaults_arm64_CODEGEN_SRCS     =  \
        jni/quick/arm64/calling_convention_arm64.cc \
        optimizing/code_generator_arm64.cc \
        optimizing/code_generator_vector_arm64.cc \
        optimizing/scheduler_arm64.cc \
        optimizing/instruction_simplifier_arm64.cc \
        optimizing/intrinsics_arm64.cc \
        utils/arm64/assembler_arm64.cc \
        utils/arm64/jni_macro_assembler_arm64.cc \
        utils/arm64/managed_register_arm64.cc \
        $(libart-compiler-defaults_arm_CODEGEN_SRCS)
libart-compiler-defaults_mips_CODEGEN_SRCS     =  \
        jni/quick/mips/calling_convention_mips.cc \
        optimizing/code_generator_mips.cc \
        optimizing/code_generator_vector_mips.cc \
        optimizing/instruction_simplifier_mips.cc \
        optimizing/intrinsics_mips.cc \
        optimizing/pc_relative_fixups_mips.cc \
        utils/mips/assembler_mips.cc \
        utils/mips/managed_register_mips.cc
libart-compiler-defaults_mips64_CODEGEN_SRCS     =  \
        jni/quick/mips64/calling_convention_mips64.cc \
        optimizing/code_generator_mips64.cc \
        optimizing/code_generator_vector_mips64.cc \
        optimizing/intrinsics_mips64.cc \
        utils/mips64/assembler_mips64.cc \
        utils/mips64/managed_register_mips64.cc
libart-compiler-defaults_x86_CODEGEN_SRCS     =  \
        jni/quick/x86/calling_convention_x86.cc \
        optimizing/code_generator_x86.cc \
        optimizing/code_generator_vector_x86.cc \
        optimizing/intrinsics_x86.cc \
        optimizing/instruction_simplifier_x86_shared.cc \
        optimizing/instruction_simplifier_x86.cc \
        optimizing/pc_relative_fixups_x86.cc \
        optimizing/x86_memory_gen.cc \
        utils/x86/assembler_x86.cc \
        utils/x86/jni_macro_assembler_x86.cc \
        utils/x86/managed_register_x86.cc
libart-compiler-defaults_x86_64_CODEGEN_SRCS     =  \
        jni/quick/x86_64/calling_convention_x86_64.cc \
        optimizing/intrinsics_x86_64.cc \
        optimizing/instruction_simplifier_x86_64.cc \
        optimizing/code_generator_x86_64.cc \
        optimizing/code_generator_vector_x86_64.cc \
        utils/x86_64/assembler_x86_64.cc \
        utils/x86_64/jni_macro_assembler_x86_64.cc \
        utils/x86_64/managed_register_x86_64.cc \
        $(libart-compiler-defaults_x86_CODEGEN_SRCS)
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
libart-compiler-defaults_LDLIBS   =  \
        $(libart-compiler-defaults_$(HOST_ARCH)_CODEGEN_HOST_LDLIBS) \
        -llzma
libart-compiler-defaults_SHARED_LIBS = libbase
libart-compiler-defaults_SRCS     =  \
        $(libart-compiler-defaults_$(HOST_ARCH)_CODEGEN_SRCS) \
        compiled_method.cc \
        debug/elf_debug_writer.cc \
        dex/inline_method_analyser.cc \
        dex/verified_method.cc \
        dex/verification_results.cc \
        driver/compiled_method_storage.cc \
        driver/compiler_options.cc \
        driver/dex_compilation_unit.cc \
        jit/jit_compiler.cc \
        jit/jit_logger.cc \
        jni/quick/calling_convention.cc \
        jni/quick/jni_compiler.cc \
        optimizing/block_builder.cc \
        optimizing/bounds_check_elimination.cc \
        optimizing/builder.cc \
        optimizing/cha_guard_optimization.cc \
        optimizing/code_generator.cc \
        optimizing/code_generator_utils.cc \
        optimizing/code_sinking.cc \
        optimizing/constant_folding.cc \
        optimizing/constructor_fence_redundancy_elimination.cc \
        optimizing/data_type.cc \
        optimizing/dead_code_elimination.cc \
        optimizing/escape.cc \
        optimizing/graph_checker.cc \
        optimizing/graph_visualizer.cc \
        optimizing/gvn.cc \
        optimizing/induction_var_analysis.cc \
        optimizing/induction_var_range.cc \
        optimizing/inliner.cc \
        optimizing/instruction_builder.cc \
        optimizing/instruction_simplifier.cc \
        optimizing/intrinsic_objects.cc \
        optimizing/intrinsics.cc \
        optimizing/licm.cc \
        optimizing/linear_order.cc \
        optimizing/load_store_analysis.cc \
        optimizing/load_store_elimination.cc \
        optimizing/locations.cc \
        optimizing/loop_analysis.cc \
        optimizing/loop_optimization.cc \
        optimizing/nodes.cc \
        optimizing/optimization.cc \
        optimizing/optimizing_compiler.cc \
        optimizing/parallel_move_resolver.cc \
        optimizing/prepare_for_register_allocation.cc \
        optimizing/reference_type_propagation.cc \
        optimizing/register_allocation_resolver.cc \
        optimizing/register_allocator.cc \
        optimizing/register_allocator_graph_color.cc \
        optimizing/register_allocator_linear_scan.cc \
        optimizing/select_generator.cc \
        optimizing/scheduler.cc \
        optimizing/sharpening.cc \
        optimizing/side_effects_analysis.cc \
        optimizing/ssa_builder.cc \
        optimizing/ssa_liveness_analysis.cc \
        optimizing/ssa_phi_elimination.cc \
        optimizing/stack_map_stream.cc \
        optimizing/superblock_cloner.cc \
        trampolines/trampoline_compiler.cc \
        utils/assembler.cc \
        utils/jni_macro_assembler.cc \
        utils/swap_space.cc \
        compiler.cc
libart-compiler-defaults_INCLUDE_DIRS =  \
        art/disassembler \
        $(LOCAL_PATH)/generated
libart-compiler-defaults_EXPORT_INCLUDE_DIRS = $(LOCAL_PATH)/.
libart-compiler-defaults_GENERATED_SOURCES =  \
        $(art_compiler_operator_srcs_GENERATED_SOURCES) \
        $(cpp-define-generator-asm-support_GENERATED_SOURCES)


include art/build/Android.common_build.mk
# link libart-compiler shared library
libart-compiler_arm_CODEGEN_HOST_LDLIBS = -lvixl
libart-compiler_arm64_CODEGEN_HOST_LDLIBS =  \
        -lvixl \
        $(libart-compiler_arm_CODEGEN_HOST_LDLIBS)
include $(CLEAR_VARS)
LOCAL_MODULE := libart-compiler
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart-compiler-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart-compiler-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart-compiler-defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(libart-compiler-defaults_LDLIBS) \
        $(libart-compiler_$(HOST_ARCH)_CODEGEN_HOST_LDLIBS)
LOCAL_SRC_FILES =  \
        $(libart-compiler-defaults_SRCS) \
        $(libart-compiler_$(HOST_ARCH)_CODEGEN_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libartbase \
        libartpalette \
        libprofile \
        libdexfile \
        $(libart-compiler-defaults_SHARED_LIBS) \
        $(libart-compiler_$(HOST_ARCH)_CODEGEN_SHARED_LIBS)
LOCAL_WHOLE_STATIC_LIBRARIES = libelffile
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart-compiler-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart-compiler-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart-compiler-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libart-compiler static library
libart-compiler_arm_CODEGEN_HOST_LDLIBS = -lvixl
libart-compiler_arm64_CODEGEN_HOST_LDLIBS =  \
        -lvixl \
        $(libart-compiler_arm_CODEGEN_HOST_LDLIBS)
include $(CLEAR_VARS)
LOCAL_MODULE := libart-compiler
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart-compiler-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart-compiler-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart-compiler-defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(libart-compiler-defaults_LDLIBS) \
        $(libart-compiler_$(HOST_ARCH)_CODEGEN_HOST_LDLIBS)
LOCAL_SRC_FILES =  \
        $(libart-compiler-defaults_SRCS) \
        $(libart-compiler_$(HOST_ARCH)_CODEGEN_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libart \
        libartbase \
        libartpalette \
        libprofile \
        libdexfile \
        $(libart-compiler-defaults_SHARED_LIBS) \
        $(libart-compiler_$(HOST_ARCH)_CODEGEN_SHARED_LIBS)
LOCAL_WHOLE_STATIC_LIBRARIES = libelffile
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart-compiler-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart-compiler-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart-compiler-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

libart-compiler_static_defaults_CXXFLAGS = $(libart-compiler_static_base_defaults_CXXFLAGS)
libart-compiler_static_defaults_CFLAGS   = $(libart-compiler_static_base_defaults_CFLAGS)
libart-compiler_static_defaults_LDFLAGS  = $(libart-compiler_static_base_defaults_LDFLAGS)
libart-compiler_static_defaults_LDLIBS   = $(libart-compiler_static_base_defaults_LDLIBS)
libart-compiler_static_defaults_SRCS     = $(libart-compiler_static_base_defaults_SRCS)
libart-compiler_static_defaults_INCLUDE_DIRS = $(libart-compiler_static_base_defaults_INCLUDE_DIRS)
libart-compiler_static_defaults_EXPORT_INCLUDE_DIRS = $(libart-compiler_static_base_defaults_EXPORT_INCLUDE_DIRS)
libart-compiler_static_defaults_GENERATED_SOURCES = $(libart-compiler_static_base_defaults_GENERATED_SOURCES)

include art/build/Android.common_build.mk
# link libartd-compiler shared library
libartd-compiler_arm_CODEGEN_SHARED_LIBS = libvixld
libartd-compiler_arm64_CODEGEN_SHARED_LIBS =  \
        libvixld \
        $(libartd-compiler_arm_CODEGEN_SHARED_LIBS)
include $(CLEAR_VARS)
LOCAL_MODULE := libartd-compiler
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart-compiler-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart-compiler-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart-compiler-defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(libart-compiler-defaults_LDLIBS) \
        $(libartd-compiler_$(HOST_ARCH)_CODEGEN_HOST_LDLIBS)
LOCAL_SRC_FILES =  \
        $(libart-compiler-defaults_SRCS) \
        $(libartd-compiler_$(HOST_ARCH)_CODEGEN_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartbased \
        libartd \
        libartpalette \
        libprofiled \
        libdexfiled \
        $(libart-compiler-defaults_SHARED_LIBS) \
        $(libartd-compiler_$(HOST_ARCH)_CODEGEN_SHARED_LIBS)
LOCAL_WHOLE_STATIC_LIBRARIES = libelffiled
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart-compiler-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart-compiler-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart-compiler-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libartd-compiler static library
libartd-compiler_arm_CODEGEN_SHARED_LIBS =  \
        libvixld \
        libvixld
libartd-compiler_arm64_CODEGEN_SHARED_LIBS =  \
        libvixld \
        libvixld \
        $(libartd-compiler_arm_CODEGEN_SHARED_LIBS) \
        $(libartd-compiler_arm_CODEGEN_SHARED_LIBS)
include $(CLEAR_VARS)
LOCAL_MODULE := libartd-compiler
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libart-compiler-defaults_CXXFLAGS)
LOCAL_CFLAGS   =  \
        $(libart-compiler-defaults_CFLAGS) \
        $(ART_HOST_CFLAGS)
LOCAL_LDFLAGS  = $(libart-compiler-defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(libart-compiler-defaults_LDLIBS) \
        $(libartd-compiler_$(HOST_ARCH)_CODEGEN_HOST_LDLIBS)
LOCAL_SRC_FILES =  \
        $(libart-compiler-defaults_SRCS) \
        $(libartd-compiler_$(HOST_ARCH)_CODEGEN_SRCS)
LOCAL_SHARED_LIBRARIES =  \
        libartbased \
        libartd \
        libartpalette \
        libprofiled \
        libdexfiled \
        $(libart-compiler-defaults_SHARED_LIBS) \
        $(libartd-compiler_$(HOST_ARCH)_CODEGEN_SHARED_LIBS)
LOCAL_WHOLE_STATIC_LIBRARIES = libelffiled
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libart-compiler-defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES =  \
        $(libart-compiler-defaults_INCLUDE_DIRS) \
        $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
$(eval $(libart-compiler-defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

libartd-compiler_static_defaults_CXXFLAGS = $(libart-compiler_static_base_defaults_CXXFLAGS)
libartd-compiler_static_defaults_CFLAGS   = $(libart-compiler_static_base_defaults_CFLAGS)
libartd-compiler_static_defaults_LDFLAGS  = $(libart-compiler_static_base_defaults_LDFLAGS)
libartd-compiler_static_defaults_LDLIBS   = $(libart-compiler_static_base_defaults_LDLIBS)
libartd-compiler_static_defaults_SRCS     = $(libart-compiler_static_base_defaults_SRCS)
libartd-compiler_static_defaults_INCLUDE_DIRS = $(libart-compiler_static_base_defaults_INCLUDE_DIRS)
libartd-compiler_static_defaults_EXPORT_INCLUDE_DIRS = $(libart-compiler_static_base_defaults_EXPORT_INCLUDE_DIRS)
libartd-compiler_static_defaults_GENERATED_SOURCES = $(libart-compiler_static_base_defaults_GENERATED_SOURCES)

include art/build/Android.common_build.mk
# link libart-compiler-gtest shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libart-compiler-gtest
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   = $(ART_HOST_CFLAGS)
LOCAL_SRC_FILES = common_compiler_test.cc
LOCAL_SHARED_LIBRARIES =  \
        libartd-compiler \
        libartd-disassembler \
        libartbase-art-gtest \
        libart-runtime-gtest \
        libbase
LOCAL_C_INCLUDES = $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
include $(BUILD_HOST_SHARED_LIBRARY)

# link libart-compiler-gtest static library
include $(CLEAR_VARS)
LOCAL_MODULE := libart-compiler-gtest
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CFLAGS   = $(ART_HOST_CFLAGS)
LOCAL_SRC_FILES = common_compiler_test.cc
LOCAL_SHARED_LIBRARIES =  \
        libartd-compiler \
        libartd-disassembler \
        libartbase-art-gtest \
        libart-runtime-gtest \
        libbase
LOCAL_C_INCLUDES = $(ART_C_INCLUDES)
LOCAL_CPP_EXTENSION = .cc
include $(BUILD_HOST_STATIC_LIBRARY)


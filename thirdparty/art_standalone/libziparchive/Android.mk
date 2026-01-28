LOCAL_PATH := $(call my-dir)
libziparchive_flags_CXXFLAGS =  \
        -Wold-style-cast \
        -Wno-missing-field-initializers \
        -Wconversion \
        -Wno-sign-conversion
libziparchive_flags_CFLAGS   =  \
        -DZLIB_CONST \
        -D_FILE_OFFSET_BITS=64

libziparchive_defaults_SHARED_LIBS =  \
        libbase \
        liblog
libziparchive_defaults_SRCS     =  \
        zip_archive.cc \
        zip_archive_stream_entry.cc \
        zip_cd_entry_map.cc \
        zip_error.cc \
        zip_writer.cc
libziparchive_defaults_EXPORT_INCLUDE_DIRS = $(LOCAL_PATH)/include
libziparchive_defaults_INCLUDE_DIRS = $(libziparchive_defaults_EXPORT_INCLUDE_DIRS) $(LOCAL_PATH)/incfs_support/include/

incfs_support_defaults_CFLAGS   = -DZIPARCHIVE_DISABLE_CALLBACK_API=1
incfs_support_defaults_EXPORT_INCLUDE_DIRS = $(LOCAL_PATH)/incfs_support/include/

libziparchive_lib_defaults_CXXFLAGS =  \
        $(libziparchive_defaults_CXXFLAGS) \
        $(libziparchive_flags_CXXFLAGS)
libziparchive_lib_defaults_CFLAGS   =  \
        $(libziparchive_defaults_CFLAGS) \
        $(libziparchive_flags_CFLAGS)
libziparchive_lib_defaults_LDFLAGS  =  \
        $(libziparchive_defaults_LDFLAGS) \
        $(libziparchive_flags_LDFLAGS)
libziparchive_lib_defaults_LDLIBS   =  \
        $(libziparchive_defaults_LDLIBS) \
        $(libziparchive_flags_LDLIBS) \
        -lz
libziparchive_lib_defaults_SHARED_LIBS =  \
        liblog \
        libbase
libziparchive_lib_defaults_SRCS     =  \
        $(libziparchive_defaults_SRCS) \
        $(libziparchive_flags_SRCS)
libziparchive_lib_defaults_INCLUDE_DIRS =  \
        $(libziparchive_defaults_INCLUDE_DIRS) \
        $(libziparchive_flags_INCLUDE_DIRS)
libziparchive_lib_defaults_EXPORT_INCLUDE_DIRS =  \
        $(libziparchive_defaults_EXPORT_INCLUDE_DIRS) \
        $(libziparchive_flags_EXPORT_INCLUDE_DIRS)
libziparchive_lib_defaults_GENERATED_SOURCES =  \
        $(libziparchive_defaults_GENERATED_SOURCES) \
        $(libziparchive_flags_GENERATED_SOURCES)

# link libziparchive shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libziparchive
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CPPFLAGS = $(libziparchive_lib_defaults_CXXFLAGS)
LOCAL_CPP_EXTENSION = cc
LOCAL_CFLAGS   =  \
        $(libziparchive_lib_defaults_CFLAGS) \
        -DINCFS_SUPPORT_DISABLED=1
LOCAL_LDFLAGS  = $(libziparchive_lib_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libziparchive_lib_defaults_LDLIBS)
LOCAL_SRC_FILES_ = $(libziparchive_lib_defaults_SRCS)
LOCAL_SRC_FILES = $(sort $(LOCAL_SRC_FILES_))
LOCAL_SHARED_LIBRARIES = $(libziparchive_lib_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libziparchive_lib_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES = $(libziparchive_lib_defaults_INCLUDE_DIRS)
$(eval $(libziparchive_lib_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)


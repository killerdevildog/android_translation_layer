LOCAL_PATH := $(call my-dir)
libbase_cflags_defaults_CFLAGS   =  \
        -Wall \
        -Wextra

libbase_cflags_defaults_LDLIBS = \
        -lbsd

libbase_defaults_CXXFLAGS =  \
        $(libbase_cflags_defaults_CXXFLAGS) \
        -Wexit-time-destructors
libbase_defaults_CFLAGS   = $(libbase_cflags_defaults_CFLAGS)
libbase_defaults_LDFLAGS  = $(libbase_cflags_defaults_LDFLAGS)
libbase_defaults_LDLIBS   = $(libbase_cflags_defaults_LDLIBS)
libbase_defaults_SHARED_LIBS = liblog
libbase_defaults_SRCS     =  \
        $(libbase_cflags_defaults_SRCS) \
        errors_unix.cpp \
        abi_compatibility.cpp \
        chrono_utils.cpp \
        cmsg.cpp \
        file.cpp \
        hex.cpp \
        logging.cpp \
        mapped_file.cpp \
        parsebool.cpp \
        parsenetaddress.cpp \
        posix_strerror_r.cpp \
        process.cpp \
        stringprintf.cpp \
        strings.cpp \
        threads.cpp \
        test_utils.cpp
libbase_defaults_INCLUDE_DIRS = $(LOCAL_PATH)/include
libbase_defaults_EXPORT_INCLUDE_DIRS = $(LOCAL_PATH)/include

# link libbase shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libbase
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libbase_defaults_CXXFLAGS)
LOCAL_CFLAGS   = $(libbase_defaults_CFLAGS)
LOCAL_LDFLAGS  = $(libbase_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libbase_defaults_LDLIBS)
LOCAL_SRC_FILES_ = $(libbase_defaults_SRCS)
LOCAL_SRC_FILES = $(sort $(LOCAL_SRC_FILES_))
LOCAL_SHARED_LIBRARIES = $(libbase_defaults_SHARED_LIBS)
LOCAL_EXPORT_C_INCLUDE_DIRS = $(libbase_defaults_EXPORT_INCLUDE_DIRS)
LOCAL_C_INCLUDES = $(libbase_defaults_INCLUDE_DIRS)
$(eval $(libbase_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

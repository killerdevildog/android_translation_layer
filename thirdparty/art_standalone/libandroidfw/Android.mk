LOCAL_PATH := $(call my-dir)
libandroidfw_defaults_CFLAGS   = -Wunreachable-code
libandroidfw_defaults_INCLUDE_DIRS := $(LOCAL_PATH)/include

# link libandroidfw shared library
include $(CLEAR_VARS)
LOCAL_MODULE := libandroidfw
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libandroidfw_defaults_CXXFLAGS)
LOCAL_CFLAGS   = $(libandroidfw_defaults_CFLAGS)
LOCAL_LDFLAGS  = $(libandroidfw_defaults_LDFLAGS)
LOCAL_LDLIBS   =  \
        $(libandroidfw_defaults_LDLIBS) \
        -lz
LOCAL_SRC_FILES_ =  \
        $(libandroidfw_defaults_SRCS) \
        ApkAssets.cpp \
        ApkParsing.cpp \
        Asset.cpp \
        AssetDir.cpp \
        AssetManager.cpp \
        AssetManager2.cpp \
        AssetsProvider.cpp \
        AttributeResolution.cpp \
        BigBuffer.cpp \
        BigBufferStream.cpp \
        ChunkIterator.cpp \
        ConfigDescription.cpp \
        FileStream.cpp \
        Idmap.cpp \
        LoadedArsc.cpp \
        Locale.cpp \
        LocaleData.cpp \
        LocaleDataLookup.cpp \
        misc.cpp \
        NinePatch.cpp \
        ObbFile.cpp \
        PosixUtils.cpp \
        Png.cpp \
        PngChunkFilter.cpp \
        PngCrunch.cpp \
        ResourceTimer.cpp \
        ResourceTypes.cpp \
        ResourceUtils.cpp \
        StreamingZipInflater.cpp \
        StringPool.cpp \
        TypeWrappers.cpp \
        Util.cpp \
        ZipFileRO.cpp \
        ZipUtils.cpp \
        androidfw_c_api.cpp
LOCAL_SRC_FILES = $(sort $(LOCAL_SRC_FILES_))
LOCAL_SHARED_LIBRARIES =  \
        libbase \
        libcutils \
        liblog \
        libutils \
        libziparchive \
        $(libandroidfw_defaults_SHARED_LIBS)
LOCAL_STATIC_LIBRARIES = libincfs-utils
LOCAL_WHOLE_STATIC_LIBRARIES =  \
        libandroidfw_pathutils \
        libincfs-utils
LOCAL_EXPORT_C_INCLUDE_DIRS =  \
        $(libandroidfw_defaults_EXPORT_INCLUDE_DIRS) \
        include
LOCAL_C_INCLUDES = $(libandroidfw_defaults_INCLUDE_DIRS)
$(eval $(libandroidfw_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_SHARED_LIBRARY)

# link libandroidfw_pathutils static library
include $(CLEAR_VARS)
LOCAL_MODULE := libandroidfw_pathutils
LOCAL_MODULE_CLASS := STATIC_LIBRARIES
LOCAL_IS_HOST_MODULE := true
LOCAL_CXXFLAGS = $(libandroidfw_defaults_CXXFLAGS)
LOCAL_CFLAGS   = $(libandroidfw_defaults_CFLAGS)
LOCAL_LDFLAGS  = $(libandroidfw_defaults_LDFLAGS)
LOCAL_LDLIBS   = $(libandroidfw_defaults_LDLIBS)
LOCAL_SRC_FILES_ =  \
        $(libandroidfw_defaults_SRCS) \
        PathUtils.cpp
LOCAL_SRC_FILES = $(sort $(LOCAL_SRC_FILES_))
LOCAL_SHARED_LIBRARIES =  \
        libutils \
        $(libandroidfw_defaults_SHARED_LIBS)
LOCAL_STATIC_LIBRARIES = libincfs-utils
LOCAL_EXPORT_C_INCLUDE_DIRS =  \
        $(libandroidfw_defaults_EXPORT_INCLUDE_DIRS) \
        $(LOCAL_PATH)/include_pathutils
LOCAL_C_INCLUDES = $(libandroidfw_defaults_INCLUDE_DIRS) \
        $(LOCAL_PATH)/include_pathutils
$(eval $(libandroidfw_defaults_GENERATED_SOURCES))
include $(BUILD_HOST_STATIC_LIBRARY)

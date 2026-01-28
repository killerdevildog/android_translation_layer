#!/bin/bash
# Android Translation Layer Runner Script

ATL_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$ATL_DIR/build"

export PATH="$BUILD_DIR/bin:$PATH"
export LD_LIBRARY_PATH="$BUILD_DIR/lib:$BUILD_DIR/lib/art:$BUILD_DIR/bionic_build:$BUILD_DIR/atl_build:$LD_LIBRARY_PATH"

# Create natives directory if needed
mkdir -p "$BUILD_DIR/lib/java/dex/android_translation_layer/natives"

# Copy libtranslation_layer_main.so to natives directory
cp -f "$BUILD_DIR/atl_build/libtranslation_layer_main.so" "$BUILD_DIR/lib/java/dex/android_translation_layer/natives/"

exec "$BUILD_DIR/atl_build/android-translation-layer" --sdk-int=28 "$@"

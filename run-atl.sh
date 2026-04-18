#!/bin/bash
# Android Translation Layer Runner Script

ATL_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$ATL_DIR/build"

export PATH="$BUILD_DIR/bin:$PATH"
export LD_LIBRARY_PATH="$BUILD_DIR/lib:$BUILD_DIR/lib/art:$BUILD_DIR/bionic_build:$BUILD_DIR/atl_build:$LD_LIBRARY_PATH"
export RUN_FROM_BUILDDIR=1

# Resolve APK path before cd'ing into the build dir, so relative paths passed
# on the command line still work.
args=()
for arg in "$@"; do
	if [[ -f "$arg" ]]; then
		args+=("$(realpath "$arg")")
	else
		args+=("$arg")
	fi
done

cd "$BUILD_DIR/atl_build"
exec ./android-translation-layer --sdk-int=28 "${args[@]}"

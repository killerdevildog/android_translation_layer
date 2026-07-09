#!/bin/sh
# Thin wrapper that sets LD_LIBRARY_PATH so android-translation-layer
# picks up the in-tree libandroid.so and libtranslation_layer_main.so.
#
# Env vars:
#   ATL_SSL_DEBUG=1   -> enable wolfSSL JSSE debug output (TLS handshake, cert load)
set -e

ATL_ROOT="$(cd "$(dirname "$0")" && pwd)"
ATL_INSTALL="$ATL_ROOT/build/install"

export LD_LIBRARY_PATH="$ATL_INSTALL/lib:$ATL_INSTALL/lib/java/dex/android_translation_layer/natives:$ATL_ROOT/build/bionic_build:$ATL_ROOT/build/lib:${LD_LIBRARY_PATH}"

EXTRA_OPTS=""
if [ "${ATL_SSL_DEBUG:-0}" = "1" ]; then
    EXTRA_OPTS="-X -Dwolfjsse.debug=true -X -Djavax.net.debug=ssl:handshake"
fi

exec "$ATL_INSTALL/bin/android-translation-layer" $EXTRA_OPTS "$@"

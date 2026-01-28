#!/bin/sh
LIBC=$1
ARCH=$2
TXT_PATH=$3
COMMON=$(tr '\n' ':' < "${TXT_PATH}/common.txt")
LIBC_SPECIFIC=$(tr '\n' ':' < "${TXT_PATH}/${LIBC}_all_common.txt")
ARCH_SPECIFIC=$(tr '\n' ':' < "${TXT_PATH}/${LIBC}_${ARCH}_passing.txt")
echo "${COMMON}:${LIBC_SPECIFIC}:${ARCH_SPECIFIC}"

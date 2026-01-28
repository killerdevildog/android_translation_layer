/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef _LINKER_SLEB128_H
#define _LINKER_SLEB128_H

#include <limits.h>
#include <stddef.h>
#include <stdint.h>
#include <stdlib.h>

#include "linker_debug.h"

// Helper classes for decoding LEB128, used in packed relocation data.
// http://en.wikipedia.org/wiki/LEB128

/* current_ passed by reference so it can be advanced */
__attribute__((always_inline)) inline size_t sleb128_decoder_pop_front(const uint8_t** current_, const uint8_t* const end_) {
	size_t value = 0;
	static const size_t size = CHAR_BIT * sizeof(value);

	size_t shift = 0;
	uint8_t byte;

	do {
		if (*current_ >= end_) {
			PRINT("sleb128_decoder ran out of bounds");
			exit(1);
		}
		byte = *(*current_)++;
		value |= ((size_t)(byte & 127) << shift);
		shift += 7;
	} while (byte & 128);

	if (shift < size && (byte & 64)) {
		value |= -((size_t)(1) << shift);
	}

	return value;
}

#endif // __LINKER_SLEB128_H

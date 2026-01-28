/*
 * Copyright (C) 2021 The Android Open Source Project
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

#include <android-base/macros.h>
#include <gtest/gtest.h>

#include "nativebridge/native_bridge.h"

namespace android {

class NativeBridgeApiTest : public ::testing::Test {};

// Test the exported API in libnativebridge and libnativebridge_lazy.
// The testing we can do here is limited since there's no exported API to
// actually load the native bridge, but we only need to test the trivial
// wrappers.

TEST_F(NativeBridgeApiTest, NeedsNativeBridge) {
  EXPECT_FALSE(NeedsNativeBridge(ABI_STRING));
}

TEST_F(NativeBridgeApiTest, PreInitializeNativeBridge) {
  EXPECT_FALSE(PreInitializeNativeBridge(nullptr, ""));
}

TEST_F(NativeBridgeApiTest, NativeBridgeAvailable) {
  EXPECT_FALSE(NativeBridgeAvailable());
}

TEST_F(NativeBridgeApiTest, NativeBridgeInitialized) {
  EXPECT_FALSE(NativeBridgeInitialized());
}

TEST_F(NativeBridgeApiTest, NativeBridgeGetTrampoline) {
  EXPECT_EQ(nullptr, NativeBridgeGetTrampoline(nullptr, nullptr, nullptr, 0));
}

TEST_F(NativeBridgeApiTest, NativeBridgeGetError) {
  EXPECT_STREQ("native bridge is not initialized", NativeBridgeGetError());
}

};  // namespace android

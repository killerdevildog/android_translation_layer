# Android Translation Layer - Fork Roadmap

This document outlines the development roadmap for this fork of Android Translation Layer.
The goal is to make ATL easier to build, run, and extend while maintaining coherence with the upstream project.

---

## Phase 1: Build System Wrapper (Current State - Workaround)

### What We Have Now
A CMake orchestrator that wraps multiple different build systems:
- **wolfSSL**: Autotools (./configure && make)
- **libunwind**: CMake (native)
- **bionic_translation**: Meson
- **art_standalone**: Custom Makefile
- **android_translation_layer**: Meson

### Current Limitations
- ⚠️ Not a true unified build - just shell command wrappers
- ⚠️ ExternalProject_Add with hardcoded commands
- ⚠️ Fragile dependency on system tools (autoconf, meson, ninja)
- ⚠️ No proper CMake target exports between components
- ⚠️ Rebuilds don't always detect changes correctly

### What Works
- [x] Single command initiates build: `cmake -B build && cmake --build build`
- [x] Dependencies bundled in `thirdparty/`
- [x] Build order enforced: wolfSSL → libunwind → bionic_translation → art_standalone → ATL
- [x] pkg-config files generated for dependency resolution

---

## Phase 2: True Unified CMake Build System (Priority: HIGH)

### Goals
Convert ALL components to build natively with CMake - no shell wrappers, no Meson, no autotools.

### 2.1 wolfSSL → CMake Native
- [ ] Use wolfSSL's built-in CMakeLists.txt
- [ ] Configure options: `WOLFSSL_TLS13`, `WOLFSSL_OPENSSLEXTRA`, etc.
- [ ] Export `wolfssl::wolfssl` CMake target
- [ ] Remove autotools dependency

### 2.2 libunwind → CMake Native  
- [x] Already uses CMake natively ✅
- [ ] Improve integration with `add_subdirectory()` instead of ExternalProject
- [ ] Export `unwind::unwind` CMake target

### 2.3 bionic_translation → CMake Port
- [ ] Port Meson build to CMakeLists.txt
- [ ] Handle C/assembly source compilation
- [ ] Export `bionic::translation` CMake target
- [ ] Remove Meson dependency for this component

### 2.4 art_standalone → CMake Port
- [ ] Port Makefile build to CMakeLists.txt
- [ ] Handle Java compilation with CMake's `UseJava` module
- [ ] Integrate dex compilation (d8/dx)
- [ ] Export `art::standalone` CMake target
- [ ] Remove Make dependency for this component

### 2.5 android_translation_layer → CMake Port
- [ ] Port Meson build to CMakeLists.txt
- [ ] Handle Java + JNI compilation
- [ ] GResource compilation for assets
- [ ] GTK4 integration via `find_package(PkgConfig)` + `pkg_check_modules`
- [ ] Export `atl::runtime` CMake target

### 2.6 Unified Build Result
- [ ] Single CMakeLists.txt at root
- [ ] All components use `add_subdirectory()`
- [ ] Proper CMake target dependencies (no ExternalProject)
- [ ] Incremental rebuilds work correctly
- [ ] `cmake --install` works for all components

### Success Criteria
```cmake
# This should be all that's needed:
cmake -B build
cmake --build build
cmake --install build
```

No autotools, no Meson, no Make - pure CMake.

---

## Phase 3: Simplified Runtime

### Goals
- [ ] **Eliminate wrapper scripts** - `atl ./app.apk` should just work
- [ ] Automatic library path resolution (no `LD_LIBRARY_PATH` needed)
- [ ] Install to system paths with `cmake --install build`
- [ ] Create `atl` symlink/wrapper that handles environment setup internally

### Tasks

#### 3.1 Runtime Library Resolution
- [ ] Embed RPATH in binaries during build
- [ ] Use `$ORIGIN` relative paths for portable builds
- [ ] Alternative: Create launcher binary that sets up environment before exec

#### 3.2 Installation Target
- [ ] Add CMake install rules for all components
- [ ] Install to `/usr/local/` by default (configurable with `CMAKE_INSTALL_PREFIX`)
- [ ] Install libraries to `lib/atl/` subdirectory
- [ ] Create `/usr/local/bin/atl` launcher script/binary

#### 3.3 Configuration
- [ ] Support `~/.config/atl/config` for user preferences
- [ ] Auto-detect SDK version from APK's `AndroidManifest.xml`
- [ ] Default to sensible SDK version (28+) without requiring `--sdk-int`

---

## Phase 4: Advanced App Support

### Goals
Expand API coverage to support more complex Android applications.

### 4.1 Missing Core APIs
- [ ] `android.content.pm.PackageInstaller` - App installation APIs
- [ ] `android.app.job.JobScheduler` - Background job scheduling
- [ ] `android.app.NotificationManager` - System notifications
- [ ] `android.accounts.AccountManager` - Account management
- [ ] `android.provider.Settings` - System settings access

### 4.2 UI Components
- [ ] `RecyclerView` - Efficient scrolling lists
- [ ] `ViewPager` / `ViewPager2` - Swipeable views
- [ ] `ConstraintLayout` - Modern layout system
- [ ] `CoordinatorLayout` - Material Design behaviors
- [ ] `BottomNavigationView` - Bottom navigation bars
- [ ] `DrawerLayout` - Navigation drawers
- [ ] `Toolbar` / `ActionBar` - App bars

### 4.3 Media & Graphics
- [ ] `MediaPlayer` improvements - More codec support
- [ ] `ExoPlayer` compatibility - Modern media playback
- [ ] `Camera2` API - Camera access
- [ ] `OpenGL ES 3.1+` features
- [ ] `Vulkan` passthrough improvements

### 4.4 Storage & Database
- [ ] `Room` database compatibility
- [ ] `ContentProvider` improvements
- [ ] Scoped storage emulation
- [ ] SAF (Storage Access Framework)

### 4.5 Networking
- [ ] `OkHttp` compatibility improvements
- [ ] `Retrofit` compatibility
- [ ] WebSocket support
- [ ] HTTP/2 support via wolfSSL

### 4.6 Services & Background
- [ ] `Service` lifecycle improvements
- [ ] `IntentService` / `JobIntentService`
- [ ] `WorkManager` compatibility
- [ ] Foreground service notifications

---

## Phase 5: Developer Experience

### Goals
Make development and debugging easier.

### 5.1 Logging & Debugging
- [ ] Improved logcat-style output
- [ ] GDB/LLDB debugging documentation
- [ ] Crash report generation
- [ ] ANR (App Not Responding) detection

### 5.2 Testing Infrastructure
- [ ] Automated test suite for API compatibility
- [ ] CI/CD pipeline with GitHub Actions
- [ ] APK compatibility matrix
- [ ] Performance benchmarks

### 5.3 Documentation
- [ ] API implementation status tracker
- [ ] Troubleshooting guide
- [ ] App porting guide
- [ ] Architecture deep-dive

---

## Phase 6: Upstream Coherence

### Goals
Stay synchronized with upstream development while maintaining fork improvements.

### 6.1 Sync Strategy
- [ ] Regular merges from upstream `master`
- [ ] Document fork-specific changes clearly
- [ ] Contribute improvements back upstream where appropriate
- [ ] Maintain compatibility with upstream build system

### 6.2 Divergence Points
Document intentional differences from upstream:
- Bundled thirdparty dependencies (vs external)
- CMake build system (vs Meson-only)
- Simplified runtime (vs manual LD_LIBRARY_PATH)

### 6.3 Contribution Back
Consider upstreaming:
- Bug fixes
- New API implementations
- Documentation improvements
- Build system improvements (if accepted)

---

## Phase 7: Platform Support

### Goals
Expand platform compatibility beyond x86_64 Linux.

### 7.1 Architecture Support
- [ ] ARM64 (aarch64) - Priority for ARM laptops
- [ ] ARM32 - For older devices
- [ ] x86 (32-bit) - Legacy support

### 7.2 Distribution Packaging
- [ ] Debian/Ubuntu `.deb` packages
- [ ] Fedora/RHEL `.rpm` packages
- [ ] Arch Linux `PKGBUILD`
- [ ] Flatpak package
- [ ] AppImage portable build

### 7.3 Alternative Platforms
- [ ] macOS support (long-term)
- [ ] BSD support (experimental)

---

## Priority Matrix

| Phase | Priority | Effort | Impact | Status |
|-------|----------|--------|--------|--------|
| Phase 1: Build Wrapper | - | - | Medium | ⚠️ Workaround |
| Phase 2: True CMake Build | **Critical** | Very High | Very High | Not Started |
| Phase 3: Simplified Runtime | High | Medium | High | Not Started |
| Phase 4: Advanced App Support | High | Very High | Very High | Not Started |
| Phase 5: Developer Experience | Medium | Medium | Medium | Not Started |
| Phase 6: Upstream Coherence | Ongoing | Low | Medium | Ongoing |
| Phase 7: Platform Support | Low | High | Medium | Not Started |

---

## Quick Wins (Low Effort, High Impact)

1. **Auto-detect SDK from APK** - Parse `AndroidManifest.xml` for `minSdkVersion`
2. **RPATH embedding** - Add `-Wl,-rpath,$ORIGIN/../lib` to link flags
3. **Desktop file generator** - Improve `--install` to create proper icons
4. **Error messages** - Better "Class not found" messages with suggestions

---

## Known Limitations

Current limitations that need addressing:

1. **SDK Version** - Many apps require SDK 21+ but detection is manual
2. **Google Play Services** - Not implemented, breaks many apps
3. **Native ARM libraries** - No ARM emulation, x86 only
4. **Fragments** - Basic support, complex navigation may fail
5. **WebView** - WebKitGTK works but some features missing

---

## Contributing

See the main [README.md](README.md) for contribution guidelines.

Priority areas for contributions:
1. API stubs for popular apps
2. Widget implementations (GTK4)
3. Test APK compatibility reports
4. Documentation improvements

---

## Version Goals

### v0.1 - Current State (Workaround Build)
- Build orchestrator wrapping multiple build systems ⚠️
- Bundled dependencies ✅
- Basic app support (Hello World works) ✅
- Requires wrapper script to run ⚠️

### v1.0 - True Unified Build
- Pure CMake build for all components
- No autotools, Meson, or Make dependencies
- `cmake -B build && cmake --build build` - that's it
- Proper incremental rebuilds

### v1.1 - Simple Runtime
- No wrapper scripts needed: `atl ./app.apk`
- System installation support: `cmake --install build`
- Auto SDK detection from APK manifest
- RPATH-based library resolution

### v1.2 - Enhanced Compatibility
- RecyclerView support
- Improved fragment handling
- Better notification support
- More Material Design widgets

### v2.0 - Production Ready
- Comprehensive API coverage
- ARM64 support
- Distribution packages (.deb, .rpm, Flatpak)
- Stable API for app developers

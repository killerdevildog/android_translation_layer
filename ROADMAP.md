# Android Translation Layer - Fork Roadmap

This document outlines the development roadmap for this fork of Android Translation Layer.
The goal is to make ATL easier to build, run, and extend while maintaining coherence with the upstream project.

---

## Phase 1: Unified Build System ✅ (Completed)

### Goals
- [x] Bundle all dependencies in `thirdparty/` folder
- [x] Create unified CMake build orchestrator
- [x] Build order: wolfSSL → libunwind → bionic_translation → art_standalone → ATL
- [x] Single command build: `cmake -B build && cmake --build build`
- [x] Generate proper pkg-config files for dependency resolution

### Current State
All thirdparty dependencies are now bundled and build automatically with CMake.

---

## Phase 2: Simplified Runtime (In Progress)

### Goals
- [ ] **Eliminate wrapper scripts** - `atl ./app.apk` should just work
- [ ] Automatic library path resolution (no `LD_LIBRARY_PATH` needed)
- [ ] Install to system paths with `cmake --install build`
- [ ] Create `atl` symlink/wrapper that handles environment setup internally

### Tasks

#### 2.1 Runtime Library Resolution
- [ ] Embed RPATH in binaries during build
- [ ] Use `$ORIGIN` relative paths for portable builds
- [ ] Alternative: Create launcher binary that sets up environment before exec

#### 2.2 Installation Target
- [ ] Add CMake install rules for all components
- [ ] Install to `/usr/local/` by default (configurable with `CMAKE_INSTALL_PREFIX`)
- [ ] Install libraries to `lib/atl/` subdirectory
- [ ] Create `/usr/local/bin/atl` launcher script/binary

#### 2.3 Configuration
- [ ] Support `~/.config/atl/config` for user preferences
- [ ] Auto-detect SDK version from APK's `AndroidManifest.xml`
- [ ] Default to sensible SDK version (28+) without requiring `--sdk-int`

---

## Phase 3: CMake-Native Thirdparty Builds

### Goals
Convert all thirdparty dependencies to build natively with CMake (no shell commands).

### Tasks

#### 3.1 wolfSSL
- [ ] Use wolfSSL's native CMake build system
- [ ] Configure with proper options for ATL requirements
- [ ] Export targets for downstream consumption

#### 3.2 libunwind  
- [ ] Use libunwind's CMakeLists.txt directly
- [ ] Configure for local (non-ptrace) unwinding only
- [ ] Static library preferred for portability

#### 3.3 bionic_translation
- [ ] Create CMake wrapper around Meson build
- [ ] Or: Port bionic_translation build to pure CMake
- [ ] Proper target export with include directories

#### 3.4 art_standalone
- [ ] Create CMake wrapper around existing Makefile
- [ ] Or: Port art_standalone build to pure CMake  
- [ ] Handle Java compilation with CMake's Java support

#### 3.5 android_translation_layer
- [ ] Create CMake wrapper around Meson build
- [ ] Or: Port ATL build to pure CMake
- [ ] Unified single CMakeLists.txt for entire project

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

| Phase | Priority | Effort | Impact |
|-------|----------|--------|--------|
| Phase 1: Unified Build | ✅ Done | - | High |
| Phase 2: Simplified Runtime | High | Medium | High |
| Phase 3: CMake-Native Builds | Medium | High | Medium |
| Phase 4: Advanced App Support | High | Very High | Very High |
| Phase 5: Developer Experience | Medium | Medium | Medium |
| Phase 6: Upstream Coherence | Ongoing | Low | Medium |
| Phase 7: Platform Support | Low | High | Medium |

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

### v1.0 - Stable Build
- Unified CMake build ✅
- Bundled dependencies ✅
- Basic app support ✅

### v1.1 - Simple Runtime
- No wrapper scripts needed
- System installation support
- Auto SDK detection

### v1.2 - Enhanced Compatibility
- RecyclerView support
- Improved fragment handling
- Better notification support

### v2.0 - Production Ready
- Comprehensive API coverage
- ARM64 support
- Distribution packages

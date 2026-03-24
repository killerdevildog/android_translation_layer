The following environment variables are recognized by the main executable:

`JDWP_LISTEN=<port>` - if set, art will listen for a jdb connection at `<port>`

---

`RUN_FROM_BUILDDIR=` - if set, will search for `api-impl.jar` and `libtranslation_layer.so` in current working directory

                       (may need `LD_LIBRARY_PATH=.` as well for `libandroid.so.0`)

---

`ANDROID_APP_DATA_DIR=<path>` - if set, overrides the default path of `~/.local/android_translation_layer` for storing app data

---

`ATL_DISABLE_WINDOW_DECORATIONS=` - if set, window decorations will be disabled; 
                                    this is useful for saving screen space on phone screens

---

`ATL_UGLY_ENABLE_LOCATION=` - if set, apps will be able to get location data using the relevant android APIs. (TODO: use bubblewrap)  

---

`ATL_UGLY_ENABLE_MICROPHONE=` - if set, apps will be able record microphone audio using the relevant android APIs. (TODO: use bubblewrap)

---

`ATL_UGLY_ENABLE_WEBVIEW=` - if not set, WebView will be stubbed as a generic View; this will avoid
                             wasting resources on WebViews which are only used for fingerprinting and ads

                             (set this for apps that use WebView for it's intended purpose)

---

`ATL_FORCE_FULLSCREEN` - if set, will fullscreen the app window on start;
                         this is useful for saving screen space on phone screens,
                         as well as making apps that can't handle arbitrary screen dimensions
                         for some reason happier (may need gamescope if the hardcoded resolution
                         doesn't match your device)

---

`ATL_IS_AUTOMOTIVE` - if set, when an app checks if it's running in a vehicle, ATL will return true.

---

`ATL_IS_TELEVISION` - if set, when an app checks if it's running on a television, ATL will return true.

---

`ATL_IS_WATCH` - if set, when an app checks if it's running on a watch, ATL will return true.

---

`ATL_SKIP_NATIVES_EXTRACTION` - if set, natives will not be extracted automatically;
                                it's already possible to replace a native lib, but removing it entirely will normally result
                                in it getting re-extracted, which would prevent you from replacing libs with native ones
                                (since bionic_translation linker considers everything inside the app's lib dir non-native)

---

`ATL_DIRECT_EGL` - if set, SurfaceViews will be mapped directly to a Wayland subsurface or X11 window
                   instead of using GtkGraphicsOffload. This might be beneficial for CPU usage and rendering latency,
                   but (on Gtk < 4.22 and on X11) does not allow the application to render other Views on top of
                   the SurfaceView

                   On Gtk >= 4.22, we punch a hole through the Gtk scene graph and (on Wayland) put the native surface below the Gtk window.

---

`ATL_VALIDATE_CERTS` - if set, the signing certificate of the APK file will be validated on startup. This adds a few extra seconds to the startup time for large APKs.

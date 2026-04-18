
#include <EGL/egl.h>
#include <GL/gl.h>
#include <X11/Xlib.h>
#include <gtk/gtk.h>
#include <jni.h>

struct ANativeWindow {
	EGLNativeWindowType egl_window;
	GtkWidget *surface_view_widget;
	struct wl_display *wayland_display;
	struct wl_surface *wayland_surface;
	Display *x11_display;
	gulong resize_handler;
	int refcount;
	int width;
	int height;
};

struct ANativeWindow *ANativeWindow_fromSurface(JNIEnv *env, jobject surface);
EGLSurface bionic_eglCreateWindowSurface(EGLDisplay display, EGLConfig config, struct ANativeWindow *native_window, EGLint const *attrib_list);
EGLBoolean bionic_eglDestroySurface(EGLDisplay display, EGLSurface surface);
EGLDisplay bionic_eglGetDisplay(NativeDisplayType native_display);
EGLBoolean bionic_eglMakeCurrent(EGLDisplay display, EGLSurface draw, EGLSurface read, EGLContext context);
EGLBoolean bionic_eglSwapBuffers(EGLDisplay display, EGLSurface surface);
void bionic_glBindFramebuffer(GLenum target, GLuint framebuffer);
void ANativeWindow_acquire(struct ANativeWindow *native_window);
void ANativeWindow_release(struct ANativeWindow *native_window);

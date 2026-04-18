#include <gdk/wayland/gdkwayland.h>
#include <gdk/x11/gdkx.h>
#include <gtk/gtk.h>

#include <wayland-egl.h>

#include <EGL/egl.h>
#include <EGL/eglext.h>

#include <GL/gl.h>
#include <GLES2/gl2.h>

// FIXME: put the header in a common place
#include "../api-impl-jni/defines.h"
#include "../api-impl-jni/widgets/android_view_SurfaceView.h"

#include "native_window.h"

extern GtkWindow *window; // TODO: how do we get rid of this? the app won't pass anything useful to eglGetDisplay

static GHashTable *egl_surface_hashtable;

// temporary for debugging
static void PrintConfigAttributes(EGLDisplay display, EGLConfig config)
{
	EGLint value;
	printf("-------------------------------------------------------------------------------\n");
	eglGetConfigAttrib(display, config, EGL_CONFIG_ID, &value);
	printf("EGL_CONFIG_ID %d\n", value);

	eglGetConfigAttrib(display, config, EGL_BUFFER_SIZE, &value);
	printf("EGL_BUFFER_SIZE %d\n", value);
	eglGetConfigAttrib(display, config, EGL_RED_SIZE, &value);
	printf("EGL_RED_SIZE %d\n", value);
	eglGetConfigAttrib(display, config, EGL_GREEN_SIZE, &value);
	printf("EGL_GREEN_SIZE %d\n", value);
	eglGetConfigAttrib(display, config, EGL_BLUE_SIZE, &value);
	printf("EGL_BLUE_SIZE %d\n", value);
	eglGetConfigAttrib(display, config, EGL_ALPHA_SIZE, &value);
	printf("EGL_ALPHA_SIZE %d\n", value);
	eglGetConfigAttrib(display, config, EGL_DEPTH_SIZE, &value);
	printf("EGL_DEPTH_SIZE %d\n", value);
	eglGetConfigAttrib(display, config, EGL_STENCIL_SIZE, &value);
	printf("EGL_STENCIL_SIZE %d\n", value);
	eglGetConfigAttrib(display, config, EGL_SAMPLE_BUFFERS, &value);
	printf("EGL_SAMPLE_BUFFERS %d\n", value);
	eglGetConfigAttrib(display, config, EGL_SAMPLES, &value);
	printf("EGL_SAMPLES %d\n", value);

	eglGetConfigAttrib(display, config, EGL_CONFIG_CAVEAT, &value);
	switch (value) {
		case EGL_NONE:
			printf("EGL_CONFIG_CAVEAT EGL_NONE\n");
			break;
		case EGL_SLOW_CONFIG:
			printf("EGL_CONFIG_CAVEAT EGL_SLOW_CONFIG\n");
			break;
	}

	eglGetConfigAttrib(display, config, EGL_MAX_PBUFFER_WIDTH, &value);
	printf("EGL_MAX_PBUFFER_WIDTH %d\n", value);
	eglGetConfigAttrib(display, config, EGL_MAX_PBUFFER_HEIGHT, &value);
	printf("EGL_MAX_PBUFFER_HEIGHT %d\n", value);
	eglGetConfigAttrib(display, config, EGL_MAX_PBUFFER_PIXELS, &value);
	printf("EGL_MAX_PBUFFER_PIXELS %d\n", value);
	eglGetConfigAttrib(display, config, EGL_NATIVE_RENDERABLE, &value);
	printf("EGL_NATIVE_RENDERABLE %s \n", (value ? "true" : "false"));
	eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &value);
	printf("EGL_NATIVE_VISUAL_ID %d\n", value);
	eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_TYPE, &value);
	printf("EGL_NATIVE_VISUAL_TYPE %d\n", value);
	eglGetConfigAttrib(display, config, EGL_RENDERABLE_TYPE, &value);
	printf("EGL_RENDERABLE_TYPE %d\n", value);
	eglGetConfigAttrib(display, config, EGL_SURFACE_TYPE, &value);
	printf("EGL_SURFACE_TYPE %d\n", value);
	eglGetConfigAttrib(display, config, EGL_TRANSPARENT_TYPE, &value);
	printf("EGL_TRANSPARENT_TYPE %d\n", value);
	printf("-------------------------------------------------------------------------------\n");
}

// this is an extension that only android implements, we can hopefully get away with just stubbing it
EGLBoolean bionic_eglPresentationTimeANDROID(EGLDisplay dpy, EGLSurface surface, EGLnsecsANDROID time)
{
	return EGL_TRUE;
}

void (*bionic_eglGetProcAddress(char const *procname))(void)
{
	if (__unlikely__(!strcmp(procname, "eglPresentationTimeANDROID")))
		return (void (*)(void))bionic_eglPresentationTimeANDROID;

	return eglGetProcAddress(procname);
}

EGLDisplay bionic_eglGetDisplay(EGLNativeDisplayType native_display)
{
	/*
	 * On android, at least SDL passes 0 (EGL_DISPLAY_DEFAULT) to eglGetDisplay and uses the resulting display.
	 * We obviously want to make the app use the correct display, which may happen to be a different one
	 * than the "default" display (especially on Wayland)
	 */
	GdkDisplay *display = gtk_root_get_display(GTK_ROOT(window));

	if (GDK_IS_WAYLAND_DISPLAY(display)) {
		struct wl_display *wl_display = gdk_wayland_display_get_wl_display(display);
		return eglGetPlatformDisplay(EGL_PLATFORM_WAYLAND_KHR, wl_display, NULL);
	} else if (GDK_IS_X11_DISPLAY(display)) {
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wdeprecated-declarations"
		Display *x11_display = gdk_x11_display_get_xdisplay(display);
#pragma GCC diagnostic pop
		return eglGetPlatformDisplay(EGL_PLATFORM_X11_KHR, x11_display, NULL);
	} else {
		return NULL;
	}
}

EGLBoolean bionic_eglChooseConfig(EGLDisplay display, EGLint *attrib_list, EGLConfig *configs, EGLint config_size, EGLint *num_config)
{
	GdkDisplay *gdk_display = gtk_root_get_display(GTK_ROOT(window));

	if (GDK_IS_X11_DISPLAY(gdk_display)) {
		/* X11 supports pbuffers just fine */
		return eglChooseConfig(display, attrib_list, configs, config_size, num_config);
	} else {
		bool has_pbuffer_bit = false;
		int attrib_list_size = 0;
		for (EGLint *attr = attrib_list; *attr != EGL_NONE; attr += 2) {
			if (*attr == EGL_SURFACE_TYPE && (*(attr + 1) & EGL_PBUFFER_BIT) && *(attr + 1) != EGL_DONT_CARE) {
				has_pbuffer_bit = true;
			}
			attrib_list_size += 2;
		}
		attrib_list_size += 1; // for EGL_NONE
		if (has_pbuffer_bit) {
			/* copy the list in case it's mapped read-only */
			EGLint *new_attrib_list = malloc(sizeof(EGLint) * attrib_list_size);
			memcpy(new_attrib_list, attrib_list, sizeof(EGLint) * attrib_list_size);
			for (EGLint *attr = new_attrib_list; *attr != EGL_NONE; attr += 2) {
				if (*attr == EGL_SURFACE_TYPE && *(attr + 1) != EGL_DONT_CARE) {
					*(attr + 1) &= ~EGL_PBUFFER_BIT;
					*(attr + 1) |= EGL_WINDOW_BIT;
				}
			}
			EGLBoolean ret = eglChooseConfig(display, new_attrib_list, configs, config_size, num_config);
			free(new_attrib_list);
			return ret;
		} else {
			return eglChooseConfig(display, attrib_list, configs, config_size, num_config);
		}
	}
}

EGLSurface bionic_eglCreatePbufferSurface(EGLDisplay display, EGLConfig config, EGLint const *attrib_list)
{
	GdkDisplay *gdk_display = gtk_root_get_display(GTK_ROOT(window));

	if (GDK_IS_X11_DISPLAY(gdk_display)) {
		/* X11 supports pbuffers just fine */
		return eglCreatePbufferSurface(display, config, attrib_list);
	} else {
		struct wl_compositor *wl_compositor = gdk_wayland_display_get_wl_compositor(gdk_display);
		struct wl_surface *wayland_surface = wl_compositor_create_surface(wl_compositor);
		EGLint width = 0;
		EGLint height = 0;
		EGLint *new_attrib_list = NULL;
		if (attrib_list) {
			size_t attrib_list_len = 0;
			for (EGLint *attr = (EGLint *)attrib_list; *attr != EGL_NONE; attr++)
				attrib_list_len++;
			new_attrib_list = malloc(attrib_list_len);
			EGLint *new_attr_pos = new_attrib_list;
			for (EGLint *attr = (EGLint *)attrib_list; *attr != EGL_NONE; attr += 2) {
				if (*attr == EGL_WIDTH) {
					width = *(attr + 1);
				} else if (*attr == EGL_HEIGHT) {
					height = *(attr + 1);
				} else {
					*new_attr_pos = *attr;
					*(new_attr_pos + 1) = *(attr + 1);
					new_attr_pos += 2;
				}
			}
			*new_attr_pos = EGL_NONE;
		}
		struct wl_egl_window *egl_window = wl_egl_window_create(wayland_surface, width, height);
		EGLSurface surface = eglCreateWindowSurface(display, config, (EGLNativeWindowType)egl_window, new_attrib_list);
		return surface;
	}
}

#define NUM_BUFFERS 3

struct _ATLSurface {
	GObject parent;
	int width;
	int height;
	SurfaceViewWidget *surface_view_widget;
	int32_t framebuffer_format;
	int32_t renderbuffer_format;
	unsigned int renderbuffer_attachment;
	uint32_t renderbuffer;
	struct atl_surface_buffer {
		struct _ATLSurface *surface;
		EGLImage egl_image;
		GdkGLTextureBuilder *texture_builder;
		uint32_t gl_texture;
		uint32_t gl_framebuffer;
	} buffers[NUM_BUFFERS];
	struct atl_surface_buffer *back_buffer;
	struct atl_surface_buffer *front_buffer;
	GAsyncQueue *vsync;
	GAsyncQueue *unused_buffers;
	gboolean destroyed;
};
G_DECLARE_FINAL_TYPE(ATLSurface, atl_surface, ATL, SURFACE, GObject);
static void atl_surface_dispose(GObject *object)
{
	ATLSurface *atl_surface = ATL_SURFACE(object);
	GdkGLContext *gtk_gl_context = gdk_surface_create_gl_context(gtk_native_get_surface(gtk_widget_get_native(GTK_WIDGET(atl_surface->surface_view_widget))), NULL);
	gdk_gl_context_make_current(gtk_gl_context);
	for (int i = 0; i < NUM_BUFFERS; i++) {
		if (atl_surface->buffers[i].texture_builder) {
			GLuint texture_id = gdk_gl_texture_builder_get_id(atl_surface->buffers[i].texture_builder);
			glDeleteTextures(1, &texture_id);
			g_object_unref(atl_surface->buffers[i].texture_builder);
		}
	}
	g_async_queue_unref(atl_surface->unused_buffers);
	g_async_queue_unref(atl_surface->vsync);
	g_object_unref(atl_surface->surface_view_widget);
}
static void atl_surface_class_init(ATLSurfaceClass *class)
{
	G_OBJECT_CLASS(class)->dispose = atl_surface_dispose;
}
static void atl_surface_init(ATLSurface *self) {}
G_DEFINE_TYPE(ATLSurface, atl_surface, G_TYPE_OBJECT)

EGLSurface bionic_eglCreateWindowSurface(EGLDisplay display, EGLConfig config, struct ANativeWindow *native_window, EGLint const *attrib_list)
{
	// better than crashing (TODO: check if apps try to use the NULL value anyway)
	if (!native_window)
		return NULL;

	if (!egl_surface_hashtable)
		egl_surface_hashtable = g_hash_table_new(NULL, NULL);

	ANativeWindow_acquire(native_window);

	PrintConfigAttributes(display, config);
	EGLSurface surface;
	if (getenv("ATL_DIRECT_EGL")) {
		surface = eglCreateWindowSurface(display, config, native_window->egl_window, attrib_list);
	} else {
		ATLSurface *atl_surface = g_object_new(atl_surface_get_type(), NULL);
		atl_surface->width = native_window->width;
		atl_surface->height = native_window->height;
		atl_surface->surface_view_widget = SURFACE_VIEW_WIDGET(g_object_ref(native_window->surface_view_widget));
		EGLint alpha_size = 0;
		EGLint depth_size = 0;
		EGLint stencil_size = 0;
		eglGetConfigAttrib(display, config, EGL_ALPHA_SIZE, &alpha_size);
		eglGetConfigAttrib(display, config, EGL_DEPTH_SIZE, &depth_size);
		eglGetConfigAttrib(display, config, EGL_STENCIL_SIZE, &stencil_size);
		if (alpha_size > 0) {
			atl_surface->framebuffer_format = GL_RGBA;
		} else {
			atl_surface->framebuffer_format = GL_RGB;
		}
		if (depth_size > 0 && stencil_size > 0) {
			atl_surface->renderbuffer_format = GL_DEPTH24_STENCIL8;
			atl_surface->renderbuffer_attachment = GL_DEPTH_STENCIL_ATTACHMENT;
		} else if (depth_size > 0) {
			atl_surface->renderbuffer_format = GL_DEPTH_COMPONENT16;
			atl_surface->renderbuffer_attachment = GL_DEPTH_ATTACHMENT;
		} else if (stencil_size > 0) {
			atl_surface->renderbuffer_format = GL_STENCIL_INDEX8;
			atl_surface->renderbuffer_attachment = GL_STENCIL_ATTACHMENT;
		}
		surface = atl_surface;
	}

	printf("EGL::: native_window->egl_window: %p\n", native_window->egl_window);
	printf("EGL::: eglGetError: %d\n", eglGetError());

	printf("EGL::: ret: %p\n", surface);

	g_hash_table_insert(egl_surface_hashtable, surface, native_window);

	if (!surface)
		ANativeWindow_release(native_window);

	return surface;
}

EGLBoolean bionic_eglDestroySurface(EGLDisplay display, EGLSurface surface)
{
	struct ANativeWindow *native_window = g_hash_table_lookup(egl_surface_hashtable, surface);

	if (!native_window)
		return eglDestroySurface(display, surface);

	EGLBoolean ret = EGL_TRUE;
	if (getenv("ATL_DIRECT_EGL")) {
		ret = eglDestroySurface(display, surface);
	} else {
		ATLSurface *atl_surface = surface;
		atl_surface->destroyed = TRUE;
		for (int i = 0; i < NUM_BUFFERS; i++) {
			if (atl_surface->buffers[i].egl_image)
				eglDestroyImage(display, atl_surface->buffers[i].egl_image);
			atl_surface->buffers[i].egl_image = 0;
			if (atl_surface->buffers[i].gl_framebuffer)
				glDeleteFramebuffers(1, &atl_surface->buffers[i].gl_framebuffer);
			if (atl_surface->buffers[i].gl_texture)
				glDeleteTextures(1, &atl_surface->buffers[i].gl_texture);
		}
		g_object_unref(atl_surface);
	}
	if (ret) {
		g_hash_table_remove(egl_surface_hashtable, surface);
		ANativeWindow_release(native_window);
	}

	return ret;
}

static GHashTable *draw_surface_hashtable = NULL;
static GHashTable *read_surface_hashtable = NULL;

EGLBoolean bionic_eglMakeCurrent(EGLDisplay display, EGLSurface draw, EGLSurface read, EGLContext context)
{
	if (getenv("ATL_DIRECT_EGL"))
		return eglMakeCurrent(display, draw, read, context);
	struct ANativeWindow *native_window = g_hash_table_lookup(egl_surface_hashtable, draw);
	if (!native_window) {
		return eglMakeCurrent(display, draw, read, context);
	}
	if (!draw_surface_hashtable)
		draw_surface_hashtable = g_hash_table_new(NULL, NULL);
	g_hash_table_insert(draw_surface_hashtable, context, draw);
	if (!read_surface_hashtable)
		read_surface_hashtable = g_hash_table_new(NULL, NULL);
	g_hash_table_insert(read_surface_hashtable, context, read);
	EGLBoolean ret = eglMakeCurrent(display, NULL, NULL, context);
	ATLSurface *atl_surface = draw;
	if (!atl_surface->unused_buffers) {
		atl_surface->vsync = g_async_queue_new();
		g_async_queue_push(atl_surface->vsync, GINT_TO_POINTER(1));
		atl_surface->unused_buffers = g_async_queue_new();
		GLint previousTexture;
		glGetIntegerv(GL_TEXTURE_BINDING_2D, &previousTexture); // Save current binding
		if (atl_surface->renderbuffer_format) {
			GLint previous_renderbuffer;
			glGetIntegerv(GL_RENDERBUFFER_BINDING, &previous_renderbuffer);
			glGenRenderbuffers(1, &atl_surface->renderbuffer);
			glBindRenderbuffer(GL_RENDERBUFFER, atl_surface->renderbuffer);
			glRenderbufferStorage(GL_RENDERBUFFER, atl_surface->renderbuffer_format, native_window->width, native_window->height);
			glBindRenderbuffer(GL_RENDERBUFFER, previous_renderbuffer);
		}

		for (int i = 0; i < NUM_BUFFERS; i++) {
			atl_surface->buffers[i].surface = atl_surface;
			glGenTextures(1, &atl_surface->buffers[i].gl_texture);
			glBindTexture(GL_TEXTURE_2D, atl_surface->buffers[i].gl_texture);
			glTexImage2D(GL_TEXTURE_2D, 0, atl_surface->framebuffer_format, native_window->width, native_window->height, 0, atl_surface->framebuffer_format, GL_UNSIGNED_BYTE, NULL);

			// Create EGLImage from texture
			atl_surface->buffers[i].egl_image = eglCreateImage(eglGetCurrentDisplay(),
			                                                   eglGetCurrentContext(), // The current context
			                                                   EGL_GL_TEXTURE_2D,
			                                                   (EGLClientBuffer)(uintptr_t)atl_surface->buffers[i].gl_texture,
			                                                   NULL);

			glGenFramebuffers(1, &atl_surface->buffers[i].gl_framebuffer);
			glBindFramebuffer(GL_FRAMEBUFFER, atl_surface->buffers[i].gl_framebuffer);
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, atl_surface->buffers[i].gl_texture, 0);
			if (atl_surface->renderbuffer)
				glFramebufferRenderbuffer(GL_FRAMEBUFFER, atl_surface->renderbuffer_attachment, GL_RENDERBUFFER, atl_surface->renderbuffer);
			g_async_queue_push(atl_surface->unused_buffers, &atl_surface->buffers[i]);
		}
		glBindTexture(GL_TEXTURE_2D, previousTexture);
	}
	if (!atl_surface->back_buffer) {
		atl_surface->back_buffer = g_async_queue_pop(atl_surface->unused_buffers);
	}
	glBindFramebuffer(GL_FRAMEBUFFER, atl_surface->back_buffer->gl_framebuffer);
	return ret;
}

/* runs on main thread */
static void destroy_texture(void *data)
{
	struct atl_surface_buffer *buffer = data;
	g_async_queue_push(buffer->surface->unused_buffers, buffer);
	g_object_unref(buffer->surface);
}

static void frame_callback(SurfaceViewWidget *surface_view_widget)
{
	g_async_queue_push(surface_view_widget->frame_callback_data, GINT_TO_POINTER(1));
}

static gboolean queue_texture(gpointer data)
{
	static PFNGLEGLIMAGETARGETTEXTURE2DOESPROC glEGLImageTargetTexture2DOES = NULL;
	ATLSurface *atl_surface = data;
	if (atl_surface->destroyed) {
		g_object_unref(atl_surface);
		return G_SOURCE_REMOVE;
	}
	struct atl_surface_buffer *buffer = atl_surface->front_buffer;
	if (!buffer->texture_builder) {
		GLuint texture_id;
		GdkGLContext *gtk_gl_context = gdk_surface_create_gl_context(gtk_native_get_surface(gtk_widget_get_native(GTK_WIDGET(atl_surface->surface_view_widget))), NULL);
		gdk_gl_context_make_current(gtk_gl_context);
		glGenTextures(1, &texture_id);
		glBindTexture(GL_TEXTURE_2D, texture_id);
		if (!glEGLImageTargetTexture2DOES)
			glEGLImageTargetTexture2DOES = (PFNGLEGLIMAGETARGETTEXTURE2DOESPROC)eglGetProcAddress("glEGLImageTargetTexture2DOES");
		glEGLImageTargetTexture2DOES(GL_TEXTURE_2D, buffer->egl_image);
		glBindTexture(GL_TEXTURE_2D, 0);
		eglDestroyImage(eglGetCurrentDisplay(), buffer->egl_image);
		buffer->egl_image = NULL;
		buffer->texture_builder = gdk_gl_texture_builder_new();
		gdk_gl_texture_builder_set_context(buffer->texture_builder, gtk_gl_context);
		gdk_gl_texture_builder_set_id(buffer->texture_builder, texture_id);
		gdk_gl_texture_builder_set_format(buffer->texture_builder, GDK_MEMORY_R8G8B8A8_PREMULTIPLIED);
		gdk_gl_texture_builder_set_width(buffer->texture_builder, atl_surface->width);
		gdk_gl_texture_builder_set_height(buffer->texture_builder, atl_surface->height);
		gdk_gl_context_clear_current();
	}
	atl_surface->surface_view_widget->frame_callback_data = atl_surface->vsync;
	atl_surface->surface_view_widget->frame_callback = frame_callback;
	surface_view_widget_set_texture(atl_surface->surface_view_widget, gdk_gl_texture_builder_build(buffer->texture_builder, destroy_texture, buffer), TRUE);
	// atl_surface reference will be dropped in destroy_texture
	return G_SOURCE_REMOVE;
}

EGLBoolean bionic_eglSwapBuffers(EGLDisplay display, EGLSurface surface)
{
	if (getenv("ATL_DIRECT_EGL"))
		return eglSwapBuffers(display, surface);
	struct ANativeWindow *native_window = g_hash_table_lookup(egl_surface_hashtable, surface);
	if (!native_window) {
		return eglSwapBuffers(display, surface);
	}
	ATLSurface *atl_surface = surface;

	glFlush();
	if (atl_surface->back_buffer) {
		g_async_queue_timeout_pop(atl_surface->vsync, 50000); // 50ms
		atl_surface->front_buffer = atl_surface->back_buffer;
		atl_surface->back_buffer = NULL;
		g_idle_add_full(G_PRIORITY_HIGH_IDLE + 20, queue_texture, g_object_ref(atl_surface), NULL);
	}
	atl_surface->back_buffer = g_async_queue_timeout_pop(atl_surface->unused_buffers, 100000); // 100ms
	if (atl_surface->back_buffer)
		glBindFramebuffer(GL_FRAMEBUFFER, atl_surface->back_buffer->gl_framebuffer);
	else
		glBindFramebuffer(GL_FRAMEBUFFER, 0);

	return EGL_TRUE;
}

EGLBoolean bionic_eglQuerySurface(EGLDisplay display, EGLSurface surface, EGLint attribute, EGLint *value)
{
	if (getenv("ATL_DIRECT_EGL"))
		return eglQuerySurface(display, surface, attribute, value);
	struct ANativeWindow *native_window = g_hash_table_lookup(egl_surface_hashtable, surface);
	if (!native_window)
		return eglQuerySurface(display, surface, attribute, value);
	if (attribute == EGL_WIDTH) {
		*value = native_window->width;
	} else if (attribute == EGL_HEIGHT) {
		*value = native_window->height;
	} else {
		printf("bionic_eglQuerySurface(%p, %p, %d, %p): attribute not implemented\n", display, surface, attribute, value);
		return eglQuerySurface(display, surface, attribute, value);
	}
	return EGL_TRUE;
}

EGLSurface bionic_eglGetCurrentSurface(EGLint readdraw)
{
	if (getenv("ATL_DIRECT_EGL"))
		return eglGetCurrentSurface(readdraw);
	EGLContext current_context = eglGetCurrentContext();
	if (readdraw == EGL_READ)
		return g_hash_table_lookup(read_surface_hashtable, current_context);
	else if (readdraw == EGL_DRAW)
		return g_hash_table_lookup(draw_surface_hashtable, current_context);

	return NULL;
}

void bionic_glBindFramebuffer(GLenum target, GLuint framebuffer)
{
	if (getenv("ATL_DIRECT_EGL") || framebuffer != 0)
		return glBindFramebuffer(target, framebuffer);
	ATLSurface *atl_surface = g_hash_table_lookup(draw_surface_hashtable, eglGetCurrentContext());
	return glBindFramebuffer(target, atl_surface ? atl_surface->back_buffer->gl_framebuffer : 0);
}

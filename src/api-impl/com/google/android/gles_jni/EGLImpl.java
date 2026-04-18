/*
 * parts of this file Copyright (C) 2006 The Android Open Source Project
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

package com.google.android.gles_jni;

import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import javax.microedition.khronos.egl.*;

public class EGLImpl implements EGL10 {
	private native long native_eglCreateContext(long egl_display, long egl_config, long share_context, int[] attrib_list);

	public EGLContext eglCreateContext(EGLDisplay display, EGLConfig config, EGLContext share_context, int[] attrib_list) {
		long native_egl_context = native_eglCreateContext(display.native_egl_display, config.native_egl_config, (share_context != null) ? share_context.native_egl_context : 0, attrib_list);
		if (native_egl_context == 0) {
			return EGL10.EGL_NO_CONTEXT;
		}
		return new EGLContext(native_egl_context);
	}

	private native boolean native_eglChooseConfig(long egl_display, int[] attrib_list, long[] egl_configs, int config_size, int[] num_config);

	public boolean eglChooseConfig(EGLDisplay display, int[] attrib_list, EGLConfig[] configs, int config_size, int[] num_config) {
		long[] native_egl_configs = null;

		if (config_size != 0) {
			if (configs == null) {
				throw new java.lang.IllegalArgumentException("app error: eglChooseConfig called with non-zero 'config_size' (" + config_size + "), but with null 'configs' array");
			}
			native_egl_configs = new long[config_size];
		}

		boolean ret = native_eglChooseConfig(display.native_egl_display, attrib_list, native_egl_configs, config_size, num_config);

		if (configs != null) {
			for (int i = 0; i < configs.length; i++) {
				configs[i] = new EGLConfig(native_egl_configs[i]);
			}
		}

		return ret;
	}

	public EGLSurface eglCreateWindowSurface(EGLDisplay display, EGLConfig config, Object native_window, int[] attrib_list) {
		Surface sur = null;
		if (native_window instanceof SurfaceView) {
			SurfaceView surfaceView = (SurfaceView)native_window;
			sur = surfaceView.getHolder().getSurface();
		} else if (native_window instanceof SurfaceHolder) {
			SurfaceHolder holder = (SurfaceHolder)native_window;
			sur = holder.getSurface();
		} else if (native_window instanceof Surface) {
			sur = (Surface)native_window;
		}

		long eglSurfaceId;
		if (sur != null) {
			eglSurfaceId = native_eglCreateWindowSurface(display.native_egl_display, config.native_egl_config, sur, attrib_list);
		} /*else if (native_window instanceof SurfaceTexture) {
			eglSurfaceId = native_eglCreateWindowSurfaceTexture(display, config,
								      native_window, attrib_list);
		}*/
		else {
			throw new java.lang.UnsupportedOperationException(
			    "eglCreateWindowSurface() can only be called with an instance of "
			    + "Surface, SurfaceView, SurfaceHolder or [FIXME]SurfaceTexture at the moment.");
		}

		if (eglSurfaceId == 0) {
			return EGL10.EGL_NO_SURFACE;
		}
		return new EGLSurfaceImpl(eglSurfaceId);
	}

	public EGLDisplay eglGetDisplay(Object display) {
		long native_display = native_eglGetDisplay(display);
		if (native_display == 0)
			return EGL10.EGL_NO_DISPLAY;

		return new EGLDisplay(native_display);
	}

	public boolean eglInitialize(EGLDisplay display, int[] major_minor) {
		return native_eglInitialize(display.native_egl_display, major_minor);
	}

	public boolean eglGetConfigAttrib(EGLDisplay display, EGLConfig config, int attribute, int[] value) {
		return native_eglGetConfigAttrib(display.native_egl_display, config.native_egl_config, attribute, value);
	}

	public boolean eglMakeCurrent(EGLDisplay display, EGLSurface draw, EGLSurface read, EGLContext context) {
		return native_eglMakeCurrent(display.native_egl_display, ((EGLSurfaceImpl)draw).mEGLSurface, ((EGLSurfaceImpl)read).mEGLSurface, context.native_egl_context);
	}

	public boolean eglSwapBuffers(EGLDisplay display, EGLSurface surface) {
		return native_eglSwapBuffers(display.native_egl_display, ((EGLSurfaceImpl)surface).mEGLSurface);
	}

	public boolean eglDestroySurface(EGLDisplay display, EGLSurface surface) {
		return native_eglDestroySurface(display.native_egl_display, ((EGLSurfaceImpl)surface).mEGLSurface);
	}

	public boolean eglDestroyContext(EGLDisplay display, EGLContext context) {
		return native_eglDestroyContext(display.native_egl_display, context.native_egl_context);
	}

	public EGLSurface eglCreatePbufferSurface(EGLDisplay display, EGLConfig config, int[] attrib_list) {
		return new EGLSurfaceImpl(native_eglCreatePbufferSurface(display.native_egl_display, config.native_egl_config, attrib_list));
	}

	/* STUBS */
	public boolean eglCopyBuffers(EGLDisplay display, EGLSurface surface, Object native_pixmap) { return false; }
	public EGLSurface eglCreatePixmapSurface(EGLDisplay display, EGLConfig config, Object native_pixmap, int[] attrib_list) { return null; }
	public boolean eglGetConfigs(EGLDisplay display, EGLConfig[] configs, int config_size, int[] num_config) { return false; }
	public EGLContext eglGetCurrentContext() { return null; }
	public EGLDisplay eglGetCurrentDisplay() { return null; }
	public EGLSurface eglGetCurrentSurface(int readdraw) { return null; }
	public int eglGetError() { return EGL_SUCCESS; } // don't let yourself get fooled, this is also a stub :P
	public boolean eglQueryContext(EGLDisplay display, EGLContext context, int attribute, int[] value) { return false; }
	public String eglQueryString(EGLDisplay display, int name) { return "FIXME"; }
	public boolean eglQuerySurface(EGLDisplay display, EGLSurface surface, int attribute, int[] value) { return false; }
	/**
	 * @hide *
	 */
	public boolean eglReleaseThread() { return false; }
	public boolean eglTerminate(EGLDisplay display) { return false; }
	public boolean eglWaitGL() { return false; }
	public boolean eglWaitNative(int engine, Object bindTarget) { return false; }

	private native long native_eglCreateWindowSurface(long native_egl_display, long native_egl_config, Surface surface, int[] attrib_list);
	private native long native_eglGetDisplay(Object native_display);
	private native boolean native_eglInitialize(long native_egl_display, int[] major_minor);
	private native boolean native_eglGetConfigAttrib(long native_egl_display, long native_edl_config, int attribute, int[] value);
	private native boolean native_eglMakeCurrent(long native_egl_display, long native_draw_surface, long native_read_surface, long native_context);
	private native boolean native_eglSwapBuffers(long native_egl_display, long native_surface);
	private native boolean native_eglDestroySurface(long native_egl_display, long native_surface);
	private native boolean native_eglDestroyContext(long native_egl_display, long native_context);
	private native long native_eglCreatePbufferSurface(long native_egl_display, long native_egl_config, int[] attrib_list);
}

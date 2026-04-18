#include "defines.h"
#include "util.h"
#include <GLES3/gl3.h>
#include <jni.h>
#include <stdint.h>

#include "../libandroid/native_window.h"

#include "generated_headers/com_google_android_gles_jni_GLImpl.h"

JNIEXPORT void JNICALL Java_com_google_android_gles_1jni_GLImpl_glHint(JNIEnv *env, jclass this, jint target, jint mode)
{
	glHint((GLenum)target, (GLenum)mode);
}

JNIEXPORT void JNICALL Java_com_google_android_gles_1jni_GLImpl_glShadeModel(JNIEnv *env, jobject this, jint mode)
{
	glShadeModel((GLenum)mode);
}

JNIEXPORT void JNICALL Java_com_google_android_gles_1jni_GLImpl_glBlendFunc(JNIEnv *env, jclass, jint sfactor, jint dfactor)
{
	glBlendFunc((GLenum)sfactor, (GLenum)dfactor);
}

JNIEXPORT void JNICALL Java_com_google_android_gles_1jni_GLImpl_glMatrixMode(JNIEnv *env, jclass, jint mode)
{
	glMatrixMode((GLenum)mode);
}

JNIEXPORT void JNICALL Java_com_google_android_gles_1jni_GLImpl_glLoadIdentity(JNIEnv *env, jobject this)
{
	glLoadIdentity();
}

JNIEXPORT void JNICALL Java_com_google_android_gles_1jni_GLImpl_glOrthof(JNIEnv *env, jobject this, jfloat left, jfloat right, jfloat bottom, jfloat top, jfloat zNear, jfloat zFar)
{
	glOrtho((GLdouble)left, (GLdouble)right, (GLdouble)bottom, (GLdouble)top, (GLdouble)zNear, (GLdouble)zFar);
}

JNIEXPORT void JNICALL Java_com_google_android_gles_1jni_GLImpl_glLineWidth(JNIEnv *env, jobject this, jfloat width)
{
	glLineWidth((GLfloat)width);
}

JNIEXPORT void JNICALL Java_com_google_android_gles_1jni_GLImpl_glEnableClientState(JNIEnv *env, jobject this, jint array)
{
	glEnableClientState((GLenum)array);
}

JNIEXPORT void JNICALL Java_com_google_android_gles_1jni_GLImpl_glVertexPointerBounds(JNIEnv *env, jobject this, jint size, jint type, jint stride, jobject pointer, jint remaining)
{
	jarray array_ref;
	jbyte *array;
	GLvoid *pixels = get_nio_buffer(env, pointer, &array_ref, &array);

	glVertexPointer(size, (GLenum)type, stride, pixels);
	release_nio_buffer(env, array_ref, array);
}

JNIEXPORT void JNICALL Java_com_google_android_gles_1jni_GLImpl_glColorPointerBounds(JNIEnv *env, jobject this, jint size, jint type, jint stride, jobject pointer, jint remaining)
{
	jarray array_ref;
	jbyte *array;
	GLvoid *pixels = get_nio_buffer(env, pointer, &array_ref, &array);

	glColorPointer((GLint)size, (GLenum)type, (GLsizei)stride, pixels);
	release_nio_buffer(env, array_ref, array);
}

JNIEXPORT void JNICALL Java_com_google_android_gles_1jni_GLImpl_glDrawArrays(JNIEnv *env, jclass this, jint mode, jint first, jint count)
{
	glDrawArrays((GLenum)mode, (GLint)first, (GLsizei)count);
}

JNIEXPORT void JNICALL Java_com_google_android_gles_1jni_GLImpl_glDisableClientState(JNIEnv *env, jobject this, jint array)
{
	glDisableClientState((GLenum)array);
}

JNIEXPORT void JNICALL Java_com_google_android_gles_1jni_GLImpl_glColor4f(JNIEnv *env, jobject this, jfloat red, jfloat green, jfloat blue, jfloat alpha)
{
	glColor4f((GLfloat)red, (GLfloat)green, (GLfloat)blue, (GLfloat)alpha);
}

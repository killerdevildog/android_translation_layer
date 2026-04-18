#include "defines.h"
#include "util.h"
#include <GLES2/gl2.h>
#include <jni.h>
#include <stdint.h>

#include "../libandroid/native_window.h"

#include "generated_headers/android_opengl_GLES20.h"

JNIEXPORT jstring JNICALL Java_android_opengl_GLES20_glGetString(JNIEnv *env, jclass, jint name)
{
	const char *chars = (const char *)glGetString((GLenum)name);
	return _JSTRING(chars);
}

JNIEXPORT jint JNICALL Java_android_opengl_GLES20_glGetError(JNIEnv *env, jclass)
{
	return (jint)glGetError();
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glGetIntegerv__I_3II(JNIEnv *env, jclass, jint pname, jintArray params_ref, jint offset)
{
	jint *params = (*env)->GetIntArrayElements(env, params_ref, NULL);
	glGetIntegerv((GLenum)pname, params + offset);
	(*env)->ReleaseIntArrayElements(env, params_ref, params, 0);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glEnableVertexAttribArray(JNIEnv *env, jclass, jint index)
{
	glEnableVertexAttribArray((GLuint)index);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glVertexAttribPointerBounds(JNIEnv *env, jclass, jint index, jint size, jint type, jboolean normalized, jint stride, jobject pointer, jint count)
{
	jarray array_ref;
	jbyte *array;
	GLvoid *pixels = get_nio_buffer(env, pointer, &array_ref, &array);

	glVertexAttribPointer(index, size, type, normalized, stride, pixels);
	release_nio_buffer(env, array_ref, array);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glDisable(JNIEnv *env, jclass, jint cap)
{
	glDisable((GLenum)cap);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glActiveTexture(JNIEnv *env, jclass, jint texture)
{
	glActiveTexture((GLenum)texture);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glEnable(JNIEnv *env, jclass, jint cap)
{
	glEnable((GLenum)cap);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glFrontFace(JNIEnv *env, jclass, jint mode)
{
	glEnable((GLenum)mode);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glViewport(JNIEnv *env, jclass, jint x, jint y, jint width, jint height)
{
	glViewport((GLint)x, (GLint)y, (GLsizei)width, (GLsizei)height);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glGenTextures__I_3II(JNIEnv *env, jclass, jint n, jintArray textures_ref, jint offset)
{
	jint *textures = (*env)->GetIntArrayElements(env, textures_ref, NULL);
	glGenTextures((GLsizei)n, (GLuint *)textures + offset);
	(*env)->ReleaseIntArrayElements(env, textures_ref, textures, 0);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glBindTexture(JNIEnv *env, jclass, jint target, jint texture)
{
	glBindTexture((GLenum)target, (GLuint)texture);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glTexImage2D(JNIEnv *env, jclass, jint target, jint level, jint internalformat, jint width, jint height, jint border, jint format, jint type, jobject pixels_buf)
{
	jarray array_ref;
	jbyte *array;
	GLvoid *pixels = get_nio_buffer(env, pixels_buf, &array_ref, &array);
	glTexImage2D((GLenum)target, (GLint)level, (GLint)internalformat, (GLsizei)width, (GLsizei)height, (GLint)border, (GLenum)format, (GLenum)type, pixels);
	release_nio_buffer(env, array_ref, array);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glTexSubImage2D(JNIEnv *env, jclass, jint target, jint level, jint xoffset, jint yoffset, jint width, jint height, jint format, jint type, jobject pixels_buf)
{
	jarray array_ref;
	jbyte *array;
	GLvoid *pixels = get_nio_buffer(env, pixels_buf, &array_ref, &array);
	glTexSubImage2D((GLenum)target, (GLint)level, (GLint)xoffset, (GLint)yoffset, (GLsizei)width, (GLsizei)height, (GLenum)format, (GLenum)type, pixels);
	release_nio_buffer(env, array_ref, array);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glTexParameterf(JNIEnv *env, jclass, jint target, jint pname, jfloat param)
{
	glTexParameterf((GLenum)target, (GLenum)pname, (GLfloat)param);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glGenBuffers__I_3II(JNIEnv *env, jclass, jint n, jintArray buffers_ref, jint offset)
{
	jint *buffers = (*env)->GetIntArrayElements(env, buffers_ref, NULL);
	glGenBuffers((GLsizei)n, (GLuint *)buffers + offset);
	(*env)->ReleaseIntArrayElements(env, buffers_ref, buffers, 0);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glBindBuffer(JNIEnv *env, jclass, jint target, jint buffer)
{
	glBindBuffer((GLenum)target, (GLuint)buffer);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glBufferData(JNIEnv *env, jclass, jint target, jint size, jobject data_buf, jint usage)
{
	jarray array_ref;
	jbyte *array;
	GLvoid *data = get_nio_buffer(env, data_buf, &array_ref, &array);
	glBufferData((GLenum)target, (GLsizeiptr)size, data, (GLenum)usage);
	release_nio_buffer(env, array_ref, array);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glDisableVertexAttribArray(JNIEnv *env, jclass, jint index)
{
	glDisableVertexAttribArray((GLuint)index);
}

JNIEXPORT jint JNICALL Java_android_opengl_GLES20_glCreateShader(JNIEnv *env, jclass, jint type)
{
	return (jint)glCreateShader((GLenum)type);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glShaderSource(JNIEnv *env, jclass, jint shader, jstring string)
{
	const char *nativeString = (*env)->GetStringUTFChars(env, string, NULL);
	const char *strings[] = {nativeString};
	glShaderSource(shader, 1, strings, 0);
	(*env)->ReleaseStringUTFChars(env, string, nativeString);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glCompileShader(JNIEnv *env, jclass, jint shader)
{
	glCompileShader((GLuint)shader);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glGetShaderiv__II_3II(JNIEnv *env, jclass, jint shader, jint pname, jintArray params_ref, jint offset)
{
	jint *params = (*env)->GetIntArrayElements(env, params_ref, NULL);
	glGetShaderiv((GLuint)shader, (GLenum)pname, params + offset);
	(*env)->ReleaseIntArrayElements(env, params_ref, params, 0);
}

JNIEXPORT jint JNICALL Java_android_opengl_GLES20_glCreateProgram(JNIEnv *env, jclass)
{
	return (jint)glCreateProgram();
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glAttachShader(JNIEnv *env, jclass, jint program, jint shader)
{
	glAttachShader((GLuint)program, (GLuint)shader);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glBindAttribLocation(JNIEnv *env, jclass, jint program, jint index, jstring name)
{
	const char *nativeName = (*env)->GetStringUTFChars(env, name, NULL);
	glBindAttribLocation((GLuint)program, (GLuint)index, nativeName);
	(*env)->ReleaseStringUTFChars(env, name, nativeName);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glLinkProgram(JNIEnv *env, jclass, jint program)
{
	glLinkProgram((GLuint)program);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glGetProgramiv__II_3II(JNIEnv *env, jclass, jint program, jint pname, jintArray params_ref, jint offset)
{
	jint *params = (*env)->GetIntArrayElements(env, params_ref, NULL);
	glGetProgramiv((GLuint)program, (GLenum)pname, params + offset);
	(*env)->ReleaseIntArrayElements(env, params_ref, params, 0);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glGetActiveAttrib__III_3II_3II_3II_3BI(JNIEnv *env, jclass, jint program, jint index, jint bufsize, jintArray length_ref, jint lengthOffset, jintArray size_ref, jint sizeOffset, jintArray type_ref, jint typeOffset, jbyteArray name_ref, jint nameOffset)
{
	jint *length = (*env)->GetIntArrayElements(env, length_ref, NULL);
	jint *size = (*env)->GetIntArrayElements(env, size_ref, NULL);
	jint *type = (*env)->GetIntArrayElements(env, type_ref, NULL);
	jbyte *name = (*env)->GetByteArrayElements(env, name_ref, NULL);
	glGetActiveAttrib((GLuint)program, (GLuint)index, (GLsizei)bufsize, (GLsizei *)length + lengthOffset, (GLint *)size + sizeOffset, (GLenum *)type + typeOffset, (char *)name + nameOffset);
	(*env)->ReleaseByteArrayElements(env, name_ref, name, 0);
	(*env)->ReleaseIntArrayElements(env, type_ref, type, 0);
	(*env)->ReleaseIntArrayElements(env, size_ref, size, 0);
	(*env)->ReleaseIntArrayElements(env, length_ref, length, 0);
}

JNIEXPORT jint JNICALL Java_android_opengl_GLES20_glGetAttribLocation(JNIEnv *env, jclass, jint program, jstring name)
{
	const char *nativeName = (*env)->GetStringUTFChars(env, name, NULL);
	jint ret = glGetAttribLocation((GLuint)program, nativeName);
	(*env)->ReleaseStringUTFChars(env, name, nativeName);
	return ret;
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glGetActiveUniform__III_3II_3II_3II_3BI(JNIEnv *env, jclass, jint program, jint index, jint bufsize, jintArray length_ref, jint lengthOffset, jintArray size_ref, jint sizeOffset, jintArray type_ref, jint typeOffset, jbyteArray name_ref, jint nameOffset)
{
	jint *length = (*env)->GetIntArrayElements(env, length_ref, NULL);
	jint *size = (*env)->GetIntArrayElements(env, size_ref, NULL);
	jint *type = (*env)->GetIntArrayElements(env, type_ref, NULL);
	jbyte *name = (*env)->GetByteArrayElements(env, name_ref, NULL);
	glGetActiveUniform((GLuint)program, (GLuint)index, (GLsizei)bufsize, (GLsizei *)length + lengthOffset, (GLint *)size + sizeOffset, (GLenum *)type + typeOffset, (char *)name + nameOffset);
	(*env)->ReleaseByteArrayElements(env, name_ref, name, 0);
	(*env)->ReleaseIntArrayElements(env, type_ref, type, 0);
	(*env)->ReleaseIntArrayElements(env, size_ref, size, 0);
	(*env)->ReleaseIntArrayElements(env, length_ref, length, 0);
}

JNIEXPORT jint JNICALL Java_android_opengl_GLES20_glGetUniformLocation(JNIEnv *env, jclass, jint program, jstring name)
{
	const char *nativeName = (*env)->GetStringUTFChars(env, name, NULL);
	jint ret = glGetUniformLocation((GLuint)program, nativeName);
	(*env)->ReleaseStringUTFChars(env, name, nativeName);
	return ret;
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glDeleteShader(JNIEnv *env, jclass, jint shader)
{
	glDeleteShader((GLuint)shader);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glDeleteTextures(JNIEnv *env, jclass, jint n, jintArray textures, jint offset)
{
	jint *tex = (*env)->GetIntArrayElements(env, textures, NULL);

	glDeleteTextures((GLsizei)n, (const GLuint *)tex + (4 * offset));

	(*env)->ReleaseIntArrayElements(env, textures, tex, 0);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glUseProgram(JNIEnv *env, jclass, jint program)
{
	glUseProgram((GLuint)program);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glVertexAttribPointer(JNIEnv *env, jclass, jint indx, jint size, jint type, jboolean normalized, jint stride, jint offset)
{
	glVertexAttribPointer((GLuint)indx, (GLint)size, (GLenum)type, (GLboolean)normalized, (GLsizei)stride, (GLvoid *)(intptr_t)offset);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glUniformMatrix4fv__IIZ_3FI(JNIEnv *env, jclass, jint location, jint count, jboolean transpose, jfloatArray value_ref, jint offset)
{
	jfloat *value = (*env)->GetFloatArrayElements(env, value_ref, NULL);
	glUniformMatrix4fv((GLint)location, (GLsizei)count, (GLboolean)transpose, (GLfloat *)value);
	(*env)->ReleaseFloatArrayElements(env, value_ref, value, 0);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glUniform1i(JNIEnv *env, jclass, jint location, jint x)
{
	glUniform1i((GLint)location, (GLint)x);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glUniform4f(JNIEnv *env, jclass, jint location, jfloat x, jfloat y, jfloat z, jfloat w)
{
	glUniform4f((GLint)location, (GLfloat)x, (GLfloat)y, (GLfloat)z, (GLfloat)w);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glDrawArrays(JNIEnv *env, jclass, jint mode, jint first, jint count)
{
	glDrawArrays((GLenum)mode, (GLint)first, (GLsizei)count);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glDrawElements(JNIEnv *env, jclass, jint mode, jint count, jint type, jobject indices)
{
	jarray array_ref;
	jbyte *array;
	GLvoid *data = get_nio_buffer(env, indices, &array_ref, &array);

	glDrawElements((GLenum)mode, (GLsizei)type, (GLenum)type, data);
	release_nio_buffer(env, array_ref, array);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glClearColor(JNIEnv *env, jclass, jfloat red, jfloat green, jfloat blue, jfloat alpha)
{
	glClearColor((GLclampf)red, (GLclampf)green, (GLclampf)blue, (GLclampf)alpha);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glClear(JNIEnv *env, jclass, jint mask)
{
	glClear((GLbitfield)mask);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glBlendFunc(JNIEnv *env, jclass, jint sfactor, jint dfactor)
{
	glBlendFunc((GLenum)sfactor, (GLenum)dfactor);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glGetFloatv__I_3FI(JNIEnv *env, jclass this, jint pname, jfloatArray params_ref, jint offset)
{
	GLfloat *params_base = (GLfloat *)(*env)->GetPrimitiveArrayCritical(env, params_ref, 0);
	GLfloat *params = params_base + offset;

	glGetFloatv((GLenum)pname, params);

	(*env)->ReleasePrimitiveArrayCritical(env, params_ref, params_base, 0);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glFlush(JNIEnv *env, jclass this)
{
	glFlush();
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glReadPixels(JNIEnv *env, jclass this, jint x, jint y, jint width, jint height, jint format, jint type, jobject pixels_buf)
{
	jarray array_ref;
	jbyte *array;
	GLvoid *pixels = get_nio_buffer(env, pixels_buf, &array_ref, &array);
	glReadPixels(x, y, width, height, format, type, pixels);
	release_nio_buffer(env, array_ref, array);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glPixelStorei(JNIEnv *env, jclass this, jint pname, jint param)
{
	glPixelStorei((GLenum)pname, (GLint)param);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glTexParameteri(JNIEnv *env, jclass this, jint target, jint pname, jint param)
{
	glTexParameteri((GLenum)target, (GLenum)pname, (GLint)param);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glGetShaderiv__IILjava_nio_IntBuffer_2(JNIEnv *env, jclass this, jint shader, jint pname, jobject params_buf)
{
	jarray array_ref;
	jbyte *array;
	GLvoid *params = get_nio_buffer(env, params_buf, &array_ref, &array);
	glGetShaderiv((GLuint)shader, (GLenum)pname, (GLint *)params);
	release_nio_buffer(env, array_ref, array);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glGetProgramiv__IILjava_nio_IntBuffer_2(JNIEnv *env, jclass this, jint program, jint pname, jobject params_buf)
{
	jarray array_ref;
	jbyte *array;
	GLvoid *params = get_nio_buffer(env, params_buf, &array_ref, &array);
	glGetProgramiv((GLuint)program, (GLenum)pname, (GLint *)params);
	release_nio_buffer(env, array_ref, array);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glDepthMask(JNIEnv *env, jclass this, jboolean flag)
{
	glDepthMask((GLboolean)flag);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glBlendFuncSeparate(JNIEnv *env, jclass this, jint srcRGB, jint dstRGB, jint srcAlpha, jint dstAlpha)
{
	glBlendFuncSeparate((GLenum)srcRGB, (GLenum)dstRGB, (GLenum)srcAlpha, (GLenum)dstAlpha);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glGenFramebuffers__I_3II(JNIEnv *env, jclass this, jint n, jintArray framebuffers_ref, jint offset)
{
	GLuint *framebuffers = (*env)->GetPrimitiveArrayCritical(env, framebuffers_ref, 0);
	glGenFramebuffers((GLsizei)n, framebuffers + offset);
	(*env)->ReleasePrimitiveArrayCritical(env, framebuffers_ref, framebuffers, 0);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glBindFramebuffer(JNIEnv *env, jclass this, jint target, jint framebuffer)
{
	bionic_glBindFramebuffer((GLenum)target, (GLuint)framebuffer);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glFramebufferTexture2D(JNIEnv *env, jclass this, jint target, jint attachment, jint textarget, jint texture, jint level)
{
	glFramebufferTexture2D((GLenum)target, (GLenum)attachment, (GLenum)textarget, (GLuint)texture, (GLint)level);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glBindRenderbuffer(JNIEnv *env, jclass this, jint target, jint renderbuffer)
{
	glBindRenderbuffer((GLenum)target, (GLuint)renderbuffer);
}

JNIEXPORT jint JNICALL Java_android_opengl_GLES20_glCheckFramebufferStatus(JNIEnv *env, jclass this, jint target)
{
	return (jint)glCheckFramebufferStatus((GLenum)target);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glDeleteFramebuffers__I_3II(JNIEnv *env, jclass this, jint n, jintArray framebuffers_ref, jint offset)
{
	GLuint *framebuffers = (*env)->GetPrimitiveArrayCritical(env, framebuffers_ref, 0);
	glDeleteFramebuffers((GLsizei)n, framebuffers + offset);
	(*env)->ReleasePrimitiveArrayCritical(env, framebuffers_ref, framebuffers, 0);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glDeleteProgram(JNIEnv *env, jclass this, jint program)
{
	glDeleteProgram((GLuint)program);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glGetFloatv__ILjava_nio_FloatBuffer_2(JNIEnv *env, jclass this, jint pname, jobject params_buf)
{
	jarray array_ref;
	jbyte *array;
	GLvoid *params = get_nio_buffer(env, params_buf, &array_ref, &array);
	glGetFloatv((GLenum)pname, (GLfloat *)params);
	release_nio_buffer(env, array_ref, array);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glGenerateMipmap(JNIEnv *env, jclass this, jint pname)
{
	glGenerateMipmap((GLenum)pname);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glLineWidth(JNIEnv *env, jclass this, jfloat width)
{
	glLineWidth((GLfloat)width);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glColorMask(JNIEnv *env, jclass this, jboolean red, jboolean green, jboolean blue, jboolean alpha)
{
	glColorMask((GLboolean)red, (GLboolean)green, (GLboolean)blue, (GLboolean)alpha);
}

JNIEXPORT void JNICALL Java_android_opengl_GLES20_glBufferSubData(JNIEnv *env, jclass this, jint target, jint offset, jint size, jobject data)
{
	glBufferSubData((GLenum)target, (GLintptr)offset, (GLsizeiptr)size, (void *)data);
}

JNIEXPORT jstring JNICALL Java_android_opengl_GLES20_glGetShaderInfoLog(JNIEnv *env, jclass this, jint shader)
{
	GLsizei bufSize;
	glGetShaderiv((GLuint)shader, GL_INFO_LOG_LENGTH, &bufSize);

	jstring output;
	if (bufSize == 0) {
		char cstring = 0;
		output = _JSTRING(&cstring);
	} else {
		GLchar *infoLog = malloc(sizeof(GLchar) * bufSize + 1);
		GLsizei length;
		glGetShaderInfoLog((GLuint)shader, bufSize, &length, infoLog);
		output = _JSTRING(infoLog);
		free(infoLog);
	}
	return output;
}

JNIEXPORT jstring JNICALL Java_android_opengl_GLES20_glGetActiveUniform__IILjava_nio_IntBuffer_2Ljava_nio_IntBuffer_2(JNIEnv *env, jclass this, jint program, jint index, jobject buffer_1, jobject buffer_2)
{
	char *name = malloc(512);
	glGetActiveUniform((GLuint)program, (GLuint)index, 512, NULL, NULL, NULL, name);
	jstring output = _JSTRING(name);
	free(name);
	return output;
}

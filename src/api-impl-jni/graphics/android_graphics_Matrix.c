#include <graphene.h>
#include <stdint.h>

#include "../defines.h"
#include "../util.h"

#include "../generated_headers/android_graphics_Matrix.h"

JNIEXPORT jlong JNICALL Java_android_graphics_Matrix_native_1create(JNIEnv *env, jclass class, jlong src)
{
	return _INTPTR(graphene_matrix_init_identity(graphene_matrix_alloc()));
}

JNIEXPORT void JNICALL Java_android_graphics_Matrix_native_1getValues(JNIEnv *env, jclass class, jlong src, jfloatArray values_ref)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(src);
	jfloat *value = (*env)->GetFloatArrayElements(env, values_ref, NULL);
	// add 0.f to all values to avoid failing CTS tests with "expected:<0.0> but was:<-0.0>"
	value[android_graphics_Matrix_MSCALE_X] = graphene_matrix_get_value(matrix, 0, 0) + 0.f;
	value[android_graphics_Matrix_MSKEW_X] = graphene_matrix_get_value(matrix, 1, 0) + 0.f;
	value[android_graphics_Matrix_MTRANS_X] = graphene_matrix_get_value(matrix, 3, 0) + 0.f;
	value[android_graphics_Matrix_MSKEW_Y] = graphene_matrix_get_value(matrix, 0, 1) + 0.f;
	value[android_graphics_Matrix_MSCALE_Y] = graphene_matrix_get_value(matrix, 1, 1) + 0.f;
	value[android_graphics_Matrix_MTRANS_Y] = graphene_matrix_get_value(matrix, 3, 1) + 0.f;
	value[android_graphics_Matrix_MPERSP_0] = graphene_matrix_get_value(matrix, 0, 3) + 0.f;
	value[android_graphics_Matrix_MPERSP_1] = graphene_matrix_get_value(matrix, 1, 3) + 0.f;
	value[android_graphics_Matrix_MPERSP_2] = graphene_matrix_get_value(matrix, 3, 3) + 0.f;
	(*env)->ReleaseFloatArrayElements(env, values_ref, value, 0);
}

JNIEXPORT void JNICALL Java_android_graphics_Matrix_native_1set(JNIEnv *env, jclass class, jlong dest_ptr, jlong src_ptr)
{
	graphene_matrix_t *dest = (graphene_matrix_t *)_PTR(dest_ptr);
	graphene_matrix_t *src = (graphene_matrix_t *)_PTR(src_ptr);
	graphene_matrix_init_from_matrix(dest, src);
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1isIdentity(JNIEnv *env, jclass class, jlong matrix_ptr)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	return graphene_matrix_is_identity(matrix);
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1preConcat(JNIEnv *env, jclass class, jlong matrix_ptr, jlong other_ptr)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_t *other = (graphene_matrix_t *)_PTR(other_ptr);
	graphene_matrix_multiply(other, matrix, matrix);

	return true;
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1postConcat(JNIEnv *env, jclass class, jlong matrix_ptr, jlong other_ptr)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_t *other = (graphene_matrix_t *)_PTR(other_ptr);
	graphene_matrix_multiply(matrix, other, matrix);
	return true;
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1mapRect(JNIEnv *env, jclass class, jlong matrix_ptr, jobject dest, jobject src)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_rect_t src_rect = GRAPHENE_RECT_INIT(
	    _GET_FLOAT_FIELD(src, "left"),
	    _GET_FLOAT_FIELD(src, "top"),
	    _GET_FLOAT_FIELD(src, "right") - _GET_FLOAT_FIELD(src, "left"),
	    _GET_FLOAT_FIELD(src, "bottom") - _GET_FLOAT_FIELD(src, "top"));

	graphene_quad_t dest_quad;
	graphene_matrix_transform_rect(matrix, &src_rect, &dest_quad);
	graphene_rect_t dest_rect;
	graphene_quad_bounds(&dest_quad, &dest_rect);

	_SET_FLOAT_FIELD(dest, "left", dest_rect.origin.x);
	_SET_FLOAT_FIELD(dest, "top", dest_rect.origin.y);
	_SET_FLOAT_FIELD(dest, "right", dest_rect.origin.x + dest_rect.size.width);
	_SET_FLOAT_FIELD(dest, "bottom", dest_rect.origin.y + dest_rect.size.height);
	return true;
}

JNIEXPORT void JNICALL Java_android_graphics_Matrix_native_1reset(JNIEnv *env, jclass class, jlong matrix_ptr)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_init_identity(matrix);
}

JNIEXPORT void JNICALL Java_android_graphics_Matrix_finalizer(JNIEnv *env, jclass class, jlong matrix_ptr)
{
	graphene_matrix_free((graphene_matrix_t *)_PTR(matrix_ptr));
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1postTranslate(JNIEnv *env, jclass class, jlong matrix_ptr, jfloat x, jfloat y)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_point3d_t translation = GRAPHENE_POINT3D_INIT(x, y, 0);
	graphene_matrix_translate(matrix, &translation);
	return true;
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1postScale__JFF(JNIEnv *env, jclass class, jlong matrix_ptr, jfloat x, jfloat y)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_scale(matrix, x, y, 1.f);
	return true;
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1postRotate__JFFF(JNIEnv *env, jclass class, jlong matrix_ptr, jfloat degrees, jfloat px, jfloat py)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_translate(matrix, &GRAPHENE_POINT3D_INIT(-px, -py, 0));
	graphene_matrix_rotate_z(matrix, degrees);
	graphene_matrix_translate(matrix, &GRAPHENE_POINT3D_INIT(px, py, 0));
	return true;
}

JNIEXPORT void Java_android_graphics_Matrix_native_1setScale__JFF(JNIEnv *env, jclass class, jlong matrix_ptr, jfloat x, jfloat y)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_init_scale(matrix, x, y, 1.f);
}

JNIEXPORT void JNICALL Java_android_graphics_Matrix_native_1setScale__JFFFF(JNIEnv *env, jclass class, jlong matrix_ptr, jfloat x, jfloat y, jfloat px, jfloat py)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_init_translate(matrix, &GRAPHENE_POINT3D_INIT(-px, -py, 0));
	graphene_matrix_scale(matrix, x, y, 1.f);
	graphene_matrix_translate(matrix, &GRAPHENE_POINT3D_INIT(px, py, 0));
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1postScale__JFFFF(JNIEnv *env, jclass class, jlong matrix_ptr, jfloat x, jfloat y, jfloat px, jfloat py)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_translate(matrix, &GRAPHENE_POINT3D_INIT(-px, -py, 0));
	graphene_matrix_scale(matrix, x, y, 1.f);
	graphene_matrix_translate(matrix, &GRAPHENE_POINT3D_INIT(px, py, 0));
	return true;
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1postRotate__JF(JNIEnv *env, jclass class, jlong matrix_ptr, jfloat degrees)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_rotate_z(matrix, degrees);
	return true;
}

#define SCALE_TO_FIT_FILL   0
#define SCALE_TO_FIT_START  1
#define SCALE_TO_FIT_CENTER 2
#define SCALE_TO_FIT_END    3
JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1setRectToRect(JNIEnv *env, jclass class, jlong matrix_ptr, jobject src, jobject dest, jint stf)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_init_translate(matrix, &GRAPHENE_POINT3D_INIT(-_GET_FLOAT_FIELD(src, "left"), -_GET_FLOAT_FIELD(src, "top"), 0));
	float src_width = _GET_FLOAT_FIELD(src, "right") - _GET_FLOAT_FIELD(src, "left");
	float src_height = _GET_FLOAT_FIELD(src, "bottom") - _GET_FLOAT_FIELD(src, "top");
	float dest_width = _GET_FLOAT_FIELD(dest, "right") - _GET_FLOAT_FIELD(dest, "left");
	float dest_height = _GET_FLOAT_FIELD(dest, "bottom") - _GET_FLOAT_FIELD(dest, "top");
	float factor_x = dest_width / src_width;
	float factor_y = dest_height / src_height;
	if (stf != SCALE_TO_FIT_FILL) {
		factor_x = factor_y = (factor_x < factor_y) ? factor_x : factor_y;
	}
	graphene_matrix_scale(matrix, factor_x, factor_y, 1.f);
	graphene_point3d_t translation = GRAPHENE_POINT3D_INIT(_GET_FLOAT_FIELD(dest, "left"), _GET_FLOAT_FIELD(dest, "top"), 0);
	if (stf == SCALE_TO_FIT_CENTER) {
		translation.x += (dest_width - src_width * factor_x) / 2;
		translation.y += (dest_height - src_height * factor_y) / 2;
	} else if (stf == SCALE_TO_FIT_END) {
		translation.x += (dest_width - src_width * factor_x);
		translation.y += (dest_height - src_height * factor_y);
	}
	graphene_matrix_translate(matrix, &translation);
	return true;
}

JNIEXPORT void JNICALL Java_android_graphics_Matrix_native_1mapPoints(JNIEnv *env, jclass class, jlong matrix_ptr, jfloatArray dst_ref, jint dst_idx, jfloatArray src_ref, jint src_idx, jint count, jboolean is_pts)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_t matrix_cpy;
	if (!is_pts) {
		// remove translation
		graphene_matrix_init_from_matrix(&matrix_cpy, matrix);
		graphene_point3d_t translation = GRAPHENE_POINT3D_INIT(
		    -graphene_matrix_get_x_translation(matrix),
		    -graphene_matrix_get_y_translation(matrix),
		    -graphene_matrix_get_z_translation(matrix));
		graphene_matrix_translate(&matrix_cpy, &translation);
		matrix = &matrix_cpy;
	}
	jfloat *src = (*env)->GetFloatArrayElements(env, src_ref, NULL);
	jfloat *dst = (*env)->GetFloatArrayElements(env, dst_ref, NULL);
	graphene_point_t p;
	graphene_point_t res;
	for (int i = 0; i < count; i++) {
		p = GRAPHENE_POINT_INIT(src[src_idx + i * 2], src[src_idx + i * 2 + 1]);
		graphene_matrix_transform_point(matrix, &p, &res);
		dst[dst_idx + i * 2] = res.x;
		dst[dst_idx + i * 2 + 1] = res.y;
	}
	(*env)->ReleaseFloatArrayElements(env, src_ref, src, 0);
	(*env)->ReleaseFloatArrayElements(env, dst_ref, dst, 0);
}

JNIEXPORT void JNICALL Java_android_graphics_Matrix_native_1setTranslate(JNIEnv *env, jclass class, jlong matrix_ptr, jfloat x, jfloat y)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_init_translate(matrix, &GRAPHENE_POINT3D_INIT(x, y, 0));
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1preRotate__JF(JNIEnv *env, jclass class, jlong matrix_ptr, jfloat degrees)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_t rotation;
	graphene_vec3_t rotation_axis;
	graphene_vec3_init(&rotation_axis, 0, 0, 1);
	graphene_matrix_init_rotate(&rotation, degrees, &rotation_axis);
	graphene_matrix_multiply(&rotation, matrix, matrix);
	return true;
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1invert(JNIEnv *env, jclass class, jlong matrix_ptr, jlong inverse_ptr)
{
	return graphene_matrix_inverse((graphene_matrix_t *)_PTR(matrix_ptr), (graphene_matrix_t *)_PTR(inverse_ptr));
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1preScale__JFF(JNIEnv *env, jclass class, jlong matrix_ptr, jfloat x, jfloat y)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_t scale;
	graphene_matrix_init_scale(&scale, x, y, 1);
	graphene_matrix_multiply(&scale, matrix, matrix);
	return true;
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1preScale__JFFFF(JNIEnv *env, jclass class, jlong matrix_ptr, jfloat x, jfloat y, jfloat px, jfloat py)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_t scale;
	graphene_matrix_init_scale(&scale, x, y, 1);
	graphene_matrix_translate(&scale, &GRAPHENE_POINT3D_INIT(-px, -py, 0));
	graphene_matrix_multiply(&scale, matrix, matrix);
	graphene_matrix_translate(matrix, &GRAPHENE_POINT3D_INIT(px, py, 0));
	return true;
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1preTranslate(JNIEnv *env, jclass class, jlong matrix_ptr, jfloat x, jfloat y)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_t translation;
	graphene_matrix_init_translate(&translation, &GRAPHENE_POINT3D_INIT(x, y, 0));
	graphene_matrix_multiply(&translation, matrix, matrix);
	return true;
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1preRotate__JFFF(JNIEnv *env, jclass class, jlong matrix_ptr, jfloat degrees, jfloat px, jfloat py)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_t rotation;
	graphene_vec3_t rotation_axis;
	graphene_vec3_init(&rotation_axis, 0, 0, 1);
	graphene_matrix_init_rotate(&rotation, degrees, &rotation_axis);
	graphene_matrix_translate(&rotation, &GRAPHENE_POINT3D_INIT(-px, -py, 0));
	graphene_matrix_multiply(&rotation, matrix, matrix);
	graphene_matrix_translate(matrix, &GRAPHENE_POINT3D_INIT(px, py, 0));
	return true;
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1equals(JNIEnv *env, jclass class, jlong matrix1_ptr, jlong matrix2_ptr)
{
	graphene_matrix_t *matrix1 = (graphene_matrix_t *)_PTR(matrix1_ptr);
	graphene_matrix_t *matrix2 = (graphene_matrix_t *)_PTR(matrix2_ptr);
	return graphene_matrix_equal(matrix1, matrix2);
}

JNIEXPORT void JNICALL Java_android_graphics_Matrix_native_1setValues(JNIEnv *env, jclass class, jlong matrix_ptr, jfloatArray values_ref)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	jfloat *values = (*env)->GetFloatArrayElements(env, values_ref, NULL);
	float values4x4[4][4] = {
		/* clang-format off */
		{values[android_graphics_Matrix_MSCALE_X],  values[android_graphics_Matrix_MSKEW_Y], 0, values[android_graphics_Matrix_MPERSP_0]},
		{ values[android_graphics_Matrix_MSKEW_X], values[android_graphics_Matrix_MSCALE_Y], 0, values[android_graphics_Matrix_MPERSP_1]},
		{                                       0,                                        0, 1,                                        0},
		{values[android_graphics_Matrix_MTRANS_X], values[android_graphics_Matrix_MTRANS_Y], 0, values[android_graphics_Matrix_MPERSP_2]},
		/* clang-format on */
	};
	graphene_matrix_init_from_float(matrix, *values4x4);
	(*env)->ReleaseFloatArrayElements(env, values_ref, values, 0);
}

JNIEXPORT void JNICALL Java_android_graphics_Matrix_native_1setRotate__JFFF(JNIEnv *env, jclass class, jlong matrix_ptr, jfloat degrees, jfloat px, jfloat py)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_init_translate(matrix, &GRAPHENE_POINT3D_INIT(-px, -py, 0));
	graphene_matrix_rotate_z(matrix, degrees);
	graphene_matrix_translate(matrix, &GRAPHENE_POINT3D_INIT(px, py, 0));
}

JNIEXPORT void JNICALL Java_android_graphics_Matrix_native_1setRotate__JF(JNIEnv *env, jclass class, jlong matrix_ptr, jfloat degrees)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	graphene_matrix_rotate_z(matrix, degrees);
}

JNIEXPORT jboolean JNICALL Java_android_graphics_Matrix_native_1rectStaysRect(JNIEnv *env, jclass class, jlong matrix_ptr)
{
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	float scale_x = graphene_matrix_get_value(matrix, 0, 0);
	float skew_x = graphene_matrix_get_value(matrix, 1, 0);
	float skew_y = graphene_matrix_get_value(matrix, 0, 1);
	float scale_y = graphene_matrix_get_value(matrix, 1, 1);
	return (!skew_x && !skew_y) || (!scale_x && !scale_y);
}

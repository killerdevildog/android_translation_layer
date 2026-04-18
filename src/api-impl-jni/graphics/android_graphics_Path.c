#include <gsk/gsk.h>

#include "../defines.h"

#include "../generated_headers/android_graphics_Path.h"

JNIEXPORT jlong JNICALL Java_android_graphics_Path_native_1create_1builder(JNIEnv *env, jclass this, jlong path_ptr, jlong builder_ptr)
{
	GskPathBuilder *builder = _PTR(builder_ptr);
	if (!builder)
		builder = gsk_path_builder_new();
	if (path_ptr) {
		GskPath *path = _PTR(path_ptr);
		gsk_path_builder_add_path(builder, path);
		gsk_path_unref(path);
	}
	return _INTPTR(builder);
}

JNIEXPORT jlong JNICALL Java_android_graphics_Path_native_1create_1path(JNIEnv *env, jclass this, jlong builder_ptr)
{
	GskPathBuilder *builder = _PTR(builder_ptr);
	if (!builder)
		builder = gsk_path_builder_new();
	return _INTPTR(gsk_path_builder_to_path(builder));
}

JNIEXPORT jlong JNICALL Java_android_graphics_Path_native_1ref_1path(JNIEnv *env, jclass this, jlong path_ptr)
{
	return _INTPTR(gsk_path_ref(_PTR(path_ptr)));
}

JNIEXPORT void JNICALL Java_android_graphics_Path_native_1reset(JNIEnv *env, jclass this, jlong path_ptr, jlong builder_ptr)
{
	if (path_ptr)
		gsk_path_unref(_PTR(path_ptr));
	if (builder_ptr)
		gsk_path_builder_unref(_PTR(builder_ptr));
}

JNIEXPORT void JNICALL Java_android_graphics_Path_native_1close(JNIEnv *env, jclass this, jlong builder_ptr)
{
	gsk_path_builder_close(_PTR(builder_ptr));
}

JNIEXPORT void JNICALL Java_android_graphics_Path_native_1move_1to(JNIEnv *env, jclass this, jlong builder_ptr, jfloat x, jfloat y)
{
	gsk_path_builder_move_to(_PTR(builder_ptr), x, y);
}

JNIEXPORT void JNICALL Java_android_graphics_Path_native_1line_1to(JNIEnv *env, jclass this, jlong builder_ptr, jfloat x, jfloat y)
{
	gsk_path_builder_line_to(_PTR(builder_ptr), x, y);
}

/* translated to gsk_path_builder_svg_arc_to, maybe there are more performant ways to implement this? */
void Java_android_graphics_Path_native_1arc_1to(JNIEnv *env, jclass this, jlong builder_ptr, jfloat left, jfloat top, jfloat right, jfloat bottom,
                                                jfloat start_angle_deg, jfloat sweep_angle_deg, jboolean force_move_to)
{
	GskPathBuilder *builder = _PTR(builder_ptr);

	/* compute ellipse center and radii */
	const graphene_point_t center = GRAPHENE_POINT_INIT((left + right) / 2.0,
	                                                    (top + bottom) / 2.0);
	const double rx = fabs(right - left) / 2.0;
	const double ry = fabs(bottom - top) / 2.0;

	/* compute points on the ellipse from angles */
	const double start_angle = DEG2RAD(start_angle_deg);
	const double end_angle = DEG2RAD(start_angle_deg + sweep_angle_deg);

	graphene_point_t p1;
	graphene_point_t p2;
	graphene_point_init(&p1,
	                    center.x + rx * cos(start_angle),
	                    center.y + ry * sin(start_angle));
	graphene_point_init(&p2,
	                    center.x + rx * cos(end_angle),
	                    center.y + ry * sin(end_angle));

	/* handle force_move_to */
	if (force_move_to)
		gsk_path_builder_move_to(builder, p1.x, p1.y);
	else if (!graphene_point_equal(gsk_path_builder_get_current_point(builder), &p1))
		gsk_path_builder_line_to(builder, p1.x, p1.y);

	/* compute flags */
	const gboolean large_arc_flag = (fabs(sweep_angle_deg) >= 180.0);
	const gboolean sweep_flag = (sweep_angle_deg > 0.0);

	gsk_path_builder_svg_arc_to(builder, rx, ry, 0.0, large_arc_flag, sweep_flag, p2.x, p2.y);
}

JNIEXPORT void JNICALL Java_android_graphics_Path_native_1cubic_1to(JNIEnv *env, jclass this, jlong builder_ptr, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat x3, jfloat y3)
{
	gsk_path_builder_cubic_to(_PTR(builder_ptr), x1, y1, x2, y2, x3, y3);
}

JNIEXPORT void JNICALL Java_android_graphics_Path_native_1quad_1to(JNIEnv *env, jclass this, jlong builder_ptr, jfloat x1, jfloat y1, jfloat x2, jfloat y2)
{
	gsk_path_builder_quad_to(_PTR(builder_ptr), x1, y1, x2, y2);
}

JNIEXPORT void JNICALL Java_android_graphics_Path_native_1rel_1move_1to(JNIEnv *env, jclass this, jlong builder_ptr, jfloat x, jfloat y)
{
	gsk_path_builder_rel_move_to(_PTR(builder_ptr), x, y);
}

JNIEXPORT void JNICALL Java_android_graphics_Path_native_1rel_1line_1to(JNIEnv *env, jclass this, jlong builder_ptr, jfloat x, jfloat y)
{
	gsk_path_builder_rel_line_to(_PTR(builder_ptr), x, y);
}

JNIEXPORT void JNICALL Java_android_graphics_Path_native_1rel_1cubic_1to(JNIEnv *env, jclass this, jlong builder_ptr, jfloat x1, jfloat y1, jfloat x2, jfloat y2, jfloat x3, jfloat y3)
{
	gsk_path_builder_rel_cubic_to(_PTR(builder_ptr), x1, y1, x2, y2, x3, y3);
}

JNIEXPORT void JNICALL Java_android_graphics_Path_native_1rel_1quad_1to(JNIEnv *env, jclass this, jlong builder_ptr, jfloat x1, jfloat y1, jfloat x2, jfloat y2)
{
	gsk_path_builder_rel_quad_to(_PTR(builder_ptr), x1, y1, x2, y2);
}

struct path_foreach_data {
	GskPathBuilder *builder;
	graphene_matrix_t *matrix;
	graphene_point_t tmp_pts[4];
};
static gboolean path_foreach_transform(GskPathOperation op, const graphene_point_t *pts, gsize n_pts, float weight, gpointer user_data)
{
	struct path_foreach_data *data = user_data;
	for (gsize i = 0; i < n_pts; i++) {
		graphene_matrix_transform_point(data->matrix, &pts[i], &data->tmp_pts[i]);
	}
	switch (op) {
		case GSK_PATH_MOVE:
			gsk_path_builder_move_to(data->builder, data->tmp_pts[0].x, data->tmp_pts[0].y);
			break;
		case GSK_PATH_CLOSE:
			gsk_path_builder_close(data->builder);
			break;
		case GSK_PATH_LINE:
			gsk_path_builder_line_to(data->builder, data->tmp_pts[1].x, data->tmp_pts[1].y);
			break;
		case GSK_PATH_QUAD:
			gsk_path_builder_quad_to(data->builder, data->tmp_pts[1].x, data->tmp_pts[1].y, data->tmp_pts[2].x, data->tmp_pts[2].y);
			break;
		case GSK_PATH_CUBIC:
			gsk_path_builder_cubic_to(data->builder, data->tmp_pts[1].x, data->tmp_pts[1].y, data->tmp_pts[2].x, data->tmp_pts[2].y, data->tmp_pts[3].x, data->tmp_pts[3].y);
			break;
		case GSK_PATH_CONIC:
			gsk_path_builder_conic_to(data->builder, data->tmp_pts[1].x, data->tmp_pts[1].y, data->tmp_pts[2].x, data->tmp_pts[2].y, weight);
			break;
	}
	return TRUE;
}

void Java_android_graphics_Path_native_1add_1arc(JNIEnv *env, jclass this, jlong builder_ptr, jfloat left, jfloat top, jfloat right, jfloat bottom,
                                                 jfloat start_angle_deg, jfloat sweep_angle_deg)
{
	GskPathBuilder *builder = _PTR(builder_ptr);
	GskPath *path;
	GskPathBuilder *arc_builder = gsk_path_builder_new();
	Java_android_graphics_Path_native_1arc_1to(env, this, _INTPTR(arc_builder), left, top, right, bottom, start_angle_deg, sweep_angle_deg, true);
	path = gsk_path_builder_free_to_path(arc_builder);
	gsk_path_builder_add_path(builder, path);
}

JNIEXPORT void JNICALL Java_android_graphics_Path_native_1add_1path(JNIEnv *env, jclass this, jlong builder_ptr, jlong path_ptr, jlong matrix_ptr)
{
	GskPathBuilder *builder = _PTR(builder_ptr);
	GskPath *path = _PTR(path_ptr);
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	if (graphene_matrix_is_identity(matrix)) {
		gsk_path_builder_add_path(builder, path);
	} else {
		struct path_foreach_data data = {
			.builder = builder,
			.matrix = matrix,
		};
		gsk_path_foreach(path, GSK_PATH_FOREACH_ALLOW_QUAD | GSK_PATH_FOREACH_ALLOW_CUBIC | GSK_PATH_FOREACH_ALLOW_CONIC, path_foreach_transform, &data);
	}
}

JNIEXPORT jlong JNICALL Java_android_graphics_Path_native_1transform(JNIEnv *env, jclass this, jlong path_ptr, jlong matrix_ptr)
{
	GskPath *path = _PTR(path_ptr);
	graphene_matrix_t *matrix = (graphene_matrix_t *)_PTR(matrix_ptr);
	struct path_foreach_data data = {
		.builder = gsk_path_builder_new(),
		.matrix = matrix,
	};
	gsk_path_foreach(path, GSK_PATH_FOREACH_ALLOW_QUAD | GSK_PATH_FOREACH_ALLOW_CUBIC | GSK_PATH_FOREACH_ALLOW_CONIC, path_foreach_transform, &data);
	gsk_path_unref(path);
	return _INTPTR(data.builder);
}

JNIEXPORT void JNICALL Java_android_graphics_Path_native_1add_1rect(JNIEnv *env, jclass this, jlong builder_ptr, jfloat left, jfloat top, jfloat right, jfloat bottom)
{
	gsk_path_builder_add_rect(_PTR(builder_ptr), &GRAPHENE_RECT_INIT(left, top, right - left, bottom - top));
}

JNIEXPORT void JNICALL Java_android_graphics_Path_native_1add_1round_1rect(JNIEnv *env, jclass this, jlong builder_ptr, jfloat left, jfloat top, jfloat right, jfloat bottom, jfloatArray radii_jobj)
{
	jfloat *radii = (*env)->GetFloatArrayElements(env, radii_jobj, NULL);
	GskRoundedRect round_rect = {
		.bounds = GRAPHENE_RECT_INIT(left, top, right - left, bottom - top),
		.corner = {{radii[0], radii[1]}, {radii[2], radii[3]}, {radii[4], radii[5]}, {radii[6], radii[7]}},
	};
	(*env)->ReleaseFloatArrayElements(env, radii_jobj, radii, 0);
	gsk_path_builder_add_rounded_rect(_PTR(builder_ptr), &round_rect);
}

JNIEXPORT void JNICALL Java_android_graphics_Path_native_1get_1bounds(JNIEnv *env, jclass this, jlong path_ptr, jobject bounds)
{
	graphene_rect_t rect;
	gsk_path_get_bounds(_PTR(path_ptr), &rect);
	_SET_FLOAT_FIELD(bounds, "left", rect.origin.x);
	_SET_FLOAT_FIELD(bounds, "top", rect.origin.y);
	_SET_FLOAT_FIELD(bounds, "right", rect.origin.x + rect.size.width);
	_SET_FLOAT_FIELD(bounds, "bottom", rect.origin.y + rect.size.height);
}

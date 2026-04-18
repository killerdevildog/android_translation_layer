#include <gsk/gsk.h>
#include <gtk/gtk.h>

#include "../defines.h"

#include "../generated_headers/android_view_RenderNode.h"

JNIEXPORT jlong JNICALL Java_android_view_RenderNode_nativeCreateSnapshot(JNIEnv *env, jobject this)
{
	return _INTPTR(gtk_snapshot_new());
}

JNIEXPORT jlong JNICALL Java_android_view_RenderNode_nativeCreateNode(JNIEnv *env, jobject this, jlong snapshot_ptr)
{
	GskRenderNode *node = gtk_snapshot_free_to_node(GTK_SNAPSHOT(_PTR(snapshot_ptr)));
	if (!node)
		node = gsk_container_node_new(NULL, 0);
	return _INTPTR(node);
}

static GskRenderNode *patch_node(GskRenderNode *node, GskRenderNode *old_child, GskRenderNode *new_child)
{
	if (node == old_child)
		return gsk_render_node_ref(new_child);
	GskRenderNode *new_node;
	GskRenderNodeType type = gsk_render_node_get_node_type(node);
	switch (type) {
		case GSK_CONTAINER_NODE: {
			guint n_children = gsk_container_node_get_n_children(node);
			GskRenderNode *children[n_children];
			gboolean modified = FALSE;
			for (guint i = 0; i < n_children; i++) {
				children[i] = patch_node(gsk_container_node_get_child(node, i), old_child, new_child);
				modified |= children[i] != gsk_container_node_get_child(node, i);
			}
			if (modified)
				new_node = gsk_container_node_new(children, n_children);
			else
				new_node = gsk_render_node_ref(node);
			for (guint i = 0; i < n_children; i++)
				gsk_render_node_unref(children[i]);
			break;
		}
		case GSK_TRANSFORM_NODE: {
			GskRenderNode *child = patch_node(gsk_transform_node_get_child(node), old_child, new_child);
			if (child != gsk_transform_node_get_child(node))
				new_node = gsk_transform_node_new(child, gsk_transform_node_get_transform(node));
			else
				new_node = gsk_render_node_ref(node);
			gsk_render_node_unref(child);
			break;
		}
		case GSK_CLIP_NODE: {
			GskRenderNode *child = patch_node(gsk_clip_node_get_child(node), old_child, new_child);
			if (child != gsk_clip_node_get_child(node))
				new_node = gsk_clip_node_new(child, gsk_clip_node_get_clip(node));
			else
				new_node = gsk_render_node_ref(node);
			gsk_render_node_unref(child);
			break;
		}
		default:
			new_node = gsk_render_node_ref(node);
	}
	return new_node;
}

JNIEXPORT jlong JNICALL Java_android_view_RenderNode_nativePatchNode(JNIEnv *env, jobject this, jlong node_ptr, jlong old_child_ptr, jlong new_child_ptr)
{
	GskRenderNode *old_node = _PTR(node_ptr);
	GskRenderNode *new_node = patch_node(old_node, _PTR(old_child_ptr), _PTR(new_child_ptr));
	gsk_render_node_unref(old_node);
	return _INTPTR(new_node);
}

JNIEXPORT jlong JNICALL Java_android_view_RenderNode_nativeTransform(JNIEnv *env, jobject this, jlong node_ptr, jfloat scale_x, jfloat scale_y, jfloat translate_x, jfloat translate_y, jfloat rotate, jfloat pivot_x, jfloat pivot_y)
{
	GskRenderNode *node = _PTR(node_ptr);
	GskTransform *transform = NULL;
	GskRenderNode *transformed_node;
	transform = gsk_transform_translate(transform, &GRAPHENE_POINT_INIT(-pivot_x, -pivot_y));
	transform = gsk_transform_scale(transform, scale_x, scale_y);
	transform = gsk_transform_rotate(transform, rotate);
	transform = gsk_transform_translate(transform, &GRAPHENE_POINT_INIT(translate_x + pivot_x, translate_y + pivot_y));
	if (transform) {
		transformed_node = gsk_transform_node_new(node, transform);
		gsk_transform_unref(transform);
	} else {
		transformed_node = gsk_render_node_ref(node);
	}
	return _INTPTR(transformed_node);
}

JNIEXPORT jlong JNICALL Java_android_view_RenderNode_nativeClip(JNIEnv *env, jobject this, jlong node_ptr, jfloat left, jfloat top, jfloat right, jfloat bottom)
{
	GskRenderNode *node = _PTR(node_ptr);
	GskRenderNode *clipped_node = gsk_clip_node_new(node, &GRAPHENE_RECT_INIT(left, top, right - left, bottom - top));
	gsk_render_node_unref(node);
	return _INTPTR(clipped_node);
}

JNIEXPORT void JNICALL Java_android_view_RenderNode_nativeUnref(JNIEnv *env, jobject this, jlong node_ptr)
{
	if (node_ptr != 0)
		gsk_render_node_unref(_PTR(node_ptr));
}

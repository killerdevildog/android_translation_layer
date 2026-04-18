#include <arpa/inet.h>
#include <graphene.h>
#include <gtk/gtk.h>

#include "NinePatchPaintable.h"

enum {
	// The 9 patch segment is not a solid color.
	NO_COLOR = 0x00000001,
	// The 9 patch segment is completely transparent.
	TRANSPARENT_COLOR = 0x00000000
};

static void ninepatch_paintable_snapshot(GdkPaintable *paintable, GdkSnapshot *snapshot, double width, double height)
{
	int i, j;
	NinePatchPaintable *ninepatch = NINEPATCH_PAINTABLE(paintable);
	struct Res_png_9patch *chunk = ninepatch->chunk;
	if (chunk) {
		int32_t *xDivs = (void *)chunk + chunk->xDivsOffset;
		int32_t *yDivs = (void *)chunk + chunk->yDivsOffset;
		int32_t *color = (void *)chunk + chunk->colorsOffset;
		float strech_factor_width = (width - (ninepatch->width - ninepatch->strechy_width)) / ninepatch->strechy_width;
		float strech_factor_height = (height - (ninepatch->height - ninepatch->strechy_height)) / ninepatch->strechy_height;

		graphene_rect_t rect;
		GdkRGBA rgba;
		if (ninepatch->tint) {
			graphene_matrix_t color_matrix;
			graphene_vec4_t color_offset;
			/* clang-format off */
			graphene_matrix_init_from_float(&color_matrix, (float[]){
				0, 0, 0, 0,
				0, 0, 0, 0,
				0, 0, 0, 0,
				0, 0, 0, ((ninepatch->tint >> 24) & 0xFF) / 255.f,
			});
			/* clang-format on */
			graphene_vec4_init(&color_offset, ((ninepatch->tint >> 16) & 0xFF) / 255.f, ((ninepatch->tint >> 8) & 0xFF) / 255.f, ((ninepatch->tint >> 0) & 0xFF) / 255.f, 0);
			gtk_snapshot_push_color_matrix(snapshot, &color_matrix, &color_offset);
		}
		for (j = 0, rect.origin.y = 0; j < chunk->numYDivs + 1; j++, rect.origin.y += rect.size.height) {
			int ydiv_start = j ? yDivs[j - 1] : 0;
			int ydiv_end = (j == chunk->numYDivs) ? ninepatch->height : yDivs[j];
			float actual_stretch_factor_height = (j % 2) ? strech_factor_height : 1;
			float patch_height = ydiv_end - ydiv_start;
			if (j % 2) // odd sections are stretchable
				patch_height *= strech_factor_height;
			rect.size.height = patch_height;
			if (!patch_height) // skip empty sections
				continue;
			for (i = 0, rect.origin.x = 0; i < chunk->numXDivs + 1; i++, rect.origin.x += rect.size.width) {
				int xdiv_start = i ? xDivs[i - 1] : 0;
				int xdiv_end = (i == chunk->numXDivs) ? ninepatch->width : xDivs[i];
				float actual_stretch_factor_width = (i % 2) ? strech_factor_width : 1;
				float patch_width = xdiv_end - xdiv_start;
				if (i % 2) // odd sections are stretchable
					patch_width *= strech_factor_width;
				rect.size.width = patch_width;
				if (!patch_width) // skip empty sections
					continue;
				if (*color == NO_COLOR) {
					gtk_snapshot_push_clip(snapshot, &rect);
					graphene_rect_t texture_bounds = GRAPHENE_RECT_INIT(rect.origin.x - xdiv_start * actual_stretch_factor_width,
					                                                    rect.origin.y - ydiv_start * actual_stretch_factor_height,
					                                                    gdk_texture_get_width(ninepatch->texture) * actual_stretch_factor_width,
					                                                    gdk_texture_get_height(ninepatch->texture) * actual_stretch_factor_height);
					gtk_snapshot_append_texture(snapshot, ninepatch->texture, &texture_bounds);
					gtk_snapshot_pop(snapshot);
				} else if (*color != TRANSPARENT_COLOR) {
					rgba.alpha = (*color >> 24 & 0xFF) / 255.f;
					rgba.red = (*color >> 16 & 0xFF) / 255.f;
					rgba.green = (*color >> 8 & 0xFF) / 255.f;
					rgba.blue = (*color & 0xFF) / 255.f;
					gtk_snapshot_append_color(snapshot, &rgba, &rect);
				}
				color++;
			}
		}
		if (ninepatch->tint)
			gtk_snapshot_pop(snapshot);
	}
}

static GdkPaintableFlags ninepatch_paintable_get_flags(GdkPaintable *paintable)
{
	return GDK_PAINTABLE_STATIC_CONTENTS | GDK_PAINTABLE_STATIC_SIZE;
}

static void ninepatch_paintable_init(NinePatchPaintable *ninepatch_paintable)
{
}

static void ninepatch_paintable_paintable_init(GdkPaintableInterface *iface)
{
	iface->snapshot = ninepatch_paintable_snapshot;
	iface->get_flags = ninepatch_paintable_get_flags;
}

static void ninepatch_paintable_class_init(NinePatchPaintableClass *class)
{
}

G_DEFINE_TYPE_WITH_CODE(NinePatchPaintable, ninepatch_paintable, G_TYPE_OBJECT,
                        G_IMPLEMENT_INTERFACE(GDK_TYPE_PAINTABLE, ninepatch_paintable_paintable_init))

GdkPaintable *ninepatch_paintable_new(struct Res_png_9patch *chunk, uint32_t chunk_size, GdkTexture *texture)
{
	int width = 0;
	int height = 0;
	int strechy_width = 0;
	int strechy_height = 0;
	int i;

	if (chunk->wasDeserialized) {
		chunk->xDivsOffset = sizeof(struct Res_png_9patch);
		chunk->yDivsOffset = chunk->xDivsOffset + chunk->numXDivs * sizeof(int32_t);
		chunk->colorsOffset = chunk->yDivsOffset + chunk->numYDivs * sizeof(int32_t);
	}

	// verify that that all arrays are fully inside the chunk bounds
	if (chunk->xDivsOffset > chunk_size || chunk->yDivsOffset > chunk_size || chunk->colorsOffset > chunk_size
	    || chunk->numXDivs > (chunk_size - chunk->xDivsOffset) / sizeof(int32_t)
	    || chunk->numYDivs > (chunk_size - chunk->yDivsOffset) / sizeof(int32_t)
	    || chunk->numColors > (chunk_size - chunk->colorsOffset) / sizeof(int32_t))
		return NULL;

	int32_t *xDivs = (void *)chunk + chunk->xDivsOffset;
	int32_t *yDivs = (void *)chunk + chunk->yDivsOffset;
	int32_t *colors = (void *)chunk + chunk->colorsOffset;

	if (!chunk->wasDeserialized) {
		for (i = 0; i < chunk->numXDivs; i++)
			xDivs[i] = ntohl(xDivs[i]);
		for (i = 0; i < chunk->numYDivs; i++)
			yDivs[i] = ntohl(yDivs[i]);
		for (i = 0; i < chunk->numColors; i++)
			colors[i] = ntohl(colors[i]);
	}

	width = gdk_texture_get_width(texture);
	height = gdk_texture_get_height(texture);

	for (i = 1; i < chunk->numXDivs + 1; i += 2)
		strechy_width += (i == chunk->numXDivs ? width : xDivs[i]) - xDivs[i - 1];
	for (i = 1; i < chunk->numYDivs + 1; i += 2)
		strechy_height += (i == chunk->numYDivs ? height : yDivs[i]) - yDivs[i - 1];

	NinePatchPaintable *ninepatch = NINEPATCH_PAINTABLE(g_object_new(ninepatch_paintable_get_type(), NULL));
	ninepatch->chunk = chunk;
	ninepatch->width = width;
	ninepatch->height = height;
	ninepatch->strechy_width = strechy_width;
	ninepatch->strechy_height = strechy_height;
	ninepatch->texture = texture;
	return GDK_PAINTABLE(ninepatch);
}

#include <gdk/gdk.h>

struct Res_png_9patch {
	int8_t wasDeserialized;
	uint8_t numXDivs;
	uint8_t numYDivs;
	uint8_t numColors;
	// The offset (from the start of this structure) to the xDivs & yDivs
	// array for this 9patch. To get a pointer to this array, call
	// getXDivs or getYDivs. Note that the serialized form for 9patches places
	// the xDivs, yDivs and colors arrays immediately after the location
	// of the Res_png_9patch struct.
	uint32_t xDivsOffset;
	uint32_t yDivsOffset;
	int32_t paddingLeft, paddingRight;
	int32_t paddingTop, paddingBottom;
	// The offset (from the start of this structure) to the colors array
	// for this 9patch.
	uint32_t colorsOffset;
} __attribute__((packed));

struct _NinePatchPaintable {
	GObject parent_instance;
	GdkTexture *texture;
	struct Res_png_9patch *chunk;
	int width;
	int height;
	int strechy_width;
	int strechy_height;
	int tint;
};
G_DECLARE_FINAL_TYPE(NinePatchPaintable, ninepatch_paintable, NINEPATCH, PAINTABLE, GObject)

GdkPaintable *ninepatch_paintable_new(struct Res_png_9patch *chunk, uint32_t chunk_size, GdkTexture *texture);

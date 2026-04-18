#include <gio/gio.h>

struct _AssetInputStream {
	GInputStream parent_instance;
	struct Asset *asset;
};

G_DECLARE_FINAL_TYPE(AssetInputStream, asset_input_stream, ATL, ASSET_INPUT_STREAM, GInputStream);

GInputStream *asset_input_stream_new(struct Asset *asset);

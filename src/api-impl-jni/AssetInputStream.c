#include <androidfw/androidfw_c_api.h>

#include "AssetInputStream.h"

static gssize asset_input_stream_read(GInputStream *gstream, void *buffer, gsize count, GCancellable *cancellable, GError **error)
{
	return Asset_read(ATL_ASSET_INPUT_STREAM(gstream)->asset, buffer, count);
}

static gboolean asset_input_stream_close(GInputStream *gstream, GCancellable *cancellable, GError **error)
{
	AssetInputStream *stream = ATL_ASSET_INPUT_STREAM(gstream);
	Asset_delete(stream->asset);
	stream->asset = NULL;
	return TRUE;
}

static void asset_input_stream_class_init(AssetInputStreamClass *class)
{
	class->parent_class.read_fn = asset_input_stream_read;
	class->parent_class.close_fn = asset_input_stream_close;
}

static void asset_input_stream_init(AssetInputStream *self)
{
}

G_DEFINE_TYPE(AssetInputStream, asset_input_stream, G_TYPE_INPUT_STREAM)

GInputStream *asset_input_stream_new(struct Asset *asset)
{
	AssetInputStream *stream = g_object_new(asset_input_stream_get_type(), NULL);
	stream->asset = asset;
	return &stream->parent_instance;
}

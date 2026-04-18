#include <gio/gunixinputstream.h>
#include <gtk/gtk.h>
#include <webkit/webkit.h>

#include <androidfw/androidfw_c_api.h>

#include "../defines.h"
#include "../util.h"

#include "../AssetInputStream.h"
#include "WrapperWidget.h"

#include "../generated_headers/android_view_View.h"
#include "../generated_headers/android_webkit_WebView.h"

static void asset_uri_scheme_request_cb(WebKitURISchemeRequest *request, gpointer user_data)
{
	const gchar *path = webkit_uri_scheme_request_get_path(request);
	if (path == NULL)
		return;
	path += 1; // remove the leading '/'
	JNIEnv *env = get_jni_env();
	WrapperWidget *wrapper = WRAPPER_WIDGET(gtk_widget_get_parent(GTK_WIDGET(webkit_uri_scheme_request_get_web_view(request))));
	jobject asset_manager_obj = (*env)->CallObjectMethod(env, wrapper->jobj, handle_cache.webview.internalGetAssetManager);
	struct AssetManager *asset_manager = _PTR(_GET_LONG_FIELD(asset_manager_obj, "mObject"));
	struct Asset *asset = AssetManager_openNonAsset(asset_manager, path, ACCESS_STREAMING);
	GInputStream *stream = asset_input_stream_new(asset);
	webkit_uri_scheme_request_finish(request, stream, Asset_getLength(asset), NULL);
	g_object_unref(stream);
}

static void web_view_load_changed(WebKitWebView *web_view, WebKitLoadEvent load_event, gpointer user_data)
{
	WrapperWidget *wrapper = WRAPPER_WIDGET(gtk_widget_get_parent(GTK_WIDGET(web_view)));
	JNIEnv *env = get_jni_env();
	(*env)->CallVoidMethod(env, wrapper->jobj, handle_cache.webview.internalLoadChanged, load_event, _JSTRING(webkit_web_view_get_uri(web_view)));
}

JNIEXPORT jlong JNICALL Java_android_webkit_WebView_native_1constructor(JNIEnv *env, jobject this, jobject context, jobject attrs)
{
	/*
	 * many apps use webview just for fingerprinting or displaying ads, which seems like
	 * a waste of resources even if we deal with fingerprinting and ads in some other way
	 * in the future.
	 */
	if (!getenv("ATL_UGLY_ENABLE_WEBVIEW"))
		return Java_android_view_View_native_1constructor(env, this, context, attrs);

	GtkWidget *wrapper = g_object_ref(wrapper_widget_new());
	GtkWidget *webview = webkit_web_view_new();
	wrapper_widget_set_child(WRAPPER_WIDGET(wrapper), webview);
	wrapper_widget_set_jobject(WRAPPER_WIDGET(wrapper), env, this);
	webkit_web_context_register_uri_scheme(webkit_web_view_get_context(WEBKIT_WEB_VIEW(webview)), "android-asset", asset_uri_scheme_request_cb, NULL, NULL);
	g_signal_connect(G_OBJECT(webview), "load-changed", G_CALLBACK(web_view_load_changed), NULL);
	return _INTPTR(webview);
}

JNIEXPORT void JNICALL Java_android_webkit_WebView_native_1loadUrl(JNIEnv *env, jobject this, jlong widget_ptr, jstring url)
{
	if (!getenv("ATL_UGLY_ENABLE_WEBVIEW"))
		return;

	WebKitWebView *webview = _PTR(widget_ptr);
	webkit_web_view_load_uri(webview, _CSTRING(url));
}

JNIEXPORT void JNICALL Java_android_webkit_WebView_native_1loadDataWithBaseURL(JNIEnv *env, jobject this, jlong widget_ptr, jstring base_url, jstring data_jstr, jstring mime_type, jstring encoding)
{
	if (!getenv("ATL_UGLY_ENABLE_WEBVIEW"))
		return;

	WebKitWebView *webview = _PTR(widget_ptr);
	jsize data_len = (*env)->GetStringUTFLength(env, data_jstr);
	jsize data_jlen = (*env)->GetStringLength(env, data_jstr);
	char *data = malloc(data_len + 1); // + 1 for NUL
	(*env)->GetStringUTFRegion(env, data_jstr, 0, data_jlen, data);
	webkit_web_view_load_bytes(webview, g_bytes_new(data, data_len), mime_type ? _CSTRING(mime_type) : "text/html", encoding ? _CSTRING(encoding) : NULL, base_url ? _CSTRING(base_url) : NULL);
}

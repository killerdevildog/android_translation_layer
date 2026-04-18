// for dladdr
#define _GNU_SOURCE

#include <cairo-svg.h>
#include <gdk/wayland/gdkwayland.h>
#include <gtk/gtk.h>
#include <libportal/portal.h>

#include "../api-impl-jni/defines.h"
#include "../api-impl-jni/util.h"
#include "../api-impl-jni/app/android_app_Activity.h"

#include "actions.h"
#include "back_button.h"
#include "libc_bio_path_overrides.h"

#include <dlfcn.h>
#include <errno.h>
#include <libgen.h>
#include <locale.h>
#include <stdio.h>
#include <sys/resource.h>
#include <sys/stat.h>

#ifndef DEFFILEMODE
	#define DEFFILEMODE (S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP | S_IROTH | S_IWOTH) /* 0666*/
#endif

#ifdef __x86_64__
	#define NATIVE_ARCH "x86_64"
#elifdef __i386__
	#define NATIVE_ARCH "x86"
#elifdef __aarch64__
	#define NATIVE_ARCH "arm64-v8a"
#elifdef __arm__
	#define NATIVE_ARCH "armeabi-v7a"
#else
	#error unknown native architecture
#endif

GtkWidget *window;
char *apk_path;

// standard Gtk Application stuff, more or less

gboolean app_exit(GtkWindow *self, JNIEnv *env) // TODO: do more cleanup?
{
	activity_close_all();
	return false;
}

// this is the equivalent of /data/data/com.example.app/
char *app_data_dir = NULL;
char *get_app_data_dir()
{
	return app_data_dir;
}

char *construct_classpath(char *prefix, char **cp_array, size_t len)
{
	size_t result_len = strlen(prefix);
	for (int i = 0; i < len; i++) {
		if (cp_array[i])
			result_len += strlen(cp_array[i]) + 1; // the 1 is for either : or the final \0
	}

	char *result = malloc(result_len);
	strcpy(result, prefix);
	for (int i = 0; i < len; i++) {
		if (cp_array[i]) {
			if (i > 0)
				strcat(result, ":");
			strcat(result, cp_array[i]);
		}
	}

	return result;
}

#define JDWP_ARG    "-XjdwpOptions:transport=dt_socket,server=y,suspend=y,address="
#define SDK_INT_ARG "-DBuild.VERSION.SDK_INT="

JNIEnv *create_vm(char *api_impl_jar, char *apk_classpath, char *framework_res_apk, char *test_runner_jar, char *api_impl_natives_dir, char *app_lib_dir, char *sdk_int, char **extra_jvm_options)
{
	JavaVM *jvm;
	JNIEnv *env;
	JavaVMInitArgs args = {
		.version = JNI_VERSION_1_6,
		.nOptions = 3,
	};
	JavaVMOption *options;

	int option_counter = args.nOptions;

	char jdwp_option_string[sizeof(JDWP_ARG) + 5] = JDWP_ARG;          // 5 chars for port number, NULL byte is counted by sizeof
	char sdk_int_option_string[sizeof(SDK_INT_ARG) + 2] = SDK_INT_ARG; // 2 chars for SDK_INT, NULL byte is counted by sizeof

	const char *jdwp_port = getenv("JDWP_LISTEN");

	if (jdwp_port)
		args.nOptions += 2;
	if (sdk_int)
		args.nOptions += 1;
	if (extra_jvm_options)
		args.nOptions += g_strv_length(extra_jvm_options);
	options = malloc(sizeof(JavaVMOption) * args.nOptions);

	if (getenv("RUN_FROM_BUILDDIR")) {
		options[0].optionString = construct_classpath("-Djava.library.path=", (char *[]){"./", app_lib_dir}, 2);
	} else {
		options[0].optionString = construct_classpath("-Djava.library.path=", (char *[]){api_impl_natives_dir, app_lib_dir}, 2);
	}

	options[1].optionString = construct_classpath("-Djava.class.path=", (char *[]){api_impl_jar, apk_classpath, framework_res_apk, test_runner_jar}, 4);
	options[2].optionString = "-Xcheck:jni";
	if (jdwp_port) {
		strncat(jdwp_option_string, jdwp_port, 5); // 5 chars is enough for a port number, and won't overflow our array
		options[option_counter++].optionString = "-XjdwpProvider:internal";
		options[option_counter++].optionString = jdwp_option_string;
	}

	if (sdk_int) {
		strncat(sdk_int_option_string, sdk_int, 2); // 2 chars should be enough for the foreseeable future, and won't overflow our array
		options[option_counter++].optionString = sdk_int_option_string;
	}

	while (extra_jvm_options && *extra_jvm_options) {
		options[option_counter++].optionString = *(extra_jvm_options++);
	}

	args.options = options;
	args.ignoreUnrecognized = JNI_FALSE;

	int ret = JNI_CreateJavaVM(&jvm, (void **)&env, &args);
	if (ret < 0) {
		fprintf(stderr, "Unable to Launch JVM\n");
	} else {
		fprintf(stderr, "JVM launched successfully\n");
	}

	free(options);
	return env;
}

void icon_override(GtkWidget *window, GList *icon_list)
{
	GdkSurface *window_surface = gtk_native_get_surface(GTK_NATIVE(window));
	// set app icon as window icon; this is a noop on Wayland because there is currently no way to set a window icon on Wayland
	gdk_toplevel_set_icon_list(GDK_TOPLEVEL(window_surface), icon_list);
}

/*
 * There is no way to get a nice clean callback for when the window is ready to be used for stuff
 * that requires non-zero dimensions, so we just check periodically
 */
gboolean hacky_on_window_focus_changed_callback(JNIEnv *env)
{
	if (gtk_widget_get_width(window) != 0) {
		activity_window_ready();
		return G_SOURCE_REMOVE;
	}

	return G_SOURCE_CONTINUE;
}

struct dynamic_launcher_callback_data {
	char *desktop_file_id;
	char *desktop_entry;
};
static void dynamic_launcher_ready_callback(GObject *portal, GAsyncResult *res, gpointer user_data)
{
	struct dynamic_launcher_callback_data *data = user_data;
	GVariant *result = xdp_portal_dynamic_launcher_prepare_install_finish(XDP_PORTAL(portal), res, NULL);
	if (!result) {
		fprintf(stderr, "cancelled\n");
		exit(0);
	}
	const char *token;
	g_variant_lookup(result, "token", "s", &token);
	GError *err = NULL;
	xdp_portal_dynamic_launcher_install(XDP_PORTAL(portal), token, data->desktop_file_id, data->desktop_entry, &err);
	g_free(data->desktop_file_id);
	g_free(data->desktop_entry);
	g_free(data);
	if (err) {
		fprintf(stderr, "failed to install dynamic launcher: %s\n", err->message);
		exit(1);
	}
	// run update-desktop-database to add the new x-scheme-handler entries to ~/.local/share/applications/mimeinfo.cache
	char *update_desktop_database = g_strdup_printf("update-desktop-database %s/applications", g_get_user_data_dir());
	printf("running: `%s`\n", update_desktop_database);
	system(update_desktop_database);
	g_free(update_desktop_database);

	exit(0);
}

static cairo_status_t cairo_write_func_gstring(void *closure, const unsigned char *data, unsigned int length)
{
	GString *str = closure;
	g_string_append_len(str, (gchar *)data, length);
	return CAIRO_STATUS_SUCCESS;
}

// this is exported by the shim bionic linker
void dl_parse_library_path(const char *path, char *delim);

#define REL_DEX_INSTALL_PATH              "../java/dex"

#define REL_API_IMPL_JAR_INSTALL_PATH     "android_translation_layer/api-impl.jar"
#define REL_API_IMPL_NATIVES_INSTALL_PATH "android_translation_layer/natives"
#define REL_FRAMEWORK_RES_INSTALL_PATH    "android_translation_layer/framework-res.apk"
#define REL_TEST_RUNNER_JAR_INSTALL_PATH  "android_translation_layer/test_runner.jar"

#define API_IMPL_JAR_PATH_LOCAL           "./api-impl.jar"
#define FRAMEWORK_RES_PATH_LOCAL          "./res/framework-res/framework-res.apk"
#define TEST_RUNNER_JAR_PATH_LOCAL        "./test_runner.jar"

struct jni_callback_data {
	char *apk_main_activity_class;
	char *apk_instrumentation_class;
	uint32_t window_width;
	uint32_t window_height;
	gboolean install;
	gboolean install_internal;
	char *prgname;
	char **extra_jvm_options;
	char **extra_string_keys;
	char *sdk_int;
};

static char *uri_option = NULL;

static void parse_string_extras(JNIEnv *env, char **extra_string_keys, jobject intent)
{
	GError *error = NULL;
	GRegex *regex = g_regex_new("(?<!\\\\)=", 0, 0, &error);
	if (!regex) {
		fprintf(stderr, "g_regex_new error: '%s'\n", error->message);
		exit(1);
	}

	for (char **arg = extra_string_keys; *arg; arg++) {
		gchar **keyval = g_regex_split_full(regex, *arg, -1, 0, 0, 2, NULL);
		if (!keyval || !keyval[0] || !keyval[1]) {
			fprintf(stderr, "extra string arg not in 'key=value' format: '%s'\n", *arg);
			exit(1);
		}
		(*env)->CallObjectMethod(env, intent, handle_cache.intent.putExtraCharSequence, _JSTRING(keyval[0]), _JSTRING(keyval[1]));
		g_strfreev(keyval);
	}
	g_regex_unref(regex);
}

/* Drag and drop callback to simulate ACTION_SEND intents */
static gboolean on_drop(GtkDropTarget *target, const GValue *value, double x, double y, gpointer user_data)
{
	const char *data = g_value_get_string(value);
	if (!data || !*data) {
		return FALSE;
	}

	JNIEnv *env = get_jni_env();
	jobject intent = (*env)->NewObject(env, handle_cache.intent.class, handle_cache.intent.constructor);
	_SET_OBJ_FIELD(intent, "action", "Ljava/lang/String;", _JSTRING("android.intent.action.SEND"));
	_SET_OBJ_FIELD(intent, "data", "Landroid/net/Uri;", (*env)->CallStaticObjectMethod(env, handle_cache.uri.class, handle_cache.uri.parse, _JSTRING(data)));

	jobject activity = (*env)->CallStaticObjectMethod(env, handle_cache.context.class, handle_cache.context.resolveActivityInternal, intent);
	if ((*env)->ExceptionCheck(env)) {
		(*env)->ExceptionDescribe(env);
	}
	if (!activity) {
		fprintf(stderr, "failed to resolve activity to handle URI: %s\n", data);
		return FALSE;
	}
	activity_start(env, activity);
	return TRUE;
}

char *find_jar_or_die(char *builddir_path, char *installed_path, char *install_prefix)
{
	char *path;

	if (getenv("RUN_FROM_BUILDDIR")) {
		path = strdup(builddir_path); // for running out of builddir; using strdup so we can always safely call free on this
	} else {
		path = g_strdup_printf("%s/%s", install_prefix, installed_path);
	}

	if (access(path, F_OK) < 0) {
		fprintf(stderr, "error: can't stat %s (%s)\n",
		        path, strerror(errno));
		exit(1);
	}

	return path;
}

static void open(GtkApplication *app, GFile **files, gint nfiles, const gchar *hint, struct jni_callback_data *d)
{
	// TODO: pass all files to classpath
	/*
	printf("nfiles: %d\n", nfiles);
	for(int i = 0; i < nfiles; i++) {
		printf(">- [%s]\n", g_file_get_path(files[i]));
	}
*/
	if (window) { // this is not the first launch, but a DBus request to open an URI in the running app
		fprintf(stderr, "opening uri over DBus %p\n", files[0]);
		char *uri = g_file_get_uri(files[0]);
		JNIEnv *env = get_jni_env();
		fprintf(stderr, "opening uri over DBus: %s\n", uri);
		jobject activity = (*env)->CallStaticObjectMethod(env, handle_cache.activity.class,
		                                                  _STATIC_METHOD(handle_cache.activity.class, "createMainActivity", "(Ljava/lang/String;JLjava/lang/String;)Landroid/app/Activity;"),
		                                                  _JSTRING(d->apk_main_activity_class), _INTPTR(window), _JSTRING(uri));
		if ((*env)->ExceptionCheck(env))
			(*env)->ExceptionDescribe(env);
		activity_start(env, activity);
		return;
	}
	char *dex_install_dir;
	char *api_impl_jar;
	char *test_runner_jar = NULL;
	char *framework_res_apk = NULL;
	const char *package_name;
	int ret;
	jobject activity_object;
	jobject application_object;

	char *apk_classpath = g_file_get_path(files[0]);
	char *apk_name = g_file_get_basename(files[0]);

	if (apk_classpath == NULL) {
		fprintf(stderr, "error: the specified file path doesn't seem to be valid\n");
		exit(1);
	}

	if (access(apk_classpath, F_OK) < 0) {
		fprintf(stderr, "error: the specified file path (%s) doesn't seem to exist (%m)\n", apk_classpath);
		exit(1);
	}

	Dl_info libart_so_dl_info;
	// JNI_CreateJavaVM chosen arbitrarily, what matters is that it's a symbol exported by by libart.so
	// TODO: we shouldn't necessarily count on art being installed in the same prefix as we are
	dladdr(JNI_CreateJavaVM, &libart_so_dl_info);
	// make sure we didn't get NULL
	if (libart_so_dl_info.dli_fname) {
		// it's simpler if we can modify the string, so strdup it
		char *libart_so_full_path = strdup(libart_so_dl_info.dli_fname);
		*strrchr(libart_so_full_path, '/') = '\0'; // now we should have something like /usr/lib64/art
		dex_install_dir = g_strdup_printf("%s/%s", libart_so_full_path, REL_DEX_INSTALL_PATH);
		free(libart_so_full_path);
	} else {
		fprintf(stderr, "error: couldn't find art install path\n");
		exit(1);
	}

	char *app_data_dir_base = getenv("ANDROID_APP_DATA_DIR");
	if (!app_data_dir_base) {
		const char *user_data_dir = g_get_user_data_dir();
		if (user_data_dir) {
			app_data_dir_base = g_strdup_printf("%s/android_translation_layer", user_data_dir);
			ret = mkdir(app_data_dir_base, DEFFILEMODE | S_IXUSR | S_IXGRP | S_IXOTH);
			if (ret) {
				if (errno != EEXIST) {
					fprintf(stderr, "error: ANDROID_APP_DATA_DIR not set, and the default directory (%s) couldn't be created (error: %s)\n", app_data_dir_base, strerror(errno));
					exit(1);
				}
			}
		} else {
			fprintf(stderr, "error: ANDROID_APP_DATA_DIR not set, and HOME is not set either so we can't construct the default path\n");
			exit(1);
		}
	}

	// TODO: we should possibly use the app id instead, but we don't currently have a way to get that soon enough
	// arguably both the app id and the apk name might have an issue with duplicates, but if two apks use the same app id, chances are it's less of an issue than when two apks have the same name
	// !IMPORTANT! Unity can't comprehend that a directory name could end in .apk, so we have to avoid that here by adding `_`
	app_data_dir = g_strdup_printf("%s/%s_/", app_data_dir_base, apk_name);

	ret = mkdir(app_data_dir, DEFFILEMODE | S_IXUSR | S_IXGRP | S_IXOTH);
	if (ret && errno != EEXIST) {
		fprintf(stderr, "can't create app data dir %s (%s)\n", app_data_dir, strerror(errno));
		exit(1);
	}

	// check for jars and apks  in './' (if running from builddir), or in system install path
	api_impl_jar = find_jar_or_die(API_IMPL_JAR_PATH_LOCAL, REL_API_IMPL_JAR_INSTALL_PATH, dex_install_dir);
	framework_res_apk = find_jar_or_die(FRAMEWORK_RES_PATH_LOCAL, REL_FRAMEWORK_RES_INSTALL_PATH, dex_install_dir);
	if (d->apk_instrumentation_class)
		test_runner_jar = find_jar_or_die(TEST_RUNNER_JAR_PATH_LOCAL, REL_TEST_RUNNER_JAR_INSTALL_PATH, dex_install_dir);

	char *api_impl_natives_dir = g_strdup_printf("%s/%s", dex_install_dir, REL_API_IMPL_NATIVES_INSTALL_PATH);

	char *app_lib_dir = malloc(strlen(app_data_dir) + strlen("/lib") + 1); // +1 for NULL
	strcpy(app_lib_dir, app_data_dir);
	strcat(app_lib_dir, "/lib");
	// create lib dir
	mkdir(app_lib_dir, DEFFILEMODE | S_IXUSR | S_IXGRP | S_IXOTH);

	// Apps which extract libraries on their own can place them anywhere in app_data_dir. Therefore, we add app_data_dir/** to the
	// BIONIC_LD_LIBRARY_PATH. While app_data_dir/lib is already matched by the wildcard, it needs to be specified again to allow loading
	// libraries by libname from app_data_dir/lib
	char *ld_path = g_strdup_printf("%s:%s**", app_lib_dir, app_data_dir);
	// calling directly into the shim bionic linker to whitelist the app's lib dir as containing bionic-linked libraries
	dl_parse_library_path(ld_path, ":");
	g_free(ld_path);

	JNIEnv *env = create_vm(api_impl_jar, apk_classpath, framework_res_apk, test_runner_jar, api_impl_natives_dir, app_lib_dir, d->sdk_int, d->extra_jvm_options);

	free(app_lib_dir);

	jclass display_class = (*env)->FindClass(env, "android/view/Display");
	_SET_STATIC_INT_FIELD(display_class, "window_width", d->window_width);
	_SET_STATIC_INT_FIELD(display_class, "window_height", d->window_height);

	/* -- register our JNI library under the appropriate classloader -- */

	/* 'android/view/View' is part of the "hax.dex" package, any other function from that package would serve just as well */
	jmethodID getClassLoader = _METHOD((*env)->FindClass(env, "java/lang/Class"), "getClassLoader", "()Ljava/lang/ClassLoader;");
	jobject class_loader = (*env)->CallObjectMethod(env, (*env)->FindClass(env, "android/view/View"), getClassLoader);

	jclass java_runtime_class = (*env)->FindClass(env, "java/lang/Runtime");

	jmethodID getRuntime = _STATIC_METHOD(java_runtime_class, "getRuntime", "()Ljava/lang/Runtime;");
	jobject java_runtime = (*env)->CallStaticObjectMethod(env, java_runtime_class, getRuntime);

	/* this method is private, but it seems we get away with calling it from C */
	jmethodID loadLibrary_with_classloader = _METHOD(java_runtime_class, "loadLibrary", "(Ljava/lang/String;Ljava/lang/ClassLoader;)V");
	(*env)->CallVoidMethod(env, java_runtime, loadLibrary_with_classloader, _JSTRING("translation_layer_main"), class_loader);

	// some apps need the apk path since they directly read their apk
	apk_path = strdup(apk_classpath);

	(*env)->GetJavaVM(env, &jvm);
	set_up_handle_cache(env);

	/* -- misc -- */

	window = gtk_application_window_new(app);

	const char *disable_decoration_env = getenv("ATL_DISABLE_WINDOW_DECORATIONS");
	gboolean decorated;
	if (disable_decoration_env)
		decorated = !strcmp(disable_decoration_env, "0") || !strcmp(disable_decoration_env, "false");
	else { // by default only enable decorations if there are any action buttons to show in the title bar
		char *decoration_layout;
		g_object_get(G_OBJECT(gtk_settings_get_default()), "gtk-decoration-layout", &decoration_layout, NULL);
		GString *gstring = g_string_new_take(decoration_layout);
		g_string_replace(gstring, "menu", "", 0); // ignore menu button
		g_string_replace(gstring, ":", "", 0);    // ignore leading or trailing colon
		decorated = gstring->len > 0;
		g_string_free(gstring, TRUE);
	}
	gtk_window_set_decorated(GTK_WINDOW(window), decorated);

	if (getenv("ATL_FORCE_FULLSCREEN"))
		gtk_window_fullscreen(GTK_WINDOW(window));

	// Load default css stylesheet
	GtkCssProvider *cssProvider = gtk_css_provider_new();
	gtk_css_provider_load_from_resource(cssProvider, "/com/gitlab/android-translation-layer/android-translation-layer/default-stylesheet.css");
	gtk_style_context_add_provider_for_display(gdk_display_get_default(), GTK_STYLE_PROVIDER(cssProvider), GTK_STYLE_PROVIDER_PRIORITY_APPLICATION);

	prepare_main_looper(env);

	/* extract native libraries from apk*/
	if (!getenv("ATL_SKIP_NATIVES_EXTRACTION"))
		extract_from_apk("lib/" NATIVE_ARCH "/", "lib/");

	// construct Application
	application_object = (*env)->CallStaticObjectMethod(env, handle_cache.context.class,
	                                                    _STATIC_METHOD(handle_cache.context.class, "createApplication", "(J)Landroid/app/Application;"), window);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);

	jclass content_provider = (*env)->FindClass(env, "android/content/ContentProvider");
	(*env)->CallStaticVoidMethod(env, content_provider, _STATIC_METHOD(content_provider, "createContentProviders", "()V"));
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);

	jmethodID on_create_method = _METHOD(handle_cache.application.class, "onCreate", "()V");
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
	(*env)->CallVoidMethod(env, application_object, on_create_method);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);

	if (d->apk_instrumentation_class) {
		if (d->apk_main_activity_class) {
			fprintf(stderr, "error: both --instrument and --launch-activity supplied, exiting\n");
			exit(1);
		}

		jobject intent = NULL;

		if (d->extra_string_keys) {
			intent = (*env)->NewObject(env, handle_cache.intent.class, handle_cache.intent.constructor);
			parse_string_extras(env, d->extra_string_keys, intent);
		}

		(*env)->CallStaticObjectMethod(env, handle_cache.instrumentation.class,
		                               _STATIC_METHOD(handle_cache.instrumentation.class, "create", "(Ljava/lang/String;Landroid/content/Intent;)Landroid/app/Instrumentation;"),
		                               _JSTRING(d->apk_instrumentation_class), intent);

		if ((*env)->ExceptionCheck(env))
			(*env)->ExceptionDescribe(env);
	}

	// construct main Activity
	if (!d->apk_instrumentation_class && !d->install_internal) {
		activity_object = (*env)->CallStaticObjectMethod(env, handle_cache.activity.class,
		                                                 _STATIC_METHOD(handle_cache.activity.class, "createMainActivity", "(Ljava/lang/String;JLjava/lang/String;)Landroid/app/Activity;"),
		                                                 _JSTRING(d->apk_main_activity_class), _INTPTR(window), (uri_option && *uri_option) ? _JSTRING(uri_option) : NULL);
		if ((*env)->ExceptionCheck(env))
			(*env)->ExceptionDescribe(env);
		if (uri_option)
			g_free(uri_option);

		if (d->extra_string_keys) {
			jobject intent = _GET_OBJ_FIELD(activity_object, "intent", "Landroid/content/Intent;");
			parse_string_extras(env, d->extra_string_keys, intent);
			g_strfreev(d->extra_string_keys);
		}
	}
	/* -- set the window title and app icon -- */

	if (!d->apk_instrumentation_class) {
		jstring package_name_jstr = (*env)->CallObjectMethod(env, application_object, handle_cache.context.get_package_name);
		package_name = package_name_jstr ? _CSTRING(package_name_jstr) : NULL;
		if ((*env)->ExceptionCheck(env))
			(*env)->ExceptionDescribe(env);
	}

	jstring app_icon_path_jstr = (*env)->CallObjectMethod(env, application_object, handle_cache.application.get_app_icon_path);
	const char *app_icon_path = app_icon_path_jstr ? _CSTRING(app_icon_path_jstr) : NULL;
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);

	if (d->install || d->install_internal) {
		if (d->apk_instrumentation_class) {
			fprintf(stderr, "error: --instrument supplied together with --install, exiting\n");
			exit(1);
		}

		XdpPortal *portal = xdp_portal_new();

		const char *app_label = _CSTRING((*env)->CallObjectMethod(env, application_object, _METHOD(handle_cache.application.class, "get_app_label", "()Ljava/lang/String;")));
		if ((*env)->ExceptionCheck(env))
			(*env)->ExceptionDescribe(env);

		GVariant *icon_serialized = NULL;
		if (!d->install_internal) {
			if (app_icon_path) {
				/* we can import the icon as-is */
				extract_from_apk(app_icon_path, app_icon_path);
				char *app_icon_path_full = g_strdup_printf("%s/%s", app_data_dir, app_icon_path);
				GMappedFile *icon_file = g_mapped_file_new(app_icon_path_full, FALSE, NULL);
				GBytes *icon_bytes = g_mapped_file_get_bytes(icon_file);
				GIcon *icon = g_bytes_icon_new(icon_bytes);
				icon_serialized = g_icon_serialize(icon);
				g_object_unref(icon);
				g_bytes_unref(icon_bytes);
				g_mapped_file_unref(icon_file);
				g_free(app_icon_path_full);
			} else {
				/* the icon is a generalized Drawable, let's render it into an SVG */
				_SET_STATIC_BOOL_FIELD((*env)->FindClass(env, "android/graphics/drawable/VectorDrawable"), "direct_draw_override", true);
				GdkPaintable *icon_paintable = _PTR((*env)->CallLongMethod(env, application_object, handle_cache.application.get_app_icon_paintable));
				GString *svg_string = g_string_new("");
				cairo_surface_t *svg_surface = cairo_svg_surface_create_for_stream(cairo_write_func_gstring, svg_string, 108, 108);
				cairo_t *cr = cairo_create(svg_surface);
				GdkSnapshot *snapshot = gtk_snapshot_new();
				gdk_paintable_snapshot(icon_paintable, snapshot, 108, 108);
				GskRenderNode *node = gtk_snapshot_to_node(snapshot);
				gsk_render_node_draw(node, cr);
				gsk_render_node_unref(node);
				g_object_unref(snapshot);
				cairo_destroy(cr);
				cairo_surface_destroy(svg_surface);
				GBytes *icon_bytes = g_string_free_to_bytes(svg_string);
				GIcon *icon = g_bytes_icon_new(icon_bytes);
				icon_serialized = g_icon_serialize(icon);
				g_object_unref(icon);
				g_bytes_unref(icon_bytes);
			}
		}

		gchar *dest_name = g_strdup_printf("%s.apk", package_name);
		GFile *dest = g_file_new_build_filename(app_data_dir_base, "_installed_apks_", d->install_internal ? dest_name : apk_name, NULL);
		free(dest_name);
		printf("installing %s to %s\n", apk_name, g_file_get_path(dest));
		g_file_make_directory(g_file_get_parent(dest), NULL, NULL);
		GError *err = NULL;
		g_file_copy(files[0], dest, G_FILE_COPY_OVERWRITE, NULL, NULL, NULL, &err);
		if (err)
			fprintf(stderr, "error copying apk: %s\n", err->message);

		if (d->install_internal)
			exit(0);

		jmethodID get_supported_mime_types = _METHOD(handle_cache.application.class, "get_supported_mime_types", "()Ljava/lang/String;");
		jstring supported_mime_types_jstr = (*env)->CallObjectMethod(env, application_object, get_supported_mime_types);
		const char *supported_mime_types = supported_mime_types_jstr ? _CSTRING(supported_mime_types_jstr) : NULL;
		if ((*env)->ExceptionCheck(env))
			(*env)->ExceptionDescribe(env);

		GString *desktop_entry = g_string_new("[Desktop Entry]\n"
		                                      "Type=Application\n"
		                                      "DBusActivatable=true\n"
		                                      "Exec=env ");
		if (getenv("RUN_FROM_BUILDDIR")) {
			printf("WARNING: RUN_FROM_BUILDDIR set and --install given: using current directory in desktop entry\n");
			g_string_append_printf(desktop_entry, "-C %s ", g_get_current_dir());
		}
		char *envs[] = {"RUN_FROM_BUILDDIR", "LD_LIBRARY_PATH", "ANDROID_APP_DATA_DIR", "ATL_UGLY_ENABLE_LOCATION", "ATL_UGLY_ENABLE_MICROPHONE", "ATL_UGLY_ENABLE_WEBVIEW", "ATL_DISABLE_WINDOW_DECORATIONS", "ATL_FORCE_FULLSCREEN", "ATL_IS_AUTOMOTIVE", "ATL_IS_TELEVISION", "ATL_IS_WATCH"};
		for (int i = 0; i < ARRAY_SIZE(envs); i++) {
			if (getenv(envs[i])) {
				g_string_append_printf(desktop_entry, "%s=%s ", envs[i], getenv(envs[i]));
			}
		}
		g_string_append_printf(desktop_entry, "%s ", d->prgname);
		g_string_append_printf(desktop_entry, "--gapplication-app-id %s ", package_name);
		if (d->apk_main_activity_class)
			g_string_append_printf(desktop_entry, "-l %s ", d->apk_main_activity_class);
		if (d->window_width)
			g_string_append_printf(desktop_entry, "-w %d ", d->window_width);
		if (d->window_height)
			g_string_append_printf(desktop_entry, "-h %d ", d->window_height);
		g_string_append_printf(desktop_entry, "%s --uri %%u\n", g_file_get_path(dest));
		if (supported_mime_types)
			g_string_append_printf(desktop_entry, "MimeType=%s\n", supported_mime_types);
		struct dynamic_launcher_callback_data *cb_data = g_new(struct dynamic_launcher_callback_data, 1);
		cb_data->desktop_file_id = g_strdup_printf("%s.desktop", package_name);
		cb_data->desktop_entry = g_string_free(desktop_entry, FALSE);
		printf("installing %s\n\n%s\n", cb_data->desktop_file_id, cb_data->desktop_entry);
		xdp_portal_dynamic_launcher_prepare_install(portal, NULL, app_label, icon_serialized, XDP_LAUNCHER_APPLICATION, NULL, TRUE, TRUE, NULL, dynamic_launcher_ready_callback, cb_data);
		return;
	}

	if (!d->apk_instrumentation_class)
		gtk_window_set_title(GTK_WINDOW(window), package_name);
	gtk_window_set_default_size(GTK_WINDOW(window), d->window_width, d->window_height);
	g_signal_connect(window, "close-request", G_CALLBACK(app_exit), env);

	GtkWidget *header_bar = gtk_header_bar_new();
	GtkWidget *back_button = back_button_new();

	gtk_header_bar_pack_start(GTK_HEADER_BAR(header_bar), back_button);
	gtk_window_set_titlebar(GTK_WINDOW(window), header_bar);
	GtkDropTarget *drop_target = gtk_drop_target_new(G_TYPE_STRING, GDK_ACTION_COPY);
	g_signal_connect(drop_target, "drop", G_CALLBACK(on_drop), NULL);
	gtk_widget_add_controller(window, GTK_EVENT_CONTROLLER(drop_target));
	gtk_window_present(GTK_WINDOW(window));

	// set package name as application id for window icon on Wayland. Needs a {package_name}.desktop file defining the icon
	GdkToplevel *toplevel = GDK_TOPLEVEL(gtk_native_get_surface(GTK_NATIVE(window)));
	if (GDK_IS_WAYLAND_TOPLEVEL(toplevel) && !d->apk_instrumentation_class) {
		gdk_wayland_toplevel_set_application_id(GDK_WAYLAND_TOPLEVEL(toplevel), package_name);
	}
	GdkMonitor *monitor = gdk_display_get_monitor_at_surface(gdk_display_get_default(), GDK_SURFACE(toplevel));
	GdkRectangle monitor_geometry;
	gdk_monitor_get_geometry(monitor, &monitor_geometry);
	jobject resources = _GET_STATIC_OBJ_FIELD(handle_cache.context.class, "r", "Landroid/content/res/Resources;");
	jobject configuration = _GET_OBJ_FIELD(resources, "mConfiguration", "Landroid/content/res/Configuration;");
	if (monitor_geometry.width >= 800 && monitor_geometry.height >= 800)
		_SET_INT_FIELD(configuration, "screenLayout", /*SCREENLAYOUT_SIZE_LARGE*/ 0x03);
	else
		_SET_INT_FIELD(configuration, "screenLayout", /*SCREENLAYOUT_SIZE_NORMAL*/ 0x02);

	if (!d->apk_instrumentation_class && app_icon_path) {
		char *app_icon_path_full = malloc(strlen(app_data_dir) + 1 + strlen(app_icon_path) + 1); // +1 for /, +1 for NULL
		sprintf(app_icon_path_full, "%s/%s", app_data_dir, app_icon_path);

		extract_from_apk(app_icon_path, app_icon_path);

		GError *error = NULL;
		GList *icon_list = g_list_append(NULL, gdk_texture_new_from_filename(app_icon_path_full, &error));
		if (error) {
			fprintf(stderr, "gdk_texture_new_from_filename: %s\n", error->message);
			g_clear_error(&error);
		}
		icon_override(window, icon_list);
		/* if Gtk sets the icon list to NULL, override it again */
		g_signal_connect_after(window, "realize", G_CALLBACK(icon_override), icon_list);
	}

	if (!d->apk_instrumentation_class) {
		activity_start(env, activity_object);

		g_timeout_add(10, G_SOURCE_FUNC(hacky_on_window_focus_changed_callback), env);
	}

	jobject input_queue_callback = g_object_get_data(G_OBJECT(window), "input_queue_callback");
	if (input_queue_callback) {
		jobject input_queue = g_object_get_data(G_OBJECT(window), "input_queue");

		(*env)->CallVoidMethod(env, input_queue_callback, handle_cache.input_queue_callback.onInputQueueCreated, input_queue);
		if ((*env)->ExceptionCheck(env))
			(*env)->ExceptionDescribe(env);
	}

	const char *app_id = g_application_get_application_id(G_APPLICATION(app));
	if (strcmp(app_id, "com.example.demo_application")) {
		// This would normally happen automatically, if the GApplication is not contructed with G_APPLICATION_NON_UNIQUE
		g_dbus_connection_call(g_bus_get_sync(G_BUS_TYPE_SESSION, NULL, NULL),
		                       "org.freedesktop.DBus",
		                       "/org/freedesktop/DBus",
		                       "org.freedesktop.DBus",
		                       "RequestName",
		                       g_variant_new("(su)", app_id, G_BUS_NAME_OWNER_FLAGS_NONE),
		                       G_VARIANT_TYPE("(u)"),
		                       0, -1, NULL, NULL, NULL);
	}
}

static void activate(GtkApplication *app, struct jni_callback_data *d)
{
	if (window) { // this is not the first launch, but a DBus activate request to the running app
		gtk_window_present(GTK_WINDOW(window));
		return;
	}
	fprintf(stderr, "error: usage: ./android-translation-layer [app.apk] [-l path/to/activity]\n"
	                "you can specify --help to see the list of options\n");
	exit(1);
}

static gboolean option_uri_cb(const gchar *option_name, const gchar *value, gpointer data, GError **error)
{
	fprintf(stderr, "option_uri_cb: %s %s, %p, %p\n", option_name, value, data, error);
	uri_option = g_strdup(value);
	return TRUE;
}

void init_cmd_parameters(GApplication *app, struct jni_callback_data *d)
{
	const GOptionEntry cmd_params[] = {
		/* clang-format off */
		/* long_name | short_name | flags | arg                 | arg_data                     | description                                                                                   | arg_desc */
		{ "launch-activity",  'l', 0, G_OPTION_ARG_STRING,       &d->apk_main_activity_class,   "the fully qualifed name of the activity you wish to launch (usually the apk's main activity)", "ACTIVITY_NAME" },
		{ "instrument",        0,  0, G_OPTION_ARG_STRING,       &d->apk_instrumentation_class, "the fully qualifed name of the instrumentation you wish to launch",                            "CLASS_NAME"    },
		{ "window-width",     'w', 0, G_OPTION_ARG_INT,          &d->window_width,              "window width to launch with (some apps react poorly to runtime window size adjustments)",      "WIDTH"         },
		{ "window-height",    'h', 0, G_OPTION_ARG_INT,          &d->window_height,             "window height to launch with (some apps react poorly to runtime window size adjustments)",     "HEIGHT"        },
		{ "install",          'i', 0, G_OPTION_ARG_NONE,         &d->install,                   "install .desktop file for the given apk",                                                      NULL            },
		{ "install-internal",  0 , 0, G_OPTION_ARG_NONE,         &d->install_internal,          "copy an apk to _installed_apks_ but don't create a desktop entry",                             NULL            },
		{ "extra-jvm-option", 'X', 0, G_OPTION_ARG_STRING_ARRAY, &d->extra_jvm_options,         "pass an additional option directly to art (e.g -X \"-verbose:jni\")",                          "\"OPTION\""    },
		{ "extra-string-key", 'e', 0, G_OPTION_ARG_STRING_ARRAY, &d->extra_string_keys,         "pass a string extra (-e key=value)",                                                           "\"KEY=VALUE\"" },
		{ "sdk-int",           0 , 0, G_OPTION_ARG_STRING,       &d->sdk_int,                   "shorthand for -X \"-DBuild.VERSION.SDK_INT=<version>\"",                                                           "SDK_INT" },
		/* long_name | short_name | flags                     | arg                  | arg_data     | description                                                                              | arg_desc */
		{ "uri",              'u', G_OPTION_FLAG_OPTIONAL_ARG, G_OPTION_ARG_CALLBACK, option_uri_cb, "open the given URI inside the application",                                               "URI"           },
		{NULL}
		/* clang-format on */
	};

	g_application_add_main_option_entries(G_APPLICATION(app), cmd_params);
}

void init__r_debug();
void remove_ongoing_notifications();

typedef bool(apply_path_overrides_func_type)(char **);
void libc_bio_set_apply_path_overrides_func(apply_path_overrides_func_type *func);

/*
 * The main thread's stack is initialized to 128KiB by Linux.
 * When the stack grows below the allocated mapping, the kernel
 * automagically maps in more memory (as long as there is no existing
 * mapping in the way).
 * However, ART puts in a guard page of it's own in order to catch stack
 * overflows (and if they happen in Java code, it handles them gracefully).
 * When ART uses pthread_getattr_np to get the main thread's stack size,
 * glibc reports RLIMIT_STACK (minus TLS and stuff), while musl reports
 * the current stack size. Therefore on musl we have to pre-grow the stack
 * to make the kernel actually grow the mapping before ART puts in it's guard
 * page and makes further growth impossible.
 */
static void pregrow_stack()
{
	/* set RLIMIT_STACK to 8 MiB, which should fit both our 6 MiB stack and whatever
	 * the libc stores above the top of stack */
	setrlimit(RLIMIT_STACK, &(struct rlimit){8 * MiB, 8 * MiB});
	/* accessing the first element of this array will grow the stack down 6MiB */
	volatile uint8_t dummy[6 * MiB];
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wuninitialized"
	/* noop, but should generate an access because volatile */
	dummy[0] = dummy[0];
#pragma GCC diagnostic pop
}

int main(int argc, char **argv)
{
	GtkApplication *app;
	int status;

	pregrow_stack();

	/* this has to be done in the main executable, so might as well do it here */
	init__r_debug();

	// locale on android is always either C or C.UTF-8, and some apps might unbeknownst to them depend on that
	// for correct functionality
	setenv("LC_ALL", "C.UTF-8", 1);

	libc_bio_set_apply_path_overrides_func(apply_path_overrides);

	struct jni_callback_data *callback_data = malloc(sizeof(struct jni_callback_data));
	callback_data->apk_main_activity_class = NULL;
	callback_data->apk_instrumentation_class = NULL;
	callback_data->window_width = 960;
	callback_data->window_height = 540;
	callback_data->install = FALSE;
	callback_data->install_internal = FALSE;
	callback_data->prgname = argv[0];
	callback_data->extra_jvm_options = NULL;
	callback_data->extra_string_keys = NULL;
	callback_data->sdk_int = NULL;

	app = gtk_application_new("com.example.demo_application", G_APPLICATION_NON_UNIQUE | G_APPLICATION_HANDLES_OPEN | G_APPLICATION_CAN_OVERRIDE_APP_ID);

	// cmdline related setup
	init_cmd_parameters(G_APPLICATION(app), callback_data);
	g_application_set_option_context_summary(G_APPLICATION(app), "a translation layer for running android applications natively on Linux");

	g_signal_connect(app, "activate", G_CALLBACK(activate), callback_data);
	g_signal_connect(app, "open", G_CALLBACK(open), callback_data);
	g_action_map_add_action_entries(G_ACTION_MAP(app), action_entries, action_entries_count, NULL);
	status = g_application_run(G_APPLICATION(app), argc, argv);
	remove_ongoing_notifications();
	g_object_unref(app);

	if (jvm) {
		JNIEnv *env = get_jni_env();
		jobject system = (*env)->FindClass(env, "java/lang/System");
		jmethodID exit = (*env)->GetStaticMethodID(env, system, "exit", "(I)V");
		(*env)->CallStaticVoidMethod(env, system, exit, status);
	}

	return status;
}

/* TODO: recall what this is doing here */
const char dl_loader[] __attribute__((section(".interp"))) = "/lib/ld-linux.so.2";

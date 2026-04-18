#include <gtk/gtk.h>

#include "../defines.h"
#include "../util.h"

#include "../generated_headers/android_view_View.h"

#include "WrapperWidget.h"
#include "src/api-impl-jni/views/AndroidLayout.h"

G_DEFINE_TYPE(WrapperWidget, wrapper_widget, GTK_TYPE_WIDGET)

typedef enum { ATL_ID = 1,
	       ATL_ID_NAME,
	       ATL_CLASS_NAME,
	       ATL_SUPER_CLASS_NAMES,
	       N_PROPERTIES } WrapperWidgetProperty;
static GParamSpec *wrapper_widget_properties[N_PROPERTIES] = {
	NULL,
};

static void wrapper_widget_set_property(GObject *object, guint property_id, const GValue *value, GParamSpec *pspec)
{
	switch ((WrapperWidgetProperty)property_id) {
		default:
			G_OBJECT_WARN_INVALID_PROPERTY_ID(object, property_id, pspec);
			break;
	}
}

static void wrapper_widget_get_property(GObject *object, guint property_id, GValue *value, GParamSpec *pspec)
{
	WrapperWidget *self = WRAPPER_WIDGET(object);

	JNIEnv *env = get_jni_env();

	jobject jobj = self->jobj;
	jclass class = _CLASS(jobj);

	switch ((WrapperWidgetProperty)property_id) {
		case ATL_ID: {
			jint id_jint = (*env)->CallIntMethod(env, jobj, handle_cache.view.getId);
			if ((*env)->ExceptionCheck(env))
				(*env)->ExceptionDescribe(env);

			const char *id = g_markup_printf_escaped("0x%08x", id_jint);
			g_value_set_string(value, id);
			break;
		}

		case ATL_ID_NAME: {
			jstring id_name_jstring = (*env)->CallObjectMethod(env, jobj, handle_cache.view.getIdName);
			if ((*env)->ExceptionCheck(env))
				(*env)->ExceptionDescribe(env);

			const char *id_name = (*env)->GetStringUTFChars(env, id_name_jstring, NULL);
			g_value_set_string(value, id_name);

			(*env)->ReleaseStringUTFChars(env, id_name_jstring, id_name);
			break;
		}

		case ATL_CLASS_NAME: {
			jstring class_name_jstring = (*env)->CallObjectMethod(env, class, _METHOD(_CLASS(class), "getName", "()Ljava/lang/String;"));
			if ((*env)->ExceptionCheck(env))
				(*env)->ExceptionDescribe(env);

			const char *class_name = (*env)->GetStringUTFChars(env, class_name_jstring, NULL);
			g_value_set_string(value, class_name);

			(*env)->ReleaseStringUTFChars(env, class_name_jstring, class_name);
			break;
		}

		case ATL_SUPER_CLASS_NAMES: {
			jstring super_classes_names_obj = (*env)->CallObjectMethod(env, jobj, handle_cache.view.getAllSuperClasses);
			if ((*env)->ExceptionCheck(env))
				(*env)->ExceptionDescribe(env);

			const char *super_classes_names = (*env)->GetStringUTFChars(env, super_classes_names_obj, NULL);
			g_value_set_string(value, super_classes_names);

			(*env)->ReleaseStringUTFChars(env, super_classes_names_obj, super_classes_names);
			break;
		}

		default:
			G_OBJECT_WARN_INVALID_PROPERTY_ID(object, property_id, pspec);
			break;
	}
}

static void wrapper_widget_init(WrapperWidget *wrapper_widget)
{
}

static void wrapper_widget_dispose(GObject *wrapper_widget)
{
	GtkWidget *widget = GTK_WIDGET(wrapper_widget);
	GtkWidget *child = gtk_widget_get_first_child(widget);
	while (child) {
		GtkWidget *_child = gtk_widget_get_next_sibling(child);
		gtk_widget_unparent(child);
		child = _child;
	}
	WrapperWidget *wrapper = WRAPPER_WIDGET(wrapper_widget);
	if (wrapper->jvm) {
		JNIEnv *env;
		(*wrapper->jvm)->GetEnv(wrapper->jvm, (void **)&env, JNI_VERSION_1_6);
		if (wrapper->jobj)
			_WEAK_UNREF(wrapper->jobj);
		if (wrapper->canvas)
			_UNREF(wrapper->canvas);
	}
	G_OBJECT_CLASS(wrapper_widget_parent_class)->dispose(wrapper_widget);
}

GtkSizeRequestMode wrapper_widget_get_request_mode(GtkWidget *widget)
{
	WrapperWidget *wrapper = WRAPPER_WIDGET(widget);
	return gtk_widget_get_request_mode(wrapper->child);
}

void wrapper_widget_measure(GtkWidget *widget, GtkOrientation orientation, int for_size, int *minimum, int *natural, int *minimum_baseline, int *natural_baseline)
{
	WrapperWidget *wrapper = WRAPPER_WIDGET(widget);
	gtk_widget_measure(wrapper->child, orientation, for_size, minimum, natural, minimum_baseline, natural_baseline);
	if (orientation == GTK_ORIENTATION_HORIZONTAL && (wrapper->layout_width > 0)) {
		*minimum = *natural = wrapper->layout_width;
	} else if (orientation == GTK_ORIENTATION_VERTICAL && (wrapper->layout_height > 0)) {
		*minimum = *natural = wrapper->layout_height;
	}
}

void wrapper_widget_allocate(GtkWidget *widget, int width, int height, int baseline)
{
	WrapperWidget *wrapper = WRAPPER_WIDGET(widget);
	if (!width && !height) {
		width = wrapper->real_width;
		height = wrapper->real_height;
	}
	GtkAllocation allocation = {
		.x = 0,
		.y = 0,
		.width = width,
		.height = height,
	};

	if (wrapper->computeScroll_method) {
		// The child needs to know its size before calling computeScroll, so we allocate it twice.
		// second allocate will not trigger onLayout, because of unchanged size
		gtk_widget_size_allocate(wrapper->child, &allocation, baseline);

		JNIEnv *env;
		(*wrapper->jvm)->GetEnv(wrapper->jvm, (void **)&env, JNI_VERSION_1_6);
		(*env)->CallVoidMethod(env, wrapper->jobj, wrapper->computeScroll_method);
		if ((*env)->ExceptionCheck(env))
			(*env)->ExceptionDescribe(env);
		allocation.x = -(*env)->CallIntMethod(env, wrapper->jobj, handle_cache.view.getScrollX);
		allocation.y = -(*env)->CallIntMethod(env, wrapper->jobj, handle_cache.view.getScrollY);
	}

	if (ATL_IS_ANDROID_LAYOUT(gtk_widget_get_layout_manager(wrapper->child))) {
		AndroidLayout *layout = ATL_ANDROID_LAYOUT(gtk_widget_get_layout_manager(wrapper->child));
		if (layout->real_width != width || layout->real_height != height) {
			layout->real_width = width;
			layout->real_height = height;
			if (!layout->needs_allocation)
				atl_safe_gtk_widget_queue_allocate(wrapper->child);
		}
		if (layout->needs_allocation)
			gtk_widget_size_allocate(wrapper->child, &allocation, baseline);
		else
			gtk_widget_size_allocate(wrapper->child, &(GtkAllocation){.x = allocation.x, .y = allocation.y}, baseline);
	} else {
		gtk_widget_size_allocate(wrapper->child, &allocation, baseline);
	}
	if (wrapper->background)
		gtk_widget_size_allocate(wrapper->background, &allocation, baseline);
}

/* this is used to avoid queing layout changes in the middle of snapshotting */
int snapshot_in_progress = 0;
static void wrapper_widget_snapshot(GtkWidget *widget, GdkSnapshot *snapshot)
{
	snapshot_in_progress++;

	WrapperWidget *wrapper = WRAPPER_WIDGET(widget);
	if (wrapper->real_height > 0 && wrapper->real_width > 0) {
		gtk_snapshot_push_clip(snapshot, &GRAPHENE_RECT_INIT(0, 0, wrapper->real_width, wrapper->real_height));
	}
	if (wrapper->draw_method) {
		JNIEnv *env = get_jni_env();
		_SET_LONG_FIELD(wrapper->canvas, "snapshot", _INTPTR(snapshot));
		(*env)->CallVoidMethod(env, wrapper->jobj, wrapper->draw_method, wrapper->canvas);
		if ((*env)->ExceptionCheck(env))
			(*env)->ExceptionDescribe(env);
	} else {
		GtkWidget *widget = &wrapper->parent_instance;
		GtkWidget *child = gtk_widget_get_first_child(widget);
		while (child) {
			gtk_widget_snapshot_child(widget, child, snapshot);
			child = gtk_widget_get_next_sibling(child);
		}
	}
	if (wrapper->real_height > 0 && wrapper->real_width > 0) {
		gtk_snapshot_pop(snapshot);
	}

	snapshot_in_progress--;
}

static void wrapper_widget_class_init(WrapperWidgetClass *class)
{
	GObjectClass *object_class = G_OBJECT_CLASS(class);
	GtkWidgetClass *widget_class = GTK_WIDGET_CLASS(class);

	object_class->dispose = wrapper_widget_dispose;

	widget_class->get_request_mode = wrapper_widget_get_request_mode;
	widget_class->measure = wrapper_widget_measure;
	widget_class->size_allocate = wrapper_widget_allocate;
	widget_class->snapshot = wrapper_widget_snapshot;

	object_class->set_property = wrapper_widget_set_property;
	object_class->get_property = wrapper_widget_get_property;

	// According to testing, these properties are not evaluated till we open the GtkInspector
	wrapper_widget_properties[ATL_ID] = g_param_spec_string("ATL-id", "ATL: ID", "ID of the component", "", G_PARAM_READABLE);
	wrapper_widget_properties[ATL_ID_NAME] = g_param_spec_string("ATL-id-name", "ATL: ID name", "Name of the ID of the component", "", G_PARAM_READABLE);
	wrapper_widget_properties[ATL_CLASS_NAME] = g_param_spec_string("ATL-class-name", "ATL: Class name", "Name of the class of the component", "", G_PARAM_READABLE);
	wrapper_widget_properties[ATL_SUPER_CLASS_NAMES] = g_param_spec_string("ATL-superclasses-names", "ATL: Super classes names", "Names of all the superclasses of the component class", "", G_PARAM_READABLE);

	g_object_class_install_properties(object_class, N_PROPERTIES, wrapper_widget_properties);
}

GtkWidget *wrapper_widget_new(void)
{
	return g_object_new(wrapper_widget_get_type(), NULL);
}

void wrapper_widget_set_child(WrapperWidget *parent, GtkWidget *child) // TODO: make sure there can only be one child
{
	gtk_widget_insert_before(child, GTK_WIDGET(parent), NULL);
	parent->child = child;
}

static guint queue_queue_redraw(GtkWidget *widget)
{
	gtk_widget_queue_draw(widget);
	g_object_unref(widget);
	return G_SOURCE_REMOVE;
}

void wrapper_widget_queue_draw(WrapperWidget *wrapper)
{
	if (wrapper->draw_method) {
		/* schedule the call to gtk_widget_queue_draw for a future event loop pass in case we're currently inside the snapshot */
		/* GTK+ uses G_PRIORITY_HIGH_IDLE + 10 for resizing operations, and G_PRIORITY_HIGH_IDLE + 20 for redrawing operations. */
		g_idle_add_full(G_PRIORITY_HIGH_IDLE + 20, G_SOURCE_FUNC(queue_queue_redraw), g_object_ref(wrapper), NULL);
	}

	if (wrapper->child)
		gtk_widget_queue_draw(wrapper->child);
	if (wrapper->computeScroll_method) {
		atl_safe_gtk_widget_queue_allocate(GTK_WIDGET(wrapper));
	}
}

static bool on_click(GtkGestureClick *gesture, int n_press, double x, double y, jobject this)
{
	JNIEnv *env = get_jni_env();

	bool ret = (*env)->CallBooleanMethod(env, this, handle_cache.view.performClick);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);

	return ret;
}

#define KEYCODE_0             7 /* KEYCODE_[1-9] = [8-16] */
#define KEYCODE_DPAD_UP       19
#define KEYCODE_DPAD_DOWN     20
#define KEYCODE_DPAD_LEFT     21
#define KEYCODE_DPAD_RIGHT    22
#define KEYCODE_A             29 /* KEYCODE_[B-Z] = [30-54] */
#define KEYCODE_COMMA         55
#define KEYCODE_PERIOD        56
#define KEYCODE_TAB           61
#define KEYCODE_SPACE         62
#define KEYCODE_ENTER         66
#define KEYCODE_DEL           67
#define KEYCODE_GRAVE         68
#define KEYCODE_MINUS         69
#define KEYCODE_EQUALS        70
#define KEYCODE_LEFT_BRACKET  71
#define KEYCODE_RIGHT_BRACKET 72
#define KEYCODE_BACKSLASH     73
#define KEYCODE_SEMICOLON     74
#define KEYCODE_APOSTROPHE    75
#define KEYCODE_SLASH         76
#define KEYCODE_AT            77
#define KEYCODE_PLUS          81
#define KEYCODE_PAGE_UP       92
#define KEYCODE_PAGE_DOWN     93
#define KEYCODE_ESCAPE        111
#define KEYCODE_FORWARD_DEL   112
#define KEYCODE_MOVE_HOME     122
#define KEYCODE_MOVE_END      123
#define KEYCODE_INSERT        124
#define KEYCODE_F1            131 /* KEYCODE_F[2-12] = [132-142] */
#define KEYCODE_NUMPAD_0      144 /* KEYCODE_NUMPAD_[1-9] = [145-153] */

static int map_key_code(int key_code)
{
	if (key_code >= GDK_KEY_0 && key_code <= GDK_KEY_9)
		return key_code - GDK_KEY_0 + KEYCODE_0;
	if (key_code >= GDK_KEY_a && key_code <= GDK_KEY_z)
		return key_code - GDK_KEY_a + KEYCODE_A;
	else if (key_code >= GDK_KEY_A && key_code <= GDK_KEY_Z)
		return key_code - GDK_KEY_A + KEYCODE_A;
	else if (key_code >= GDK_KEY_F1 && key_code <= GDK_KEY_F12)
		return key_code - GDK_KEY_F1 + KEYCODE_F1;
	else if (key_code >= GDK_KEY_KP_0 && key_code <= GDK_KEY_KP_9)
		return key_code - GDK_KEY_KP_0 + KEYCODE_NUMPAD_0;
	switch (key_code) {
		case GDK_KEY_Up:
			return KEYCODE_DPAD_UP;
		case GDK_KEY_Down:
			return KEYCODE_DPAD_DOWN;
		case GDK_KEY_Left:
			return KEYCODE_DPAD_LEFT;
		case GDK_KEY_Right:
			return KEYCODE_DPAD_RIGHT;
		case GDK_KEY_comma:
			return KEYCODE_COMMA;
		case GDK_KEY_period:
			return KEYCODE_PERIOD;
		case GDK_KEY_Tab:
			return KEYCODE_TAB;
		case GDK_KEY_space:
			return KEYCODE_SPACE;
		case GDK_KEY_Return:
			return KEYCODE_ENTER;
		case GDK_KEY_BackSpace:
			return KEYCODE_DEL;
		case GDK_KEY_grave:
			return KEYCODE_GRAVE;
		case GDK_KEY_minus:
			return KEYCODE_MINUS;
		case GDK_KEY_equal:
			return KEYCODE_EQUALS;
		case GDK_KEY_bracketleft:
			return KEYCODE_LEFT_BRACKET;
		case GDK_KEY_bracketright:
			return KEYCODE_RIGHT_BRACKET;
		case GDK_KEY_backslash:
			return KEYCODE_BACKSLASH;
		case GDK_KEY_semicolon:
			return KEYCODE_SEMICOLON;
		case GDK_KEY_apostrophe:
			return KEYCODE_APOSTROPHE;
		case GDK_KEY_slash:
			return KEYCODE_SLASH;
		case GDK_KEY_at:
			return KEYCODE_AT;
		case GDK_KEY_plus:
			return KEYCODE_PLUS;
		case GDK_KEY_Page_Up:
			return KEYCODE_PAGE_UP;
		case GDK_KEY_Page_Down:
			return KEYCODE_PAGE_DOWN;
		case GDK_KEY_Escape:
			return KEYCODE_ESCAPE;
		case GDK_KEY_Delete:
			return KEYCODE_FORWARD_DEL;
		case GDK_KEY_Home:
			return KEYCODE_MOVE_HOME;
		case GDK_KEY_End:
			return KEYCODE_MOVE_END;
		case GDK_KEY_Insert:
			return KEYCODE_INSERT;
		default:
			return key_code;
	}
}

#define META_SHIFT_ON     (1 << 0)
#define META_ALT_ON       (1 << 1)
#define META_CTRL_ON      (1 << 12)
#define META_META_ON      (1 << 16)
#define META_CAPS_LOCK_ON (1 << 20)

static int map_meta_state(GdkModifierType state)
{
	int meta_state = 0;
	if (state & GDK_SHIFT_MASK)
		meta_state |= META_SHIFT_ON;
	if (state & GDK_CONTROL_MASK)
		meta_state |= META_CTRL_ON;
	if (state & GDK_META_MASK)
		meta_state |= META_META_ON;
	if (state & GDK_ALT_MASK)
		meta_state |= META_ALT_ON;
	if (state & GDK_LOCK_MASK)
		meta_state |= META_CAPS_LOCK_ON;
	return meta_state;
}

#define ACTION_DOWN 0
#define ACTION_UP   1

static gboolean on_key_pressed(GtkEventControllerKey *controller, guint keyval, guint keycode, GdkModifierType state, WrapperWidget *wrapper)
{
	JNIEnv *env = get_jni_env();

	jobject key_event = (*env)->NewObject(env, handle_cache.key_event.class, handle_cache.key_event.constructor, (jlong)0, (jlong)0, ACTION_DOWN, map_key_code(keyval), 0, map_meta_state(state));
	_SET_INT_FIELD(key_event, "unicodeValue", gdk_keyval_to_unicode(keyval));
	gboolean ret = (*env)->CallBooleanMethod(env, wrapper->jobj, handle_cache.view.dispatchKeyEvent, key_event);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
	return ret;
}

static gboolean on_key_released(GtkEventControllerKey *controller, guint keyval, guint keycode, GdkModifierType state, WrapperWidget *wrapper)
{
	JNIEnv *env = get_jni_env();

	jobject key_event = (*env)->NewObject(env, handle_cache.key_event.class, handle_cache.key_event.constructor, (jlong)0, (jlong)0, ACTION_UP, map_key_code(keyval), 0, map_meta_state(state));
	_SET_INT_FIELD(key_event, "unicodeValue", gdk_keyval_to_unicode(keyval));
	gboolean ret = (*env)->CallBooleanMethod(env, wrapper->jobj, handle_cache.view.dispatchKeyEvent, key_event);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
	return ret;
}

static void map_cb(WrapperWidget *wrapper, jmethodID method)
{
	JNIEnv *env = get_jni_env();
	(*env)->CallVoidMethod(env, wrapper->jobj, method);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
}

GtkWidget *currently_unmapping = NULL;

static void unmap_cb(WrapperWidget *wrapper, jmethodID method)
{
	JNIEnv *env = get_jni_env();
	currently_unmapping = wrapper->child;
	(*env)->CallVoidMethod(env, wrapper->jobj, method);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
	currently_unmapping = NULL;
}

void wrapper_widget_set_jobject(WrapperWidget *wrapper, JNIEnv *env, jobject jobj)
{
	JavaVM *jvm;
	(*env)->GetJavaVM(env, &jvm);
	wrapper->jvm = jvm;
	wrapper->jobj = _WEAK_REF(jobj);
	jmethodID on_draw_method = _METHOD(_CLASS(jobj), "onDraw", "(Landroid/graphics/Canvas;)V");
	jmethodID dispatch_draw_method = _METHOD(_CLASS(jobj), "dispatchDraw", "(Landroid/graphics/Canvas;)V");
	jmethodID draw_method = _METHOD(_CLASS(jobj), "draw", "(Landroid/graphics/Canvas;)V");
	if (on_draw_method != handle_cache.view.onDraw || draw_method != handle_cache.view.draw || dispatch_draw_method != handle_cache.view.dispatchDraw) {
		wrapper->draw_method = draw_method;
		jclass canvas_class = (*env)->FindClass(env, "android/atl/GskCanvas");
		jmethodID canvas_constructor = _METHOD(canvas_class, "<init>", "(J)V");
		wrapper->canvas = _REF((*env)->NewObject(env, canvas_class, canvas_constructor, 0));
		(*env)->DeleteLocalRef(env, canvas_class);
	}

	jmethodID performClick_method = _METHOD(_CLASS(jobj), "performClick", "()Z");
	if (performClick_method != handle_cache.view.performClick) {
		GtkEventController *controller = GTK_EVENT_CONTROLLER(gtk_gesture_click_new());

		g_signal_connect(controller, "released", G_CALLBACK(on_click), wrapper->jobj);
		gtk_widget_add_controller(GTK_WIDGET(wrapper), controller);
		widget_set_needs_allocation(wrapper->child);
	}

	jmethodID ontouchevent_method = _METHOD(_CLASS(jobj), "onTouchEvent", "(Landroid/view/MotionEvent;)Z");
	jmethodID dispatchtouchevent_method = _METHOD(_CLASS(jobj), "dispatchTouchEvent", "(Landroid/view/MotionEvent;)Z");
	wrapper->custom_dispatch_touch = (dispatchtouchevent_method != handle_cache.view.dispatchTouchEvent && dispatchtouchevent_method != handle_cache.view_group.dispatchTouchEvent);
	if (ontouchevent_method != handle_cache.view.onTouchEvent || wrapper->custom_dispatch_touch) {
		_setOnTouchListener(env, jobj, GTK_WIDGET(wrapper));
	}

	jmethodID computeScroll_method = _METHOD(_CLASS(jobj), "computeScroll", "()V");
	if (computeScroll_method != handle_cache.view.computeScroll) {
		wrapper->computeScroll_method = computeScroll_method;
	}

	jmethodID dispatch_key_event_method = _METHOD(_CLASS(jobj), "dispatchKeyEvent", "(Landroid/view/KeyEvent;)Z");
	jmethodID on_key_down_method = _METHOD(_CLASS(jobj), "onKeyDown", "(ILandroid/view/KeyEvent;)Z");
	if (dispatch_key_event_method != handle_cache.view.dispatchKeyEvent || on_key_down_method != handle_cache.view.onKeyDown) {
		GtkEventController *controller = GTK_EVENT_CONTROLLER(gtk_event_controller_key_new());
		g_signal_connect(controller, "key-pressed", G_CALLBACK(on_key_pressed), wrapper);
		g_signal_connect(controller, "key-released", G_CALLBACK(on_key_released), wrapper);
		gtk_widget_add_controller(GTK_WIDGET(wrapper), controller);
		gtk_widget_set_focusable(GTK_WIDGET(wrapper), TRUE);
	}
	g_signal_connect(wrapper, "map", G_CALLBACK(map_cb), handle_cache.view.onAttachedToWindow);
	g_signal_connect(wrapper, "unmap", G_CALLBACK(unmap_cb), handle_cache.view.onDetachedFromWindow);
}

void wrapper_widget_set_layout_params(WrapperWidget *wrapper, int width, int height)
{
	wrapper->layout_width = width;
	wrapper->layout_height = height;
}

void wrapper_widget_set_background(WrapperWidget *wrapper, GdkPaintable *paintable)
{
	if (!paintable) {
		if (wrapper->background) {
			gtk_widget_unparent(wrapper->background);
			wrapper->background = NULL;
		}
		return;
	}
	if (!wrapper->background) {
		wrapper->background = gtk_picture_new();
		gtk_widget_insert_after(wrapper->background, GTK_WIDGET(wrapper), NULL);
	}
	gtk_picture_set_paintable(GTK_PICTURE(wrapper->background), paintable);
}

static gboolean on_touch_event_consume(GtkEventControllerLegacy *controller, GdkEvent *event)
{
	switch (gdk_event_get_event_type(event)) {
		case GDK_BUTTON_PRESS:
		case GDK_TOUCH_BEGIN:
		case GDK_BUTTON_RELEASE:
		case GDK_TOUCH_END:
		case GDK_MOTION_NOTIFY:
		case GDK_TOUCH_UPDATE:
			return TRUE;
		default:
			return FALSE;
	}
}

// Add default on touch listener, which just consumes all events to prevent bubbling to the parent
void wrapper_widget_consume_touch_events(WrapperWidget *wrapper)
{
	GtkEventController *controller = GTK_EVENT_CONTROLLER(gtk_event_controller_legacy_new());
	g_signal_connect(controller, "event", G_CALLBACK(on_touch_event_consume), NULL);
	gtk_widget_add_controller(GTK_WIDGET(wrapper), controller);
	g_object_set_data(G_OBJECT(wrapper), "on_touch_listener", controller);
}

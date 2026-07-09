#include <assert.h>
#include <stdio.h>

#include <gtk/gtk.h>

#include "../defines.h"
#include "../util.h"

#include "../views/AndroidLayout.h"
#include "../widgets/WrapperWidget.h"

#include "../generated_headers/android_view_MotionEvent.h"
#include "../generated_headers/android_view_View.h"

#define JAVA_ENUM_CLASS android_view_MotionEvent
enum {
	JAVA_ENUM(ACTION_DOWN),
	JAVA_ENUM(ACTION_UP),
	JAVA_ENUM(ACTION_MOVE),
	JAVA_ENUM(ACTION_CANCEL),

	JAVA_ENUM(ACTION_POINTER_DOWN),
	JAVA_ENUM(ACTION_POINTER_UP),

	JAVA_ENUM(ACTION_HOVER_MOVE),
	JAVA_ENUM(ACTION_SCROLL),
	JAVA_ENUM(ACTION_HOVER_ENTER),
	JAVA_ENUM(ACTION_HOVER_EXIT),
};
#undef JAVA_ENUM_CLASS

#define SOURCE_TOUCHSCREEN   0x1002
#define SOURCE_CLASS_POINTER 0x2

#define MAX_POINTERS         50

struct pointer {
	int id;
	int index;
	float coord_x;
	float coord_y;
	float raw_x;
	float raw_y;
};

GPtrArray *pointer_indices = NULL;

gpointer canceled_event = NULL;
WrapperWidget *cancel_triggerer = NULL;
int canceled_action = 0;

static struct pointer pointers[MAX_POINTERS] = {};

bool view_dispatch_motionevent(JNIEnv *env, WrapperWidget *wrapper, GtkPropagationPhase phase, jobject motion_event, gpointer event, int action)
{
	int ret;

	jobject this = wrapper->jobj;
	bool is_synthetic = event == motion_event;

	if (cancel_triggerer == wrapper) { // cancel done
		canceled_event = NULL;
		cancel_triggerer = NULL;
		if (is_synthetic)
			_SET_INT_FIELD(motion_event, "action", canceled_action);
	}

	if (wrapper->custom_dispatch_touch) {
		ret = (*env)->CallBooleanMethod(env, this, handle_cache.view.dispatchTouchEvent, motion_event);
	} else if (phase == GTK_PHASE_CAPTURE && _GET_BOOL_FIELD(this, "disallowIntercept")) {
		if (action == ACTION_UP || action == ACTION_CANCEL)
			_SET_BOOL_FIELD(this, "disallowIntercept", false);
		ret = false;
	} else if (phase == GTK_PHASE_CAPTURE && !wrapper->intercepting_touch) {
		wrapper->intercepting_touch = (*env)->CallBooleanMethod(env, this, handle_cache.view.onInterceptTouchEvent, motion_event);
		if (wrapper->intercepting_touch) {
			// store the event that was canceled and let it propagate to the child widgets
			canceled_event = event;
			cancel_triggerer = wrapper;
			if (is_synthetic) {
				canceled_action = _GET_INT_FIELD(motion_event, "action");
				_SET_INT_FIELD(motion_event, "action", ACTION_CANCEL);
			}
		}
		ret = false;
	} else {
		ret = (*env)->CallBooleanMethod(env, this, handle_cache.view.onTouchEventInternal, motion_event, (jboolean)is_synthetic);
	}

	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);

	if (action == ACTION_UP || action == ACTION_CANCEL)
		wrapper->intercepting_touch = false;

	if (action == ACTION_CANCEL)
		ret = false;

	return ret;
}

static bool call_ontouch_callback(WrapperWidget *wrapper, int action, struct pointer pointers[MAX_POINTERS], GPtrArray *pointer_indices, GtkPropagationPhase phase, guint32 timestamp, GdkEvent *event)
{
	bool ret;
	JNIEnv *env = get_jni_env();

	int num_pointers = pointer_indices->len;
	jintArray ids = (*env)->NewIntArray(env, num_pointers);
	jfloatArray coords = (*env)->NewFloatArray(env, num_pointers * 4);
	for (int i = 0; i < num_pointers; i++) {
		struct pointer *pointer = (struct pointer *)g_ptr_array_index(pointer_indices, i);
		(*env)->SetIntArrayRegion(env, ids, i, 1, &pointer->id);
		/* put in all four float values starting at coord_x */
		(*env)->SetFloatArrayRegion(env, coords, 4 * i, 4, &pointer->coord_x);
	}

	jobject motion_event = (*env)->NewObject(env, handle_cache.motion_event.class, handle_cache.motion_event.constructor, SOURCE_TOUCHSCREEN, action, timestamp, ids, coords);

	ret = view_dispatch_motionevent(env, wrapper, phase, motion_event, event, action);

	(*env)->DeleteLocalRef(env, motion_event);

	return ret;
}

static void call_hover_callback(jobject this, int action, float x, float y, float raw_x, float raw_y)
{
	JNIEnv *env = get_jni_env();
	jobject motion_event = (*env)->NewObject(env, handle_cache.motion_event.class, handle_cache.motion_event.constructor_single, SOURCE_CLASS_POINTER, action, 0, x, y, x, y);
	(*env)->CallBooleanMethod(env, this, handle_cache.view.dispatchHoverEvent, motion_event);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
}

static void transform_coords_to_widget_relative(GtkWidget *widget, double *x, double *y)
{
	int ret;

	graphene_point_t p;
	double off_x;
	double off_y;

	GtkWidget *window = GTK_WIDGET(gtk_widget_get_native(widget));
	gtk_native_get_surface_transform(GTK_NATIVE(window), &off_x, &off_y);
	ret = gtk_widget_compute_point(window, widget, &GRAPHENE_POINT_INIT(*x - off_x, *y - off_y), &p);
	assert(ret);

	*x = p.x;
	*y = p.y;
}

void remove_pointer_fast(GPtrArray *pointer_indices, struct pointer *pointer)
{
	g_ptr_array_remove_index_fast(pointer_indices, pointer->index);
	if (pointer->index != pointer_indices->len) {
		/* update the index field of the pointer that was moved in to fill the empty space */
		struct pointer *replacement_pointer = (struct pointer *)g_ptr_array_index(pointer_indices, pointer->index);
		replacement_pointer->index = pointer->index;
	}
	/* mark as empty, other fields don't matter */
	pointer->id = 0;
}

static bool action_up_already_handled = false;

// TODO: find a way to reconcile this with libandroid/input.c?
static gboolean on_pointer_event(GtkEventControllerLegacy *event_controller, GdkEvent *event, gpointer user_data)
{
	double x;
	double y;

	if ((gdk_event_get_event_type(event) == GDK_TOUCH_END || gdk_event_get_event_type(event) == GDK_BUTTON_RELEASE) && action_up_already_handled) {
		action_up_already_handled = false;
		return true;
	}

	uintptr_t id = (uintptr_t)gdk_event_get_event_sequence(event);
	if (id > MAX_POINTERS - 1) // sequence id is a real pointer for drag and drop events
		return false;

	/* FIXME: this will clash with touchscreen */
	if (id == 0)
		id = 1;

	int pointer_index;

	if (pointers[id].id) {
		pointer_index = pointers[id].index;
	} else {
		/* index of a hypothetical next appended element */
		pointer_index = pointer_indices->len;
	}

	int action;
	GdkEventType event_type = gdk_event_get_event_type(event);
	switch (event_type) {
		case GDK_BUTTON_PRESS:
		case GDK_TOUCH_BEGIN:
			action = pointer_index > 0 ? (ACTION_POINTER_DOWN | (pointer_index << 8)) : ACTION_DOWN;
			break;
		case GDK_BUTTON_RELEASE:
		case GDK_TOUCH_END:
			action = pointer_index > 0 ? (ACTION_POINTER_UP | (pointer_index << 8)) : ACTION_UP;
			break;
		case GDK_MOTION_NOTIFY:
			if (!(gdk_event_get_modifier_state(event) & GDK_BUTTON1_MASK))
				return false;
		case GDK_TOUCH_UPDATE:
			action = ACTION_MOVE;
			break;
		default: // not a touch or mouse event, nothing to do here
			return false;
	}

	GtkWidget *widget = gtk_event_controller_get_widget(GTK_EVENT_CONTROLLER(event_controller));
	WrapperWidget *wrapper = WRAPPER_WIDGET(widget);
	GtkPropagationPhase phase = gtk_event_controller_get_propagation_phase(GTK_EVENT_CONTROLLER(event_controller));
	guint32 timestamp = gdk_event_get_time(event);

	if (id >= MAX_POINTERS) {
		fprintf(stderr, "exiting - event sequence %w64d larger than %d, did Gtk implementation change?", id, MAX_POINTERS);
		exit(1);
	}

	double raw_x;
	double raw_y;
	gdk_event_get_position(event, &raw_x, &raw_y);
	x = raw_x, y = raw_y;
	transform_coords_to_widget_relative(widget, &x, &y);

	if (!pointers[id].id) {
		/* if this is a new sequence, add a slot for it */
		g_ptr_array_add(pointer_indices, &pointers[id]);
		/* we appended a single element, so it must be at pointer_index */
		pointers[id].index = pointer_index;
		pointers[id].id = id;
	}

	pointers[id].coord_x = x;
	pointers[id].coord_y = y;
	pointers[id].raw_x = raw_x;
	pointers[id].raw_y = raw_y;

	if (event == canceled_event && cancel_triggerer != wrapper) {
		action = ACTION_CANCEL;
	}

	gboolean ret = call_ontouch_callback(wrapper, action, pointers, pointer_indices, phase, timestamp, event);
	if (event_type == GDK_BUTTON_RELEASE || event_type == GDK_TOUCH_END) {
		remove_pointer_fast(pointer_indices, &pointers[id]);
	}
	if (pointer_indices->len == 0 && wrapper->hover_exit_pending) {
		wrapper->hover_exit_pending = FALSE;
		call_hover_callback(wrapper->jobj, ACTION_HOVER_EXIT, x, y, raw_x, raw_y);
	}
	return ret;
}

static gboolean on_event(GtkEventControllerLegacy *event_controller, GdkEvent *event, gpointer user_data)
{
	GdkEventType event_type = gdk_event_get_event_type(event);
	switch (event_type) {
		case GDK_BUTTON_PRESS:
		case GDK_TOUCH_BEGIN:
		case GDK_BUTTON_RELEASE:
		case GDK_TOUCH_END:
		case GDK_MOTION_NOTIFY:
		case GDK_TOUCH_UPDATE:
			return on_pointer_event(event_controller, event, user_data);
		default: // not a touch or mouse event, nothing to do here
			return false;
	}
}

static void on_click(GtkGestureClick *gesture, int n_press, double x, double y, gpointer user_data)
{
	JNIEnv *env = get_jni_env();
	GtkWidget *widget = gtk_event_controller_get_widget(GTK_EVENT_CONTROLLER(gesture));
	WrapperWidget *wrapper = WRAPPER_WIDGET(gtk_widget_get_parent(widget));

	jboolean handled = (*env)->CallBooleanMethod(env, wrapper->jobj, handle_cache.view.performClick);

	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);

	if (handled) {
		gtk_gesture_set_state(GTK_GESTURE(gesture), GTK_EVENT_SEQUENCE_CLAIMED);
		// claiming fails if there is no last event, set a flag in that case
		GdkEvent *event = gtk_gesture_get_last_event(GTK_GESTURE(gesture), gtk_gesture_get_last_updated_sequence(GTK_GESTURE(gesture)));
		if (!event) {
			action_up_already_handled = true;
		}
	}
}

#define MAGIC_SCROLL_FACTOR 32

/* Gtk4 for whatever reason doesn't report cursor position for GtkEventControllerScroll, thankfully
 * it's not too annoying to get it ourselves */
gboolean hacky_get_cursor_position(GtkWidget *widget, double *x, double *y)
{
	GdkSeat *seat;
	GdkSurface *surface;
	GdkDevice *device_pointer;

	seat = gdk_display_get_default_seat(gtk_widget_get_display(widget));

	device_pointer = gdk_seat_get_pointer(seat);
	surface = gtk_native_get_surface(gtk_widget_get_native(widget));

	return gdk_surface_get_device_position(surface,
	                                       device_pointer,
	                                       x, y, NULL);
}

static gboolean scroll_cb(GtkEventControllerScroll *controller, gdouble dx, gdouble dy, jobject this)
{
	double x;
	double y;
	double raw_x;
	double raw_y;

	JNIEnv *env = get_jni_env();
	GdkScrollUnit scroll_unit = gtk_event_controller_scroll_get_unit(controller);
	GdkEvent *event = gtk_event_controller_get_current_event(GTK_EVENT_CONTROLLER(controller));
	GtkWidget *widget = gtk_event_controller_get_widget(GTK_EVENT_CONTROLLER(controller));
	WrapperWidget *wrapper = WRAPPER_WIDGET(widget);
	hacky_get_cursor_position(widget, &raw_x, &raw_y);
	x = raw_x, y = raw_y;
	transform_coords_to_widget_relative(widget, &x, &y);

	if (scroll_unit == GDK_SCROLL_UNIT_SURFACE) {
		dx /= MAGIC_SCROLL_FACTOR;
		dy /= MAGIC_SCROLL_FACTOR;
	}
	jobject motion_event = (*env)->NewObject(env, handle_cache.motion_event.class,
	                                         handle_cache.motion_event.constructor_scroll,
	                                         SOURCE_CLASS_POINTER, ACTION_SCROLL,
	                                         gdk_event_get_time(event), x, y, raw_x, raw_y, dx, -dy);

	gboolean ret = (*env)->CallBooleanMethod(env, this, handle_cache.view.dispatchGenericMotionEvent, motion_event);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);

	// HACK: replay last hover event to work around scrolling issues in composeUI apps
	if (wrapper->hover_x && wrapper->hover_y)
		call_hover_callback(this, ACTION_HOVER_MOVE, wrapper->hover_x, wrapper->hover_y, wrapper->hover_x, wrapper->hover_y);

	return ret;
}

static void hover_enter_cb(GtkEventControllerMotion *controller, double x, double y, WrapperWidget *wrapper)
{
	double raw_x;
	double raw_y;
	GdkEvent *event = gtk_event_controller_get_current_event(GTK_EVENT_CONTROLLER(controller));
	gdk_event_get_position(event, &raw_x, &raw_y);
	wrapper->hover_exit_pending = FALSE;
	wrapper->hover_x = x;
	wrapper->hover_y = y;
	call_hover_callback(wrapper->jobj, ACTION_HOVER_ENTER, x, y, raw_x, raw_y);
}

static void hover_leave_cb(GtkEventControllerMotion *controller, WrapperWidget *wrapper)
{
	if (pointer_indices->len) // composeUI apps don't like ACTION_HOVER_EXIT events while a pointer is down
		wrapper->hover_exit_pending = TRUE;
	else
		call_hover_callback(wrapper->jobj, ACTION_HOVER_EXIT, 0, 0, 0, 0);
}

static void hover_motion_cb(GtkEventControllerMotion *controller, double x, double y, WrapperWidget *wrapper)
{
	double raw_x;
	double raw_y;
	GdkEvent *event = gtk_event_controller_get_current_event(GTK_EVENT_CONTROLLER(controller));
	gdk_event_get_position(event, &raw_x, &raw_y);
	wrapper->hover_x = x;
	wrapper->hover_y = y;
	call_hover_callback(wrapper->jobj, ACTION_HOVER_MOVE, x, y, raw_x, raw_y);
}

void _setOnTouchListener(JNIEnv *env, jobject this, GtkWidget *widget)
{
	GtkEventController *old_controller = g_object_get_data(G_OBJECT(widget), "on_touch_listener");
	if (old_controller)
		return;

	if (!pointer_indices)
		pointer_indices = g_ptr_array_new_full(20, NULL);

	GtkEventController *controller = GTK_EVENT_CONTROLLER(gtk_event_controller_legacy_new());
	gtk_event_controller_set_propagation_phase(controller, GTK_PHASE_BUBBLE);

	g_signal_connect(controller, "event", G_CALLBACK(on_event), NULL);
	gtk_widget_add_controller(widget, controller);
	g_object_set_data(G_OBJECT(widget), "on_touch_listener", controller);

	WrapperWidget *wrapper = WRAPPER_WIDGET(widget);
	jmethodID onintercepttouchevent_method = _METHOD(_CLASS(this), "onInterceptTouchEvent", "(Landroid/view/MotionEvent;)Z");
	if (onintercepttouchevent_method != handle_cache.view.onInterceptTouchEvent || wrapper->custom_dispatch_touch) {
		GtkEventController *old_controller = g_object_get_data(G_OBJECT(widget), "on_intercept_touch_listener");
		if (old_controller)
			gtk_widget_remove_controller(widget, old_controller);

		GtkEventController *controller = GTK_EVENT_CONTROLLER(gtk_event_controller_legacy_new());
		gtk_event_controller_set_propagation_phase(controller, GTK_PHASE_CAPTURE);

		g_signal_connect(controller, "event", G_CALLBACK(on_event), NULL);
		gtk_widget_add_controller(widget, controller);
		g_object_set_data(G_OBJECT(widget), "on_intercept_touch_listener", controller);
	}

	if (!wrapper->needs_allocation && (wrapper->real_width || wrapper->real_height))
		gtk_widget_size_allocate(GTK_WIDGET(wrapper), &(GtkAllocation){.x = 0, .y = 0, .width = wrapper->real_width, .height = wrapper->real_height}, 0);

	wrapper->needs_allocation = true;
	gtk_widget_set_overflow(GTK_WIDGET(wrapper), GTK_OVERFLOW_HIDDEN);
}

JNIEXPORT void JNICALL Java_android_view_View_nativeSetOnTouchListener(JNIEnv *env, jobject this, jlong widget_ptr)
{
	GtkWidget *widget = gtk_widget_get_parent(GTK_WIDGET(_PTR(widget_ptr)));

	_setOnTouchListener(env, this, widget);
}

JNIEXPORT void JNICALL Java_android_view_View_nativeSetOnClickListener(JNIEnv *env, jobject this, jlong widget_ptr)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));

	GtkEventController *old_controller = g_object_get_data(G_OBJECT(widget), "on_click_listener");
	if (old_controller)
		return;

	GtkEventController *controller = GTK_EVENT_CONTROLLER(gtk_gesture_click_new());

	g_signal_connect(controller, "released", G_CALLBACK(on_click), NULL); // the release completes the click, I guess?
	gtk_widget_add_controller(widget, controller);
	g_object_set_data(G_OBJECT(widget), "on_click_listener", controller);
	widget_set_needs_allocation(widget);
}

JNIEXPORT jint JNICALL Java_android_view_View_getWidth(JNIEnv *env, jobject this)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(_GET_LONG_FIELD(this, "widget")));

	if (ATL_IS_ANDROID_LAYOUT(gtk_widget_get_layout_manager(widget))) {
		AndroidLayout *layout = ATL_ANDROID_LAYOUT(gtk_widget_get_layout_manager(widget));
		return layout->real_width;
	}
	return gtk_widget_get_width(widget);
}

JNIEXPORT jint JNICALL Java_android_view_View_getHeight(JNIEnv *env, jobject this)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(_GET_LONG_FIELD(this, "widget")));

	if (ATL_IS_ANDROID_LAYOUT(gtk_widget_get_layout_manager(widget))) {
		AndroidLayout *layout = ATL_ANDROID_LAYOUT(gtk_widget_get_layout_manager(widget));
		return layout->real_height;
	}
	return gtk_widget_get_height(widget);
}

#define GRAVITY_TOP               (1 << 5) //0x30
#define GRAVITY_BOTTOM            (1 << 6) //0x50
#define GRAVITY_LEFT              (1 << 1) //0x3
#define GRAVITY_RIGHT             (1 << 2) //0x5

#define GRAVITY_CENTER_VERTICAL   0x10
#define GRAVITY_CENTER_HORIZONTAL 0x01

#define GRAVITY_CENTER            (GRAVITY_CENTER_VERTICAL | GRAVITY_CENTER_HORIZONTAL)

#define MATCH_PARENT              (-1)

JNIEXPORT void JNICALL Java_android_view_View_native_1setLayoutParams(JNIEnv *env, jobject this, jlong widget_ptr, jint width, jint height, jint gravity, jfloat weight,
                                                                      jint leftMargin, jint topMargin, jint rightMargin, jint bottomMargin)
{
	GtkWidget *widget = gtk_widget_get_parent(GTK_WIDGET(_PTR(widget_ptr)));

	GtkAlign halign = GTK_ALIGN_FILL;
	GtkAlign valign = GTK_ALIGN_FILL;
	gboolean hexpand = FALSE;
	gboolean vexpand = FALSE;

	if (gravity != -1) {
		if (gravity & GRAVITY_BOTTOM)
			valign = GTK_ALIGN_END;
		else if (gravity & GRAVITY_TOP)
			valign = GTK_ALIGN_START;
		else
			valign = GTK_ALIGN_FILL;

		if (gravity & GRAVITY_RIGHT)
			halign = GTK_ALIGN_END;
		else if (gravity & GRAVITY_LEFT)
			halign = GTK_ALIGN_START;
		else
			halign = GTK_ALIGN_FILL;

		if (gravity == GRAVITY_CENTER) {
			valign = GTK_ALIGN_CENTER; // GTK_ALIGN_CENTER doesn't seem to be the right one?
			halign = GTK_ALIGN_CENTER; // ditto (GTK_ALIGN_CENTER)
			hexpand = TRUE;            // haxx or not?
			vexpand = TRUE;            // seems to be the deciding factor for whether to expand, guess I should try on android
		}
	}

	if (weight > 0.f) {
		hexpand = TRUE;
		vexpand = TRUE;
	}

	if (width == MATCH_PARENT) {
		hexpand = true;
		halign = GTK_ALIGN_FILL;
	}
	if (height == MATCH_PARENT) {
		vexpand = true;
		valign = GTK_ALIGN_FILL;
	}

	gtk_widget_set_hexpand(widget, hexpand);
	gtk_widget_set_vexpand(widget, vexpand);
	gtk_widget_set_halign(widget, halign);
	gtk_widget_set_valign(widget, valign);

	if (width > 0)
		g_object_set(G_OBJECT(widget), "width-request", width, NULL);
	if (height > 0)
		g_object_set(G_OBJECT(widget), "height-request", height, NULL);

	GtkWidget *parent = gtk_widget_get_parent(widget);
	// if parent is Java widget, it will handle the margins by itself
	if (parent && !ATL_IS_ANDROID_LAYOUT(gtk_widget_get_layout_manager(parent))) {
		gtk_widget_set_margin_start(widget, leftMargin);
		gtk_widget_set_margin_top(widget, topMargin);
		gtk_widget_set_margin_end(widget, rightMargin);
		gtk_widget_set_margin_bottom(widget, bottomMargin);
	}

	GtkLayoutManager *layout_manager = gtk_widget_get_layout_manager(WRAPPER_WIDGET(widget)->child);
	if (ATL_IS_ANDROID_LAYOUT(layout_manager))
		android_layout_set_params(ATL_ANDROID_LAYOUT(layout_manager), width, height);

	wrapper_widget_set_layout_params(WRAPPER_WIDGET(widget), width, height);

	atl_ensure_widget_snapshotability(widget);
}

#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wdeprecated-declarations"
JNIEXPORT void JNICALL Java_android_view_View_native_1setPadding(JNIEnv *env, jobject this, jlong widget_ptr, jint left, jint top, jint right, jint bottom)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));
	if (ATL_IS_ANDROID_LAYOUT(gtk_widget_get_layout_manager(widget))) {
		return;
	}

	GtkStyleContext *style_context = gtk_widget_get_style_context(widget);
	GtkCssProvider *old_provider = g_object_get_data(G_OBJECT(widget), "padding_style_provider");
	if (old_provider)
		gtk_style_context_remove_provider(style_context, GTK_STYLE_PROVIDER(old_provider));
	GtkCssProvider *css_provider = gtk_css_provider_new();

	char *css_string = g_markup_printf_escaped("*{ padding-left: %dpx; padding-top: %dpx; padding-right: %dpx; padding-bottom: %dpx; }", left, top, right, bottom);
	gtk_css_provider_load_from_string(css_provider, css_string);
	g_free(css_string);

	gtk_style_context_add_provider(style_context, GTK_STYLE_PROVIDER(css_provider), GTK_STYLE_PROVIDER_PRIORITY_APPLICATION);
	g_object_set_data(G_OBJECT(widget), "padding_style_provider", css_provider);
}

JNIEXPORT void JNICALL Java_android_view_View_native_1setVisibility(JNIEnv *env, jobject this, jlong widget_ptr, jint visibility, jfloat alpha)
{
	GtkWidget *widget = gtk_widget_get_parent(GTK_WIDGET(_PTR(widget_ptr)));

	atl_safe_gtk_widget_set_visible(widget, visibility != android_view_View_GONE);
	gtk_widget_set_opacity(widget, (visibility != android_view_View_INVISIBLE) * alpha);
	gtk_widget_set_sensitive(widget, visibility != android_view_View_INVISIBLE && alpha != 0.0f);
}

/** JavaWidget:
 * Minimal gtk widget class which does nothing.
 * Drawing will be overwritten by WrapperWidget.
 * If it holds children, they will be laid out by AndroidLayout
 */
struct _JavaWidget {
	GtkWidget parent_instance;
};
G_DECLARE_FINAL_TYPE(JavaWidget, java_widget, JAVA, WIDGET, GtkWidget)
static void java_widget_dispose(GObject *java_widget)
{
	GtkWidget *widget = GTK_WIDGET(java_widget);
	GtkWidget *child = gtk_widget_get_first_child(widget);
	while (child) {
		GtkWidget *_child = gtk_widget_get_next_sibling(child);
		gtk_widget_unparent(child);
		child = _child;
	}
}
static void java_widget_init(JavaWidget *java_widget) {}
static void java_widget_class_init(JavaWidgetClass *class)
{
	G_OBJECT_CLASS(class)->dispose = java_widget_dispose;
}
G_DEFINE_TYPE(JavaWidget, java_widget, GTK_TYPE_WIDGET)

JNIEXPORT jlong JNICALL Java_android_view_View_native_1constructor(JNIEnv *env, jobject this, jobject context, jobject attrs)
{
	WrapperWidget *wrapper = g_object_ref(WRAPPER_WIDGET(wrapper_widget_new()));
	GtkWidget *widget = g_object_new(java_widget_get_type(), NULL);
	gtk_widget_set_layout_manager(widget, android_layout_new(this));
	wrapper_widget_set_child(WRAPPER_WIDGET(wrapper), widget);
	wrapper_widget_set_jobject(WRAPPER_WIDGET(wrapper), env, this);

	jclass class = _CLASS(this);
	jstring nameObj = (*env)->CallObjectMethod(env, class,
	                                           _METHOD(_CLASS(class), "getName", "()Ljava/lang/String;"));
	const char *name = (*env)->GetStringUTFChars(env, nameObj, NULL);
	gtk_widget_set_name(widget, name);
	(*env)->ReleaseStringUTFChars(env, nameObj, name);

	if (_METHOD(class, "onGenericMotionEvent", "(Landroid/view/MotionEvent;)Z") != handle_cache.view.onGenericMotionEvent
	    || _METHOD(class, "dispatchGenericMotionEvent", "(Landroid/view/MotionEvent;)Z") != handle_cache.view.dispatchGenericMotionEvent) {
		GtkEventController *controller = gtk_event_controller_scroll_new(GTK_EVENT_CONTROLLER_SCROLL_VERTICAL);

		g_signal_connect(controller, "scroll", G_CALLBACK(scroll_cb), wrapper->jobj);
		gtk_widget_add_controller(GTK_WIDGET(wrapper), controller);
	}
	if (_METHOD(class, "dispatchHoverEvent", "(Landroid/view/MotionEvent;)Z") != handle_cache.view.dispatchHoverEvent) {
		GtkEventController *controller = gtk_event_controller_motion_new();

		g_signal_connect(controller, "enter", G_CALLBACK(hover_enter_cb), wrapper);
		g_signal_connect(controller, "leave", G_CALLBACK(hover_leave_cb), wrapper);
		g_signal_connect(controller, "motion", G_CALLBACK(hover_motion_cb), wrapper);
		gtk_widget_add_controller(GTK_WIDGET(wrapper), controller);
	}

	return _INTPTR(widget);
}

JNIEXPORT void JNICALL Java_android_view_View_nativeInvalidate(JNIEnv *env, jclass class, jlong widget_ptr)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));

	wrapper_widget_queue_draw(WRAPPER_WIDGET(gtk_widget_get_parent(widget)));
}

JNIEXPORT void JNICALL Java_android_view_View_native_1destructor(JNIEnv *env, jobject this, jlong widget_ptr)
{
	g_object_unref(gtk_widget_get_parent(_PTR(widget_ptr)));
}

#define MEASURE_SPEC_UNSPECIFIED (0 << 30)
#define MEASURE_SPEC_EXACTLY     (1 << 30)
#define MEASURE_SPEC_AT_MOST     (2 << 30)
#define MEASURE_SPEC_MASK        (0x3 << 30)

JNIEXPORT void JNICALL Java_android_view_View_native_1measure(JNIEnv *env, jobject this, jlong widget_ptr, jint width_spec, jint height_spec)
{
	int width = -1;
	int height = -1;
	GtkWidget *widget = gtk_widget_get_parent(GTK_WIDGET(_PTR(widget_ptr)));
	int width_spec_size = width_spec & ~MEASURE_SPEC_MASK;
	int height_spec_size = height_spec & ~MEASURE_SPEC_MASK;
	int width_spec_mode = width_spec & MEASURE_SPEC_MASK;
	int height_spec_mode = height_spec & MEASURE_SPEC_MASK;
	GtkSizeRequestMode request_mode;

	if (width_spec_mode == MEASURE_SPEC_EXACTLY) {
		width = width_spec_size;
		request_mode = GTK_SIZE_REQUEST_HEIGHT_FOR_WIDTH;
	}
	if (height_spec_mode == MEASURE_SPEC_EXACTLY) {
		height = height_spec_size;
		request_mode = GTK_SIZE_REQUEST_WIDTH_FOR_HEIGHT;
	}
	if (width == -1 && height == -1)
		request_mode = gtk_widget_get_request_mode(widget);
	if (width == -1 && request_mode == GTK_SIZE_REQUEST_HEIGHT_FOR_WIDTH) {
		gtk_widget_measure(widget, GTK_ORIENTATION_HORIZONTAL, height, NULL, &width, NULL, NULL);
		if (width_spec_mode == MEASURE_SPEC_AT_MOST && width > width_spec_size)
			width = width_spec_size;
	}
	if (height == -1) {
		gtk_widget_measure(widget, GTK_ORIENTATION_VERTICAL, width, NULL, &height, NULL, NULL);
		if (height_spec_mode == MEASURE_SPEC_AT_MOST && height > height_spec_size)
			height = height_spec_size;
	}
	if (width == -1) {
		gtk_widget_measure(widget, GTK_ORIENTATION_HORIZONTAL, height, NULL, &width, NULL, NULL);
		if (width_spec_mode == MEASURE_SPEC_AT_MOST && width > width_spec_size)
			width = width_spec_size;
	}

	(*env)->CallVoidMethod(env, this, handle_cache.view.setMeasuredDimension, width, height);
}

JNIEXPORT void JNICALL Java_android_view_View_native_1layout(JNIEnv *env, jobject this, jlong widget_ptr, jint l, jint t, jint r, jint b)
{
	GtkWidget *widget = gtk_widget_get_parent(GTK_WIDGET(_PTR(widget_ptr)));

	GtkAllocation allocation = {
		.x = l,
		.y = t,
	};
	int width = r - l;
	int height = b - t;
	WrapperWidget *wrapper = WRAPPER_WIDGET(widget);
	if (wrapper->real_width != width || wrapper->real_height != height) {
		wrapper->real_width = width;
		wrapper->real_height = height;
		if (!wrapper->needs_allocation)
			gtk_widget_queue_allocate(widget);
	}
	if (wrapper->needs_allocation) {
		allocation.width = width;
		allocation.height = height;
	}

	if (wrapper->jobj) {
		allocation.x += _GET_FLOAT_FIELD(wrapper->jobj, "translationX");
		allocation.y += _GET_FLOAT_FIELD(wrapper->jobj, "translationY");
	}

	gtk_widget_size_allocate(widget, &allocation, -1);
}

JNIEXPORT void JNICALL Java_android_view_View_native_1requestLayout(JNIEnv *env, jobject this, jlong widget_ptr)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));

	atl_safe_gtk_widget_queue_resize(widget);
}

/* we kinda need per-widget css */
#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wdeprecated-declarations"
JNIEXPORT void JNICALL Java_android_view_View_native_1setBackgroundColor(JNIEnv *env, jobject this, long widget_ptr, jint color)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));
	WrapperWidget *wrapper = WRAPPER_WIDGET(gtk_widget_get_parent(widget));
	wrapper_widget_set_background(wrapper, NULL);

	GtkStyleContext *style_context = gtk_widget_get_style_context(widget);

	GtkCssProvider *old_provider = g_object_get_data(G_OBJECT(widget), "background_style_provider");
	if (old_provider)
		gtk_style_context_remove_provider(style_context, GTK_STYLE_PROVIDER(old_provider));

	GtkCssProvider *css_provider = gtk_css_provider_new();

	char *css_string = g_markup_printf_escaped("* { background-image: none; background-color: #%06x%02x; }", color & 0xFFFFFF, (color >> 24) & 0xFF);
	gtk_css_provider_load_from_string(css_provider, css_string);
	g_free(css_string);

	gtk_style_context_add_provider(style_context, GTK_STYLE_PROVIDER(css_provider), GTK_STYLE_PROVIDER_PRIORITY_APPLICATION);
	g_object_set_data(G_OBJECT(widget), "background_style_provider", css_provider);
	if (((color >> 24) & 0xFF) != 0)
		widget_set_needs_allocation(widget);
}

JNIEXPORT void JNICALL Java_android_view_View_native_1setBackgroundDrawable(JNIEnv *env, jobject this, jlong widget_ptr, jlong paintable_ptr)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));
	GdkPaintable *paintable = GDK_PAINTABLE(_PTR(paintable_ptr));
	wrapper_widget_set_background(WRAPPER_WIDGET(gtk_widget_get_parent(widget)), paintable);
	GtkStyleContext *style_context = gtk_widget_get_style_context(widget);

	GtkCssProvider *old_provider = g_object_get_data(G_OBJECT(widget), "background_style_provider");
	if (old_provider)
		gtk_style_context_remove_provider(style_context, GTK_STYLE_PROVIDER(old_provider));

	GtkCssProvider *css_provider = gtk_css_provider_new();

	char *css_string = g_markup_printf_escaped("* { background-image: none; background-color: #00000000; }");
	gtk_css_provider_load_from_string(css_provider, css_string);
	g_free(css_string);

	gtk_style_context_add_provider(style_context, GTK_STYLE_PROVIDER(css_provider), GTK_STYLE_PROVIDER_PRIORITY_APPLICATION);
	g_object_set_data(G_OBJECT(widget), "background_style_provider", css_provider);
	if (paintable)
		widget_set_needs_allocation(widget);
}
#pragma GCC diagnostic pop

JNIEXPORT jboolean JNICALL Java_android_view_View_native_1getGlobalVisibleRect(JNIEnv *env, jobject this, jlong widget_ptr, jobject rect)
{
	GtkWidget *widget = gtk_widget_get_parent(GTK_WIDGET(_PTR(widget_ptr)));
	graphene_point_t point_in = {0, 0};
	graphene_point_t point_out;
	double off_x;
	double off_y;
	gboolean ret;
	GtkWidget *window = GTK_WIDGET(gtk_widget_get_native(widget));
	gtk_native_get_surface_transform(GTK_NATIVE(window), &off_x, &off_y);
	ret = gtk_widget_compute_point(widget, window, &point_in, &point_out);
	if (!ret)
		return false;
	_SET_INT_FIELD(rect, "left", point_out.x + off_x);
	_SET_INT_FIELD(rect, "top", point_out.y + off_y);
	point_in = (graphene_point_t){Java_android_view_View_getWidth(env, this), Java_android_view_View_getHeight(env, this)};
	ret = gtk_widget_compute_point(widget, window, &point_in, &point_out);
	if (!ret)
		return false;
	_SET_INT_FIELD(rect, "right", point_out.x + off_x);
	_SET_INT_FIELD(rect, "bottom", point_out.y + off_y);
	return true;
}

static void on_long_click(GtkGestureLongPress *gesture, double x, double y, gpointer user_data)
{
	JNIEnv *env = get_jni_env();
	GtkWidget *widget = gtk_event_controller_get_widget(GTK_EVENT_CONTROLLER(gesture));
	WrapperWidget *wrapper = WRAPPER_WIDGET(gtk_widget_get_parent(widget));

	bool ret = (*env)->CallBooleanMethod(env, wrapper->jobj, handle_cache.view.performLongClick, x, y);

	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);

	if (ret)
		gtk_gesture_set_state(GTK_GESTURE(gesture), GTK_EVENT_SEQUENCE_CLAIMED);
}

static void on_long_click_update(GtkGesture *gesture, GdkEventSequence *sequence, gpointer user_data)
{
	GdkEvent *event = gtk_gesture_get_last_event(gesture, sequence);
	if (event == canceled_event) {
		gtk_event_controller_reset(GTK_EVENT_CONTROLLER(gesture));
	}
}

JNIEXPORT void JNICALL Java_android_view_View_nativeSetOnLongClickListener(JNIEnv *env, jobject this, jlong widget_ptr)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));
	GtkEventController *old_controller = g_object_get_data(G_OBJECT(widget), "on_long_click_listener");
	if (old_controller)
		return;

	GtkEventController *controller = GTK_EVENT_CONTROLLER(gtk_gesture_long_press_new());

	g_signal_connect(controller, "pressed", G_CALLBACK(on_long_click), NULL);
	g_signal_connect(controller, "update", G_CALLBACK(on_long_click_update), NULL);
	gtk_widget_add_controller(widget, controller);
	g_object_set_data(G_OBJECT(widget), "on_long_click_listener", controller);
}

JNIEXPORT jboolean JNICALL Java_android_view_View_native_1getMatrix(JNIEnv *env, jobject this, jlong widget_ptr, jlong matrix_ptr)
{
	GtkWidget *widget = gtk_widget_get_parent(GTK_WIDGET(_PTR(widget_ptr)));
	return gtk_widget_compute_transform(gtk_widget_get_parent(widget), widget, _PTR(matrix_ptr));
}

JNIEXPORT void JNICALL Java_android_view_View_native_1queueAllocate(JNIEnv *env, jobject this, jlong widget_ptr)
{
	atl_safe_gtk_widget_queue_allocate(GTK_WIDGET(_PTR(widget_ptr)));
}

JNIEXPORT void JNICALL Java_android_view_View_native_1drawBackground(JNIEnv *env, jobject this, jlong widget_ptr, jlong snapshot_ptr)
{
	WrapperWidget *wrapper = WRAPPER_WIDGET(gtk_widget_get_parent(GTK_WIDGET(_PTR(widget_ptr))));
	GdkSnapshot *snapshot = GDK_SNAPSHOT(_PTR(snapshot_ptr));
	if (wrapper->background)
		gtk_widget_snapshot_child(&wrapper->parent_instance, wrapper->background, snapshot);
}

JNIEXPORT void JNICALL Java_android_view_View_native_1drawContent(JNIEnv *env, jobject this, jlong widget_ptr, jlong snapshot_ptr)
{
	WrapperWidget *wrapper = WRAPPER_WIDGET(gtk_widget_get_parent(GTK_WIDGET(_PTR(widget_ptr))));
	GdkSnapshot *snapshot = GDK_SNAPSHOT(_PTR(snapshot_ptr));
	gtk_widget_snapshot_child(&wrapper->parent_instance, wrapper->child, snapshot);
}

JNIEXPORT void JNICALL Java_android_view_View_nativeSetFullscreen(JNIEnv *env, jobject this, jlong widget_ptr, jboolean fullscreen)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));
	GtkWindow *window = GTK_WINDOW(gtk_widget_get_native(widget));
	if (fullscreen) {
		if (gtk_window_is_maximized(window)) {
			gtk_window_fullscreen(window);
			// Android apps expect the fullscreen to happen immediately, so we need to mock the fullscreened window size
			if (ATL_IS_ANDROID_LAYOUT(gtk_widget_get_layout_manager(widget))) {
				AndroidLayout *layout = ATL_ANDROID_LAYOUT(gtk_widget_get_layout_manager(widget));
				GdkSurface *surface = gtk_native_get_surface(GTK_NATIVE(window));
				GdkDisplay *display = gdk_surface_get_display(surface);
				GdkMonitor *monitor = gdk_display_get_monitor_at_surface(display, surface);
				GdkRectangle geometry;
				gdk_monitor_get_geometry(monitor, &geometry);
				// check if fullscreen is slightly larger than the window, if so, we can safely mock the new size
				if (layout->real_width <= geometry.width && layout->real_width > geometry.width - 200 && layout->real_height <= geometry.height && layout->real_height > geometry.height - 200) {
					layout->real_width = geometry.width;
					layout->real_height = geometry.height;
					_SET_INT_FIELD(this, "right", geometry.width);
					_SET_INT_FIELD(this, "bottom", geometry.height);
				}
			}
		} else {
			printf("blocking fullscreen request, because window is not maximized\n");
		}
	} else {
		gtk_window_unfullscreen(window);
	}
}

JNIEXPORT void JNICALL Java_android_view_View_native_1addClass(JNIEnv *env, jobject this, jlong widget_ptr, jstring class_name_jstr)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));

	const char *class_name = (*env)->GetStringUTFChars(env, class_name_jstr, NULL);
	gtk_widget_add_css_class(widget, class_name);

	(*env)->ReleaseStringUTFChars(env, class_name_jstr, class_name);
}

JNIEXPORT void JNICALL Java_android_view_View_native_1removeClass(JNIEnv *env, jobject this, jlong widget_ptr, jstring class_name_jstr)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));

	const char *class_name = (*env)->GetStringUTFChars(env, class_name_jstr, NULL);
	gtk_widget_remove_css_class(widget, class_name);

	(*env)->ReleaseStringUTFChars(env, class_name_jstr, class_name);
}

JNIEXPORT void JNICALL Java_android_view_View_native_1addClasses(JNIEnv *env, jobject this, jlong widget_ptr, jobjectArray class_names_jarray)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));

	int length = (*env)->GetArrayLength(env, class_names_jarray);

	for (int i = 0; i < length; i++) {
		jstring class_name_jstr = (jstring)((*env)->GetObjectArrayElement(env, class_names_jarray, i));

		const char *class_name = (*env)->GetStringUTFChars(env, class_name_jstr, NULL);
		gtk_widget_add_css_class(widget, class_name);

		(*env)->ReleaseStringUTFChars(env, class_name_jstr, class_name);
	}
}

JNIEXPORT void JNICALL Java_android_view_View_native_1removeClasses(JNIEnv *env, jobject this, jlong widget_ptr, jobjectArray class_names_jarray)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));

	int length = (*env)->GetArrayLength(env, class_names_jarray);

	for (int i = 0; i < length; i++) {
		jstring class_name_jstr = (jstring)((*env)->GetObjectArrayElement(env, class_names_jarray, i));

		const char *class_name = (*env)->GetStringUTFChars(env, class_name_jstr, NULL);
		gtk_widget_remove_css_class(widget, class_name);

		(*env)->ReleaseStringUTFChars(env, class_name_jstr, class_name);
	}
}

JNIEXPORT void JNICALL Java_android_view_View_nativeRequestFocus(JNIEnv *env, jobject this, jlong widget_ptr, jint direction)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));
	GtkWidget *wrapper = gtk_widget_get_parent(widget);
	if (gtk_widget_get_focusable(widget))
		gtk_widget_grab_focus(widget);
	else
		gtk_widget_grab_focus(wrapper);
}

JNIEXPORT jboolean JNICALL Java_android_view_View_nativeIsFocused(JNIEnv *env, jobject this, jlong widget_ptr)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));
	GtkWidget *wrapper = gtk_widget_get_parent(widget);
	return gtk_widget_has_focus(widget) || gtk_widget_has_focus(wrapper);
}

JNIEXPORT void JNICALL Java_android_view_View_native_1keep_1screen_1on(JNIEnv *env, jobject this, jlong widget_ptr, jboolean enable)
{
	GtkApplication *application = GTK_APPLICATION(g_application_get_default());
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));
	guint cookie = GPOINTER_TO_INT(g_object_get_data(G_OBJECT(widget), "keep-screen-on-cookie"));
	if (cookie && !enable) {
		gtk_application_uninhibit(application, cookie);
		g_object_set_data(G_OBJECT(widget), "keep-screen-on-cookie", NULL);
	} else if (!cookie && enable) {
		GtkWindow *window = GTK_WINDOW(gtk_widget_get_native(widget));
		GtkApplicationInhibitFlags flags = GTK_APPLICATION_INHIBIT_SUSPEND | GTK_APPLICATION_INHIBIT_IDLE;
		cookie = gtk_application_inhibit(application, window, flags, "keep-screen-on");
		g_object_set_data(G_OBJECT(widget), "keep-screen-on-cookie", GINT_TO_POINTER(cookie));
	}
}

JNIEXPORT jboolean JNICALL Java_android_view_View_nativeIsAttachedToWindow(JNIEnv *env, jobject this, jlong widget_ptr)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));
	return currently_unmapping == widget || gtk_widget_get_mapped(widget);
}

JNIEXPORT jobject JNICALL Java_android_view_View_native_1get_1window(JNIEnv *env, jobject this, jlong widget_ptr)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));
	GtkRoot *root = gtk_widget_get_root(widget);
	if (!root)
		return NULL;
	return g_object_get_data(G_OBJECT(root), "jobject");
}

extern GtkWindow *window;

JNIEXPORT void JNICALL Java_android_view_View_getWindowVisibleDisplayFrame(JNIEnv *env, jobject this, jobject rect)
{
	int width;
	int height;
	gtk_window_get_default_size(window, &width, &height);
	_SET_INT_FIELD(rect, "left", 0);
	_SET_INT_FIELD(rect, "top", 0);
	_SET_INT_FIELD(rect, "right", width);
	_SET_INT_FIELD(rect, "bottom", height);
}

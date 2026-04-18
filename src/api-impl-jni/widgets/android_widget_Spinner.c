#include <gtk/gtk.h>

#include "../defines.h"
#include "../util.h"

#include "AdapterView.h"
#include "WrapperWidget.h"

#include "../generated_headers/android_view_View.h"
#include "../generated_headers/android_widget_Spinner.h"

static void range_list_model_init(RangeListModel *list_model) {}
static void range_list_model_class_init(RangeListModelClass *class) {}

static guint range_list_model_get_n_items(GListModel *list_model)
{
	return (RANGE_LIST_MODEL(list_model))->n_items;
}

static gpointer range_list_model_get_item(GListModel *list_model, guint index)
{
	if (index >= RANGE_LIST_MODEL(list_model)->n_items)
		return NULL;
	RangeListItem *item = g_object_new(range_list_item_get_type(), NULL);
	item->model = RANGE_LIST_MODEL(list_model);
	return item;
}

static void range_list_model_model_init(GListModelInterface *iface)
{
	iface->get_n_items = range_list_model_get_n_items;
	iface->get_item_type = (GType (*)(GListModel *))range_list_item_get_type;
	iface->get_item = range_list_model_get_item;
}

G_DEFINE_TYPE_WITH_CODE(RangeListModel, range_list_model, G_TYPE_OBJECT,
                        G_IMPLEMENT_INTERFACE(G_TYPE_LIST_MODEL, range_list_model_model_init))

static void range_list_item_class_init(RangeListItemClass *cls) {}
static void range_list_item_init(RangeListItem *self) {}
G_DEFINE_TYPE(RangeListItem, range_list_item, G_TYPE_OBJECT)

static void bind_listitem_cb(GtkListItemFactory *factory, GtkListItem *list_item, jobject this)
{
	JNIEnv *env = get_jni_env();

	guint index = gtk_list_item_get_position(list_item);
	WrapperWidget *wrapper = WRAPPER_WIDGET(gtk_list_item_get_child(list_item));
	RangeListModel *model = RANGE_LIST_ITEM(gtk_list_item_get_item(list_item))->model;
	int n_items = g_list_model_get_n_items(G_LIST_MODEL(model));
	if (index >= n_items) {
		printf("invalid index: %d >= %d\n", index, n_items);
		exit(0);
	}
	jmethodID getView = _METHOD(_CLASS(model->adapter), "getDropDownView", "(ILandroid/view/View;Landroid/view/ViewGroup;)Landroid/view/View;");
	jobject view = (*env)->CallObjectMethod(env, model->adapter, getView, index, wrapper ? wrapper->jobj : NULL, model->jobject);
	view = _REF(view);
	GtkWidget *child = gtk_widget_get_parent(GTK_WIDGET(_PTR(_GET_LONG_FIELD(view, "widget"))));
	jobject background_drawable = _GET_OBJ_FIELD(this, "popupBackground", "Landroid/graphics/drawable/Drawable;");
	GdkPaintable *background_paintable = background_drawable ? GDK_PAINTABLE(_PTR(_GET_LONG_FIELD(background_drawable, "paintable"))) : NULL;
	wrapper_widget_set_background(WRAPPER_WIDGET(child), background_paintable);
	gtk_list_item_set_child(list_item, child);
}

JNIEXPORT jlong JNICALL Java_android_widget_Spinner_native_1constructor(JNIEnv *env, jobject this, jobject context, jobject attrs)
{
	WrapperWidget *wrapper = g_object_ref(WRAPPER_WIDGET(wrapper_widget_new()));
	wrapper_widget_set_jobject(wrapper, env, this);
	GtkListItemFactory *factory = gtk_signal_list_item_factory_new();
	g_signal_connect(factory, "bind", G_CALLBACK(bind_listitem_cb), wrapper->jobj);
	RangeListModel *model = g_object_new(range_list_model_get_type(), NULL);
	GtkWidget *dropdown = gtk_drop_down_new(G_LIST_MODEL(model), NULL);
	gtk_drop_down_set_factory(GTK_DROP_DOWN(dropdown), factory);
	model->list_view = dropdown;
	model->jobject = _WEAK_REF(this);
	wrapper_widget_set_child(WRAPPER_WIDGET(wrapper), dropdown);
	gtk_widget_set_name(dropdown, "Spinner");
	return _INTPTR(dropdown);
}

JNIEXPORT void JNICALL Java_android_widget_Spinner_native_1setAdapter(JNIEnv *env, jobject this, jlong widget_ptr, jobject adapter)
{
	GtkDropDown *dropdown = GTK_DROP_DOWN(_PTR(widget_ptr));
	RangeListModel *model = RANGE_LIST_MODEL(gtk_drop_down_get_model(dropdown));

	if (model->adapter)
		_UNREF(model->adapter);
	model->adapter = adapter ? _REF(adapter) : NULL;
	guint old_n_items = model->n_items;
	model->n_items = adapter ? (*env)->CallIntMethod(env, adapter, _METHOD(_CLASS(adapter), "getCount", "()I")) : 0;
	g_list_model_items_changed(G_LIST_MODEL(model), 0, old_n_items, model->n_items);
}

static void on_selected_changed(GtkDropDown *dropdown, GParamSpec *pspec, jobject listener)
{
	JNIEnv *env = get_jni_env();
	int index = gtk_drop_down_get_selected(dropdown);
	gpointer selected = gtk_drop_down_get_selected_item(dropdown);
	if (!selected)
		return;
	RangeListModel *model = RANGE_LIST_ITEM(selected)->model;
	jmethodID onItemSelected = _METHOD(_CLASS(listener), "onItemSelected", "(Landroid/widget/AdapterView;Landroid/view/View;IJ)V");
	(*env)->CallVoidMethod(env, listener, onItemSelected, model->jobject, NULL, index, (long)0);
}

JNIEXPORT void JNICALL Java_android_widget_Spinner_setOnItemSelectedListener(JNIEnv *env, jobject this, jobject listener)
{
	GtkDropDown *dropdown = GTK_DROP_DOWN(_PTR(_GET_LONG_FIELD(this, "widget")));
	g_signal_connect(dropdown, "notify::selected", G_CALLBACK(on_selected_changed), _REF(listener));
}

JNIEXPORT void JNICALL Java_android_widget_Spinner_native_1setBackgroundDrawable(JNIEnv *env, jobject this, jlong widget_ptr, jlong paintable_ptr)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));
	// background must be set to the GtkToggleButton which is the first child of the GtkDropDown
	Java_android_view_View_native_1setBackgroundDrawable(env, this, _INTPTR(gtk_widget_get_first_child(widget)), paintable_ptr);
}

JNIEXPORT void JNICALL Java_android_widget_Spinner_native_1setBackgroundColor(JNIEnv *env, jobject this, jlong widget_ptr, jint color)
{
	GtkWidget *widget = GTK_WIDGET(_PTR(widget_ptr));
	// background must be set to the GtkToggleButton which is the first child of the GtkDropDown
	Java_android_view_View_native_1setBackgroundColor(env, this, _INTPTR(gtk_widget_get_first_child(widget)), color);
}

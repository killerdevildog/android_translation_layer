#include <gtk/gtk.h>
#include <jni.h>

#include "../defines.h"
#include "../util.h"
#include "../generated_headers/android_app_AlertDialog.h"

JNIEXPORT void JNICALL Java_android_app_AlertDialog_nativeSetMessage(JNIEnv *env, jobject this, jlong ptr, jstring message)
{
	GtkWindow *dialog = GTK_WINDOW(_PTR(ptr));
	const char *nativeMessage = (*env)->GetStringUTFChars(env, message, NULL);
	GtkWidget *content_area = gtk_window_get_child(dialog);
	GtkWidget *label = gtk_label_new(nativeMessage);
	gtk_label_set_wrap(GTK_LABEL(label), TRUE);
	gtk_label_set_max_width_chars(GTK_LABEL(label), 50);
	gtk_box_append(GTK_BOX(content_area), label);
	(*env)->ReleaseStringUTFChars(env, message, nativeMessage);
}

JNIEXPORT void JNICALL Java_android_app_AlertDialog_nativeSetButton(JNIEnv *env, jobject this, jlong ptr, jlong widget_ptr)
{
	GtkWindow *dialog = GTK_WINDOW(_PTR(ptr));
	GtkWidget *content_area = gtk_window_get_child(dialog);
	GtkWidget *button = GTK_WIDGET(_PTR(widget_ptr));
	gtk_box_append(GTK_BOX(content_area), gtk_widget_get_parent(button));
}

struct click_callback_data {
	JavaVM *jvm;
	jobject this;
	jobject on_click_listener;
	jmethodID on_click_method;
};

struct _ListEntry {
	GObject parent;
	const char *text;
};
G_DECLARE_FINAL_TYPE(ListEntry, list_entry, ATL, LIST_ENTRY, GObject);
static void list_entry_class_init(ListEntryClass *cls) {}
static void list_entry_init(ListEntry *self) {}
G_DEFINE_TYPE(ListEntry, list_entry, G_TYPE_OBJECT)

static void setup_listitem_cb(GtkListItemFactory *factory, GtkListItem *list_item)
{
	gtk_list_item_set_child(list_item, gtk_label_new(""));
}

static void bind_listitem_cb(GtkListItemFactory *factory, GtkListItem *list_item)
{
	GtkWidget *label = gtk_list_item_get_child(list_item);
	ListEntry *entry = gtk_list_item_get_item(list_item);

	atl_safe_gtk_label_set_text(GTK_LABEL(label), entry->text);
}

static void activate_cb(GtkListView *list, guint position, struct click_callback_data *d)
{
	JNIEnv *env;
	(*d->jvm)->GetEnv(d->jvm, (void **)&env, JNI_VERSION_1_6);

	(*env)->CallVoidMethod(env, d->on_click_listener, d->on_click_method, d->this, position);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
}

JNIEXPORT void JNICALL Java_android_app_AlertDialog_nativeSetItems(JNIEnv *env, jobject this, jlong ptr, jobjectArray items, jobject on_click)
{
	GtkWindow *dialog = GTK_WINDOW(_PTR(ptr));

	GListStore *store = g_list_store_new(list_entry_get_type());
	int stringCount = (*env)->GetArrayLength(env, items);
	for (int i = 0; i < stringCount; i++) {
		ListEntry *entry = ATL_LIST_ENTRY(g_object_new(list_entry_get_type(), NULL));
		entry->text = _CSTRING((*env)->GetObjectArrayElement(env, items, i));
		g_list_store_append(store, entry);
	}

	GtkListItemFactory *factory = gtk_signal_list_item_factory_new();
	g_signal_connect(factory, "setup", G_CALLBACK(setup_listitem_cb), NULL);
	g_signal_connect(factory, "bind", G_CALLBACK(bind_listitem_cb), NULL);
	GtkWidget *list = gtk_list_view_new(GTK_SELECTION_MODEL(gtk_single_selection_new(G_LIST_MODEL(store))), factory);
	gtk_list_view_set_single_click_activate(GTK_LIST_VIEW(list), TRUE);

	GtkWidget *content_area = gtk_window_get_child(dialog);
	gtk_box_append(GTK_BOX(content_area), list);

	JavaVM *jvm;
	(*env)->GetJavaVM(env, &jvm);

	struct click_callback_data *callback_data = malloc(sizeof(struct click_callback_data));
	callback_data->jvm = jvm;
	callback_data->this = _REF(this);
	callback_data->on_click_listener = _REF(on_click);
	callback_data->on_click_method = _METHOD(_CLASS(on_click), "onClick", "(Landroid/content/DialogInterface;I)V");

	g_signal_connect(list, "activate", G_CALLBACK(activate_cb), callback_data);
}

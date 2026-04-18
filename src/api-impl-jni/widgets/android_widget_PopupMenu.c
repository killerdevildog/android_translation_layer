#include <gtk/gtk.h>

#include "../defines.h"
#include "../util.h"

#include "../generated_headers/android_widget_PopupMenu.h"

JNIEXPORT jlong JNICALL Java_android_widget_PopupMenu_native_1init(JNIEnv *env, jobject this)
{
	return _INTPTR(g_menu_new());
}

JNIEXPORT void JNICALL Java_android_widget_PopupMenu_native_1insertItem(JNIEnv *env, jobject this, jlong menu_ptr, jint position, jstring title_jstr, jint id)
{
	const gchar *title = (*env)->GetStringUTFChars(env, title_jstr, NULL);
	printf("insertItem position: %d title: %s\n", position, title);
	GMenuItem *item = g_menu_item_new(title, NULL);
	(*env)->ReleaseStringUTFChars(env, title_jstr, title);
	g_menu_item_set_action_and_target(item, "popupmenu.clicked", "i", id);
	g_menu_insert_item(G_MENU(_PTR(menu_ptr)), position, item);
	g_object_unref(item);
}

JNIEXPORT void JNICALL Java_android_widget_PopupMenu_native_1insertSubmenu(JNIEnv *env, jobject this, jlong menu_ptr, jint position, jstring title_jstr, jlong submenu_ptr)
{
	const gchar *title = (*env)->GetStringUTFChars(env, title_jstr, NULL);
	printf("insertSubmenu position: %d title: %s\n", position, title);
	GMenuItem *item = g_menu_item_new_submenu(title, G_MENU_MODEL(_PTR(submenu_ptr)));
	(*env)->ReleaseStringUTFChars(env, title_jstr, title);
	g_menu_insert_item(G_MENU(_PTR(menu_ptr)), position, item);
	g_object_unref(item);
}

JNIEXPORT void JNICALL Java_android_widget_PopupMenu_native_1removeItem(JNIEnv *env, jobject this, jlong menu_ptr, jint position)
{
	g_menu_remove(G_MENU(_PTR(menu_ptr)), position);
}

static void popupmenu_activated(GSimpleAction *action, GVariant *parameter, gpointer user_data)
{
	int id = g_variant_get_int32(parameter);
	JNIEnv *env = get_jni_env();
	jobject this = (jobject)user_data;
	jmethodID onMenuItemClick = _METHOD(_CLASS(this), "menuItemClickCallback", "(I)V");
	(*env)->CallVoidMethod(env, this, onMenuItemClick, id);
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
}

static const GActionEntry action_entry = {"clicked", popupmenu_activated, "i", NULL, NULL};

JNIEXPORT jlong JNICALL Java_android_widget_PopupMenu_native_1buildPopover(JNIEnv *env, jobject this, jlong menu_ptr)
{
	GtkWidget *popover = gtk_popover_menu_new_from_model(G_MENU_MODEL(_PTR(menu_ptr)));
	GSimpleActionGroup *group = g_simple_action_group_new();
	g_action_map_add_action_entries(G_ACTION_MAP(group), &action_entry, 1, _REF(this));
	gtk_widget_insert_action_group(popover, "popupmenu", G_ACTION_GROUP(group));
	return _INTPTR(popover);
}

JNIEXPORT void JNICALL Java_android_widget_PopupMenu_native_1show(JNIEnv *env, jobject this, jlong popover_ptr, jlong anchor_ptr)
{
	GtkWidget *anchor = gtk_widget_get_parent(GTK_WIDGET(_PTR(anchor_ptr)));
	GtkPopover *popover = GTK_POPOVER(_PTR(popover_ptr));
	gtk_widget_insert_before(GTK_WIDGET(popover), GTK_WIDGET(anchor), NULL);
	gtk_popover_present(popover);
	gtk_popover_popup(popover);
}

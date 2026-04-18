#ifndef ANDROID_LAYOUT_H
#define ANDROID_LAYOUT_H

#include <gtk/gtk.h>
#include <jni.h>

#define MEASURE_SPEC_UNSPECIFIED (0 << 30)
#define MEASURE_SPEC_EXACTLY     (1 << 30)
#define MEASURE_SPEC_AT_MOST     (2 << 30)
#define MEASURE_SPEC_MASK        (0x3 << 30)

#define MATCH_PARENT             (-1)
#define WRAP_CONTENT             (-2)

G_DECLARE_FINAL_TYPE(AndroidLayout, android_layout, ATL, ANDROID_LAYOUT, GtkLayoutManager);

struct _AndroidLayout {
	GtkLayoutManager parent_instance;
	jobject view;
	int width;
	int height;
	int real_width;
	int real_height;
	gboolean needs_allocation;
};

GtkLayoutManager *android_layout_new(jobject view);
void android_layout_set_params(AndroidLayout *layout, int width, int height);

void widget_set_needs_allocation(GtkWidget *widget);

#endif // ANDROID_LAYOUT_H

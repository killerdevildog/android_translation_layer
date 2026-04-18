#### quick guide to implementing as-of-yet uinmplemented android APIs to make a new app work

If you try to run any random android app using the translation layer, you will most likely encounter errors 
telling you that some class/interface/function doesn't exist. If you wish to help improve the translation layer 
by adding support for the missing APIs (and if not you, who will), then read ahead :)

##### simplest case: stubbing

In a lot of cases, the functionality of the missing APIs is not really relevant to you. And even when it is, 
you would probably prefer to first get the app to launch without errors. With a lot of APIs, you can get away 
with writing stubs.

What is a stub? Well, the simplest stub would be an empty class, like this:
`src/api-impl/android/webkit/WebView.java:`
```Java
package android.webkit;

public class WebView {
}
```

This should fix the "no such class" error, and let you get further in your attempt to simply launch the app.  

__NOTE__: you may also get a "no such class" error for a class inside the app, caused by that class being 
a descendant of some android class that we're missing. Decompiling the app with e.g `jadx` should help
you figure out what class you need to stub out.

If the app uses a non-default constructor, you will need to provide that as well (empty is fine), and you will 
need to provide stub classes for any types used for paramters.

Since the class was needed, it's pretty likely that up next you will get a "no such method" error.  
(`java.lang.NoSuchMethodError: No [static] method [method_name]([parameters])[return type] in class L[class]`)  
The easiest case is a void method:
```Java
package android.webkit;

import android.content.Context;

public class WebView {
	public void doSomething(Context context) {
	}
}
```
Here, all that you need to take care of is that at `src/api-impl/android/content/Context.java`, you have at minimum 
a stub class.

Unfortunately, in the WebView case, the method that an app was trying call wasn't returning `void`. If this is 
the case, your best case scenario is that you can return some sort of value that will make you progress further. 
For example, if a method is called DoWeHaveInternetConnection, it's pretty likely that upon decompiling 
the app (e.g with `jadx`), you will find that returning `false` from that function makes the app decide 
not to attempt to use Internet-related APIs (which you might not feel like implementing at the moment).  
Sadly, in our case, the return type is an Object. If that's the case, and the Object is of a type not yet 
implemented, you can try simply making a stub class for said object, and then returning `null`.

```Java
package android.webkit;

import android.content.Context;

// the only reason we need to implement this is that some app developers are such scumbags that they try to use this for tracking purposes
public class WebView {
	public WebView (Context context) {
	}

	public WebSettings getSettings() {
		return null;
	}
}

```
this will obviously only work when the app checks for NULL, and decides to abandon whatever it was planning to 
do when NULL is returned. (and you don't mind that it does so)

When that's not an option, simply return a new instance of the stub class:
```Java
package android.webkit;

import android.content.Context;

// the only reason we need to implement this is that some app developers are such scumbags that they try to use this for tracking purposes
public class WebView {
	public WebView (Context context) {
	}

	public WebSettings getSettings() {
		return new WebSettings();
	}
}
```

This might be enough, but quite often, the returned Object's methods are called by the app. If that's the case,
simply create stub methods in that class same as we did above, and after a few iterations you should get to the
end of the rabbit hole.

__NOTE__: exceptions like `java.lang.NoSuchMethodException` may sometimes not provide information about what class
the method is supposed to be in (not even with the "or it's superclasses" disclaimer). If that happens, you will
again need to decompile the app's code and look around the place mentioned in the stack trace for calls to a method
of the name mentioned.

__NOTE__: in some cases, such as with enums and interfaces, you should be able to simply copy the APACHE-licensed
android code. With interfaces, you might want to comment out any methods not needed for the app you are trying
to get to work in order to cut down on the amount of stubbing you need to do. This also applies to simple
utilty classes (e.g. `android.util.Rational`).

Random Layout widgets can also mostly be copied from AOSP, with minor changes and maybe some commenting out
of things that make the code not compile (if they turn out to have been important, can always fix them properly later)

__IMPORTANT__: run `clang-format --style="{BasedOnStyle: InheritParentConfig, ReflowComments: IndentOnly}"`
(see CodeStyle.md) on any AOSP file that you are importing; manual code style changes are not required, but this
simple automatic step is. Make sure to run this before making any changes to the code, since e.g commented out
sections do not get reformatted by `clang-format`. __NOTE__: it is strongly suggested that you move operators
at the ends of lines to the next line, otherwise clang-format will collapse the whole line.

If you added any classes, make sure to add them in `src/api-impl/meson.build` (sorted alphabetically).

##### intriguing case: widgets

There are two basic types of widgets (Views): containers (Layouts) and the rest.

Initially all container widgets were backed by Gtk container widgets. As this caused lots of behaviour diffeneces with AOSP,
we have instead implemented the API of ViewGroup, which is the super class of all container widgets. This allows to more or
less completely reuse specialized container widget implementation from AOSP source code.

To implement any other widget, copy a widget that is closest to what you're looking for, and if Gtk has 
a better approximation for your widget, then change to that as the backing Gtk widget. If Gtk doesn't have 
anything close enough, you will need to implement your own widget. You might need to do that anyway, and wrap 
the close-enough Gtk widget, since subclassing is mostly not possible in Gtk.

__NOTE__: We now support widgets implemented fully in Java quite well, since that is necessary
to support most modern apps, so you can also copy widgets from AOSP with varying level of success.
If the the implementation details can in principle be relied upon by apps, this may in fact be necessary.
(In that case, the following doesn't apply.)

###### case study: ImageView

`src/api-impl/android/widget/ImageView.java`
```Java
package android.widget;
```
↑ most widgets are in this package, but not all of them are
```Java
import android.util.AttributeSet;
import android.content.Context;

import android.view.View;
```
↑ any widget will need to import these
```Java
public class ImageView extends View {
	public ImageView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ImageView(Context context) {
		this(context, null);
	}

	public ImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		haveCustomMeasure = false;
```
↑ TODO: explain this
```Java
		TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.ImageView, defStyleAttr, 0);
		setImageDrawable(a.getDrawable(com.android.internal.R.styleable.ImageView_src));
		setScaleType(scaletype_from_int[a.getInt(com.android.internal.R.styleable.ImageView_scaleType, 3 /*CENTER*/)]);
		a.recycle();
```
↑ you will probably want to add this stuff at a later point, here we apply properties from layout xml files
```Java
	}

	@Override
	protected native long native_constructor(Context context, AttributeSet attrs);
```
↑ at least these will be needed for any widget
```Java
	public /*native*/ void setImageResource(final int resid) {}
	public void setAdjustViewBounds(boolean adjustViewBounds) {}

	public void setScaleType(ScaleType scaleType) {}

    public enum ScaleType { ... }
}
```
↑ you might need some stubs, don't fall into the trap of thinking that you need to immediately implement everything

---

`src/api-impl-jni/widgets/android_widget_ImageView.c`
```C
#include <gtk/gtk.h>

#include "../defines.h"
#include "../util.h"

#include "WrapperWidget.h"

```
↑ every widget will be under `src/api-impl-jni/widgets/` and will have these includes
```C
#include "../generated_headers/android_widget_ImageView.h"
```
↑ this is the jni-generated header file (btw, t's name is what dicates the name of this .c file)
↓ native constructor
```C
JNIEXPORT jlong JNICALL Java_android_widget_ImageView_native_1constructor(JNIEnv *env, jobject this, jobject context, jobject attrs)
{
	GtkWidget *wrapper = g_object_ref(wrapper_widget_new());
```
↑ the wrapper widget is required, it's expected by generic functions operating on widgets; the purpose is to allow for things like background image
handling for cases where we can't subclass the backing widget itself
```C
	GtkWidget *image = gtk_picture_new();
```
↑ here we create the actual backing Gtk widget.
```C
	wrapper_widget_set_child(WRAPPER_WIDGET(wrapper), image);
	wrapper_widget_set_jobject(WRAPPER_WIDGET(wrapper), env, this);
```
↑ put the widget in the wrapper
```C
	return _INTPTR(image);
```
↑ the Java constructor will set the `widget` member of the View-derived class to the pointer to our widget that we return here; 
this will then be passed to other native functions that will operate on the backing widget.
```C
}

```

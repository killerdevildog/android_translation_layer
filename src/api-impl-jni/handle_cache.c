#include "util.h"

struct handle_cache handle_cache = {0};

void set_up_handle_cache(JNIEnv *env)
{
	handle_cache.activity.class = _REF((*env)->FindClass(env, "android/app/Activity"));
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
	handle_cache.activity.onCreate = _METHOD(handle_cache.activity.class, "onCreate", "(Landroid/os/Bundle;)V");
	handle_cache.activity.onPostCreate = _METHOD(handle_cache.activity.class, "onPostCreate", "(Landroid/os/Bundle;)V");
	handle_cache.activity.onStart = _METHOD(handle_cache.activity.class, "onStart", "()V");
	handle_cache.activity.onWindowFocusChanged = _METHOD(handle_cache.activity.class, "onWindowFocusChanged", "(Z)V");
	handle_cache.activity.onResume = _METHOD(handle_cache.activity.class, "onResume", "()V");
	handle_cache.activity.onPostResume = _METHOD(handle_cache.activity.class, "onPostResume", "()V");
	handle_cache.activity.onDestroy = _METHOD(handle_cache.activity.class, "onDestroy", "()V");
	handle_cache.activity.onStop = _METHOD(handle_cache.activity.class, "onStop", "()V");
	handle_cache.activity.onPause = _METHOD(handle_cache.activity.class, "onPause", "()V");
	handle_cache.activity.onBackPressed = _METHOD(handle_cache.activity.class, "onBackPressed", "()V");
	handle_cache.activity.onNewIntent = _METHOD(handle_cache.activity.class, "onNewIntent", "(Landroid/content/Intent;)V");

	handle_cache.attribute_set.class = _REF((*env)->FindClass(env, "android/util/AttributeSet"));
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
	handle_cache.attribute_set.getAttributeValue_string = _METHOD(handle_cache.attribute_set.class, "getAttributeValue", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
	handle_cache.attribute_set.getAttributeValue_int = _METHOD(handle_cache.attribute_set.class, "getAttributeIntValue", "(Ljava/lang/String;Ljava/lang/String;I)I");

	handle_cache.array_list.class = _REF((*env)->FindClass(env, "java/util/ArrayList"));
	handle_cache.array_list.add = _METHOD(handle_cache.array_list.class, "add", "(Ljava/lang/Object;)Z");
	handle_cache.array_list.remove = _METHOD(handle_cache.array_list.class, "remove", "(Ljava/lang/Object;)Z");
	handle_cache.array_list.get = _METHOD(handle_cache.array_list.class, "get", "(I)Ljava/lang/Object;");
	handle_cache.array_list.size = _METHOD(handle_cache.array_list.class, "size", "()I");
	handle_cache.array_list.clear = _METHOD(handle_cache.array_list.class, "clear", "()V");

	handle_cache.paint.class = _REF((*env)->FindClass(env, "android/graphics/Paint"));
	handle_cache.paint.getColor = _METHOD(handle_cache.paint.class, "getColor", "()I");

	handle_cache.motion_event.class = _REF((*env)->FindClass(env, "android/view/MotionEvent"));
	handle_cache.motion_event.constructor = _METHOD(handle_cache.motion_event.class, "<init>", "(IIJ[I[F)V");
	handle_cache.motion_event.constructor_single = _METHOD(handle_cache.motion_event.class, "<init>", "(IIJFFFF)V");
	handle_cache.motion_event.constructor_scroll = _METHOD(handle_cache.motion_event.class, "<init>", "(IIJFFFFFF)V");

	handle_cache.sensor_event.class = _REF((*env)->FindClass(env, "android/hardware/SensorEvent"));
	handle_cache.sensor_event.constructor = _METHOD(handle_cache.sensor_event.class, "<init>", "([FLandroid/hardware/Sensor;)V");

	handle_cache.audio_track_periodic_listener.class = _REF((*env)->FindClass(env, "android/media/AudioTrack$OnPlaybackPositionUpdateListener"));
	handle_cache.audio_track_periodic_listener.onPeriodicNotification = _METHOD(handle_cache.audio_track_periodic_listener.class, "onPeriodicNotification", "(Landroid/media/AudioTrack;)V");

	handle_cache.input_queue_callback.class = _REF((*env)->FindClass(env, "android/view/InputQueue$Callback"));
	handle_cache.input_queue_callback.onInputQueueCreated = _METHOD(handle_cache.input_queue_callback.class, "onInputQueueCreated", "(Landroid/view/InputQueue;)V");

	handle_cache.surface_view.class = _REF((*env)->FindClass(env, "android/view/SurfaceView"));
	handle_cache.surface_view.surfaceCreated = _METHOD(handle_cache.surface_view.class, "surfaceCreated", "()V");
	handle_cache.surface_view.surfaceChanged = _METHOD(handle_cache.surface_view.class, "surfaceChanged", "(III)V");

	handle_cache.view.class = _REF((*env)->FindClass(env, "android/view/View"));
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
	handle_cache.view.setLayoutParams = _METHOD(handle_cache.view.class, "setLayoutParams", "(Landroid/view/ViewGroup$LayoutParams;)V");
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
	handle_cache.view.onDraw = _METHOD(handle_cache.view.class, "onDraw", "(Landroid/graphics/Canvas;)V");
	handle_cache.view.dispatchDraw = _METHOD(handle_cache.view.class, "dispatchDraw", "(Landroid/graphics/Canvas;)V");
	handle_cache.view.draw = _METHOD(handle_cache.view.class, "draw", "(Landroid/graphics/Canvas;)V");
	handle_cache.view.onMeasure = _METHOD(handle_cache.view.class, "onMeasure", "(II)V");
	handle_cache.view.onLayout = _METHOD(handle_cache.view.class, "onLayout", "(ZIIII)V");
	handle_cache.view.getMeasuredWidth = _METHOD(handle_cache.view.class, "getMeasuredWidth", "()I");
	handle_cache.view.getMeasuredHeight = _METHOD(handle_cache.view.class, "getMeasuredHeight", "()I");
	handle_cache.view.getSuggestedMinimumWidth = _METHOD(handle_cache.view.class, "getSuggestedMinimumWidth", "()I");
	handle_cache.view.getSuggestedMinimumHeight = _METHOD(handle_cache.view.class, "getSuggestedMinimumHeight", "()I");
	handle_cache.view.setMeasuredDimension = _METHOD(handle_cache.view.class, "setMeasuredDimension", "(II)V");
	handle_cache.view.onGenericMotionEvent = _METHOD(handle_cache.view.class, "onGenericMotionEvent", "(Landroid/view/MotionEvent;)Z");
	handle_cache.view.dispatchGenericMotionEvent = _METHOD(handle_cache.view.class, "dispatchGenericMotionEvent", "(Landroid/view/MotionEvent;)Z");
	handle_cache.view.computeScroll = _METHOD(handle_cache.view.class, "computeScroll", "()V");
	handle_cache.view.getScrollX = _METHOD(handle_cache.view.class, "getScrollX", "()I");
	handle_cache.view.getScrollY = _METHOD(handle_cache.view.class, "getScrollY", "()I");
	handle_cache.view.performClick = _METHOD(handle_cache.view.class, "performClick", "()Z");
	handle_cache.view.onTouchEvent = _METHOD(handle_cache.view.class, "onTouchEvent", "(Landroid/view/MotionEvent;)Z");
	handle_cache.view.onTouchEventInternal = _METHOD(handle_cache.view.class, "onTouchEventInternal", "(Landroid/view/MotionEvent;Z)Z");
	handle_cache.view.dispatchTouchEvent = _METHOD(handle_cache.view.class, "dispatchTouchEvent", "(Landroid/view/MotionEvent;)Z");
	handle_cache.view.onInterceptTouchEvent = _METHOD(handle_cache.view.class, "onInterceptTouchEvent", "(Landroid/view/MotionEvent;)Z");
	handle_cache.view.layoutInternal = _METHOD(handle_cache.view.class, "layoutInternal", "(II)V");
	handle_cache.view.measure = _METHOD(handle_cache.view.class, "measure", "(II)V");
	handle_cache.view.performLongClick = _METHOD(handle_cache.view.class, "performLongClick", "(FF)Z");
	handle_cache.view.getId = _METHOD(handle_cache.view.class, "getId", "()I");
	handle_cache.view.getIdName = _METHOD(handle_cache.view.class, "getIdName", "()Ljava/lang/String;");
	handle_cache.view.getAllSuperClasses = _METHOD(handle_cache.view.class, "getAllSuperClasses", "()Ljava/lang/String;");
	handle_cache.view.dispatchKeyEvent = _METHOD(handle_cache.view.class, "dispatchKeyEvent", "(Landroid/view/KeyEvent;)Z");
	handle_cache.view.onKeyDown = _METHOD(handle_cache.view.class, "onKeyDown", "(ILandroid/view/KeyEvent;)Z");
	handle_cache.view.onAttachedToWindow = _METHOD(handle_cache.view.class, "onAttachedToWindow", "()V");
	handle_cache.view.onDetachedFromWindow = _METHOD(handle_cache.view.class, "onDetachedFromWindow", "()V");
	handle_cache.view.dispatchHoverEvent = _METHOD(handle_cache.view.class, "dispatchHoverEvent", "(Landroid/view/MotionEvent;)Z");

	handle_cache.view_group.class = _REF((*env)->FindClass(env, "android/view/ViewGroup"));
	handle_cache.view_group.dispatchTouchEvent = _METHOD(handle_cache.view_group.class, "dispatchTouchEvent", "(Landroid/view/MotionEvent;)Z");

	handle_cache.asset_manager.class = _REF((*env)->FindClass(env, "android/content/res/AssetManager"));
	handle_cache.asset_manager.extractFromAPK = _STATIC_METHOD(handle_cache.asset_manager.class, "extractFromAPK", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");

	handle_cache.context.class = _REF((*env)->FindClass(env, "android/content/Context"));
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
	handle_cache.context.get_package_name = _METHOD(handle_cache.context.class, "getPackageName", "()Ljava/lang/String;");
	if ((*env)->ExceptionCheck(env))
		(*env)->ExceptionDescribe(env);
	handle_cache.context.sendBroadcast = _METHOD(handle_cache.context.class, "sendBroadcast", "(Landroid/content/Intent;)V");
	handle_cache.context.startActivity = _METHOD(handle_cache.context.class, "startActivity", "(Landroid/content/Intent;)V");
	handle_cache.context.resolveActivityInternal = _STATIC_METHOD(handle_cache.context.class, "resolveActivityInternal", "(Landroid/content/Intent;)Landroid/app/Activity;");
	handle_cache.context.startService = _METHOD(handle_cache.context.class, "startService", "(Landroid/content/Intent;)Landroid/content/ComponentName;");

	handle_cache.application.class = _REF((*env)->FindClass(env, "android/app/Application"));
	handle_cache.application.get_app_icon_path = _METHOD(handle_cache.application.class, "get_app_icon_path", "()Ljava/lang/String;");
	handle_cache.application.get_app_icon_paintable = _METHOD(handle_cache.application.class, "get_app_icon_paintable", "()J");

	handle_cache.looper.class = _REF((*env)->FindClass(env, "android/os/Looper"));
	handle_cache.looper.loop = _STATIC_METHOD(handle_cache.looper.class, "loop", "()V");
	handle_cache.looper.prepareMainLooper = _STATIC_METHOD(handle_cache.looper.class, "prepareMainLooper", "()V");

	handle_cache.key_event.class = _REF((*env)->FindClass(env, "android/view/KeyEvent"));
	handle_cache.key_event.constructor = _METHOD(handle_cache.key_event.class, "<init>", "(JJIIII)V");

	handle_cache.drawable.class = _REF((*env)->FindClass(env, "android/graphics/drawable/Drawable"));
	handle_cache.drawable.draw = _METHOD(handle_cache.drawable.class, "draw", "(Landroid/graphics/Canvas;)V");
	handle_cache.drawable.setBounds = _METHOD(handle_cache.drawable.class, "setBounds", "(IIII)V");

	handle_cache.intent.class = _REF((*env)->FindClass(env, "android/content/Intent"));
	handle_cache.intent.constructor = _METHOD(handle_cache.intent.class, "<init>", "()V");
	handle_cache.intent.putExtraCharSequence = _METHOD(handle_cache.intent.class, "putExtra", "(Ljava/lang/String;Ljava/lang/CharSequence;)Landroid/content/Intent;");
	handle_cache.intent.putExtraByteArray = _METHOD(handle_cache.intent.class, "putExtra", "(Ljava/lang/String;[B)Landroid/content/Intent;");
	handle_cache.intent.putExtraInt = _METHOD(handle_cache.intent.class, "putExtra", "(Ljava/lang/String;I)Landroid/content/Intent;");
	handle_cache.intent.putExtraLong = _METHOD(handle_cache.intent.class, "putExtra", "(Ljava/lang/String;J)Landroid/content/Intent;");
	handle_cache.intent.putExtraParcelable = _METHOD(handle_cache.intent.class, "putExtra", "(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;");
	handle_cache.intent.getDataString = _METHOD(handle_cache.intent.class, "getDataString", "()Ljava/lang/String;");
	handle_cache.intent.setClassName = _METHOD(handle_cache.intent.class, "setClassName", "(Landroid/content/Context;Ljava/lang/String;)Landroid/content/Intent;");

	handle_cache.instrumentation.class = _REF((*env)->FindClass(env, "android/app/Instrumentation"));

	handle_cache.webview.class = _REF((*env)->FindClass(env, "android/webkit/WebView"));
	handle_cache.webview.internalGetAssetManager = _METHOD(handle_cache.webview.class, "internalGetAssetManager", "()Landroid/content/res/AssetManager;");
	handle_cache.webview.internalLoadChanged = _METHOD(handle_cache.webview.class, "internalLoadChanged", "(ILjava/lang/String;)V");

	handle_cache.canvas.class = _REF((*env)->FindClass(env, "android/graphics/Canvas"));
	handle_cache.canvas.drawText = _METHOD(handle_cache.canvas.class, "drawText", "(Ljava/lang/CharSequence;IIFFLandroid/graphics/Paint;)V");

	handle_cache.uri.class = _REF((*env)->FindClass(env, "android/net/Uri"));
	handle_cache.uri.parse = _STATIC_METHOD(handle_cache.uri.class, "parse", "(Ljava/lang/String;)Landroid/net/Uri;");

	handle_cache.bundle.class = _REF((*env)->FindClass(env, "android/os/Bundle"));
	handle_cache.bundle.get = _METHOD(handle_cache.bundle.class, "get", "(Ljava/lang/String;)Ljava/lang/Object;");
	handle_cache.bundle.keySet = _METHOD(handle_cache.bundle.class, "keySet", "()Ljava/util/Set;");

	handle_cache.set.class = _REF((*env)->FindClass(env, "java/util/Set"));
	handle_cache.set.toArray = _METHOD(handle_cache.set.class, "toArray", "()[Ljava/lang/Object;");

	handle_cache.parcel.class = _REF((*env)->FindClass(env, "android/os/Parcel"));
	handle_cache.parcel.writeParcelable = _METHOD(handle_cache.parcel.class, "writeParcelable", "(Landroid/os/Parcelable;I)V");
	handle_cache.parcel.readParcelable = _METHOD(handle_cache.parcel.class, "readParcelable", "(Ljava/lang/ClassLoader;)Landroid/os/Parcelable;");

	handle_cache.builder_parcel.class = _REF((*env)->FindClass(env, "android/atl/GVariantBuilderParcel"));
	handle_cache.builder_parcel.constructor = _METHOD(handle_cache.builder_parcel.class, "<init>", "(J)V");

	handle_cache.iter_parcel.class = _REF((*env)->FindClass(env, "android/atl/GVariantIterParcel"));
	handle_cache.iter_parcel.constructor = _METHOD(handle_cache.iter_parcel.class, "<init>", "(J)V");

	handle_cache.view_tree_observer.class = _REF((*env)->FindClass(env, "android/view/ViewTreeObserver"));
	handle_cache.view_tree_observer.dispatchOnGlobalLayout = _METHOD(handle_cache.view_tree_observer.class, "dispatchOnGlobalLayout", "()V");

	handle_cache.time_picker.class = _REF((*env)->FindClass(env, "android/widget/TimePicker"));
	handle_cache.time_picker.onTimeChange = _METHOD(handle_cache.time_picker.class, "onTimeChange", "()V");

	handle_cache.date_picker.class = _REF((*env)->FindClass(env, "android/widget/DatePicker"));
	handle_cache.date_picker.onDateChange = _METHOD(handle_cache.date_picker.class, "onDateChange", "()V");
}

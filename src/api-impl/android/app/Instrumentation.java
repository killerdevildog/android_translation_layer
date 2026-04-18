package android.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageParser;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.MessageQueue;
import android.util.Slog;
import android.view.KeyEvent;
import dalvik.system.DexClassLoader;
/* for hacky classloader patching */
import dalvik.system.DexFile;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class Instrumentation {
	private static final String TAG = "Instrumentation";
	public static final String REPORT_KEY_IDENTIFIER = "id";
	public static final String REPORT_KEY_STREAMRESULT = "stream";

	private static Instrumentation create(String className, Intent arguments) throws Exception {
		Thread.setUncaughtExceptionPreHandler(new ExceptionHandler());
		try {
			String target_package = null;
			for (PackageParser.Instrumentation instrumentation : Context.pkg.instrumentation) {
				if (className.equals(instrumentation.className)) {
					target_package = instrumentation.info.targetPackage;
					break;
				}
			}

			System.out.println("targetPackage: " + target_package);

			String target_path = android.os.Environment.getExternalStorageDirectory() + "/../_installed_apks_/" + target_package + ".apk";

			Context.this_application.getAssets().addAssetPath(target_path);

			patchClassLoader(DexClassLoader.getSystemClassLoader(), new File(target_path));

			Class<? extends Instrumentation> cls = Class.forName(className).asSubclass(Instrumentation.class);
			Constructor<? extends Instrumentation> constructor = cls.getConstructor();
			Instrumentation i = constructor.newInstance();
			i.onCreate(arguments.getExtras());

			return i;
		} catch (Exception e) {
			/* there is no global handler for exceptions on the main thread */
			Thread.getUncaughtExceptionPreHandler().uncaughtException(Thread.currentThread(), e);
		}
		return null; // we will never get here
	}

	public Instrumentation() {
	}

	public void start() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				//Looper.prepare();
				onStart();
			}
		});
		t.start();
	}

	public void onCreate(Bundle arguments) {
	}

	public boolean onException(Object obj, Throwable e) {
		return false;
	}

	public void onStart() {
	}

	public Context getContext() {
		return Context.this_application;
	}

	public Context getTargetContext() {
		return Context.this_application;
	}

	public void setAutomaticPerformanceSnapshots() {
	}

	public void setInTouchMode(boolean inTouch) {
		Slog.w(TAG, "FIXME: Instrumentation.setInTouchMode: " + inTouch);
	}

	public void sendKeySync(KeyEvent event) {
		validateNotAppThread();
		/*long downTime = event.getDownTime();
		long eventTime = event.getEventTime();
		int source = event.getSource();
		if (source == InputDevice.SOURCE_UNKNOWN) {
			source = InputDevice.SOURCE_KEYBOARD;
		}
		if (eventTime == 0) {
			eventTime = SystemClock.uptimeMillis();
		}
		if (downTime == 0) {
			downTime = eventTime;
		}
		KeyEvent newEvent = new KeyEvent(event);
		newEvent.setTime(downTime, eventTime);
		newEvent.setSource(source);
		newEvent.setFlags(event.getFlags() | KeyEvent.FLAG_FROM_SYSTEM);
		setDisplayIfNeeded(newEvent);
		InputManagerGlobal.getInstance().injectInputEvent(newEvent,
		InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);*/
		Slog.w(TAG, "FIXME: Instrumentation.sendKeySync: " + event);
	}

	public void sendKeyDownUpSync(int keyCode) {
		sendKeySync(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
		sendKeySync(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
	}

	static public Application newApplication(Class<?> clazz, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		return Context.this_application; // we don't (currently?) support multiple applications in a single process
	}

	public Activity newActivity(Class<?> clazz, Context context, IBinder token, Application application,
	                            Intent intent, ActivityInfo info, CharSequence title, Activity parent,
	                            String id, Object lastNonConfigurationInstance) throws InstantiationException, IllegalAccessException {
		Activity activity = (Activity)clazz.newInstance();
		activity.getWindow().set_native_window(Context.this_application.native_window);
		Slog.i(TAG, "activity.getWindow().native_window >" + activity.getWindow().native_window + "<");
		return activity;
	}

	/* Copyright (C) 2006 The Android Open Source Project */
	public void callActivityOnCreate(Activity activity, Bundle savedState) {
		//prePerformCreate(activity);
		runOnMainSync(new Runnable() {
			public void run() {
				activity.onCreate(savedState);
			}
		});
		//postPerformCreate(activity);
	}

	public Activity startActivitySync(Intent intent) {
		return startActivitySync(intent, null);
	}

	/* TODO - deduplicate with startActivityForResult? */
	public Activity startActivitySync(Intent intent, Bundle options) {
		Slog.i(TAG, "startActivitySync(" + intent + ", " + options + ") called");
		if (intent.getComponent() != null) {
			try {
				final Activity activity = Activity.internalCreateActivity(intent.getComponent().getClassName(), Context.this_application.native_window, intent);
				runOnMainSync(new Runnable() {
					@Override
					public void run() {
						Activity.nativeStartActivity(activity);
					}
				});

				return activity;
			} catch (ReflectiveOperationException e) {
				/* not sure what to do here */
			}
		} /*else if (FILE_CHOOSER_ACTIONS.contains(intent.getAction())) { // not sure what to do here either
			nativeFileChooser(FILE_CHOOSER_ACTIONS.indexOf(intent.getAction()), intent.getType(), intent.getStringExtra("android.intent.extra.TITLE"), requestCode);
		} */
		else {
			Slog.i(TAG, "startActivityForResult: intent was not handled.");
		}

		return null;
	}

	public void runOnMainSync(Runnable runner) {
		validateNotAppThread();
		SyncRunnable sr = new SyncRunnable(runner);
		new Handler(Looper.getMainLooper()).post(sr);
		sr.waitForComplete();
	}

	public void sendStatus(int resultCode, Bundle results) {
		if (results != null) {
			for (String key : sorted(results.keySet())) {
				System.out.println("INSTRUMENTATION_STATUS: " + key + "=" + results.get(key));
			}
		}
		System.out.println("INSTRUMENTATION_STATUS_CODE: " + resultCode);
	}

	public void finish(int resultCode, Bundle results) {
		boolean need_hack = false;
		if (results != null) {
			for (String key : sorted(results.keySet())) {
				System.out.println("INSTRUMENTATION_RESULT: " + key + "=" + results.get(key));
				/* HACK: no idea why this isn't recognized as an error otherwise */
				if (((String)results.get(key)).contains("Test run aborted due to unexpected exception"))
					need_hack = true;
			}
		}
		if (need_hack) {
			System.out.println("INSTRUMENTATION_STATUS: shortMsg=ugly hack: Test run aborted due to unexpected exception");
			System.out.println("INSTRUMENTATION_STATUS_CODE: -1");
		}

		System.out.println("INSTRUMENTATION_CODE: " + resultCode);
		System.exit(0);
	}

	/* Copyright (C) 2006 The Android Open Source Project */
	private static Collection<String> sorted(Collection<String> list) {
		final ArrayList<String> copy = new ArrayList<>(list);
		Collections.sort(copy);
		return copy;
	}

	/* Copyright (C) 2006 The Android Open Source Project */
	private static final class SyncRunnable implements Runnable {
		private final Runnable mTarget;
		private boolean mComplete;
		public SyncRunnable(Runnable target) {
			mTarget = target;
		}
		public void run() {
			mTarget.run();
			synchronized (this) {
				mComplete = true;
				notifyAll();
			}
		}
		public void waitForComplete() {
			synchronized (this) {
				while (!mComplete) {
					try {
						wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	/* Copyright (C) 2006 The Android Open Source Project */
	private static final class EmptyRunnable implements Runnable {
		public void run() {}
	}

	/* Copyright (C) 2006 The Android Open Source Project */
	private static final class Idler implements MessageQueue.IdleHandler {
		private final Runnable mCallback;
		private boolean mIdle;
		public Idler(Runnable callback) {
			mCallback = callback;
			mIdle = false;
		}
		public final boolean queueIdle() {
			if (mCallback != null) {
				mCallback.run();
			}
			synchronized (this) {
				mIdle = true;
				notifyAll();
			}
			return false;
		}
		public void waitForIdle() {
			synchronized (this) {
				while (!mIdle) {
					try {
						wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}

	/* Copyright (C) 2006 The Android Open Source Project */
	public void waitForIdleSync() {
		/*validateNotAppThread();
		Idler idler = new Idler(null);
		Looper.myLooper().myQueue().addIdleHandler(idler);
		new Handler(Looper.myLooper()).post(new EmptyRunnable());
		idler.waitForIdle();*/
	}

	/* Copyright (C) 2006 The Android Open Source Project */
	private final void validateNotAppThread() {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			throw new RuntimeException("This method can not be called from the main application thread");
		}
	}

	private static class ExceptionHandler implements Thread.UncaughtExceptionHandler {
		public void uncaughtException(Thread t, Throwable e) {
			//onException(null /*FIXME?*/, e);
			System.out.print("INSTRUMENTATION_RESULT: shortMsg=");
			e.printStackTrace();
			System.out.println("INSTRUMENTATION_STATUS_CODE: -1");
			System.exit(1);
		}
	}

	/* -- a hacky method to patch in a classpath entry (there should be a better way *in theory*, but other approaches didn't work) -- */
	private static Object getFieldObject(Class cls, Object obj, String field_name) {
		try {
			Field field = cls.getDeclaredField(field_name);
			field.setAccessible(true);
			Object ret = field.get(obj);
			field.setAccessible(false);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static void setFieldObject(Class cls, Object obj, String field_name, Object value) {
		try {
			Field field = cls.getDeclaredField(field_name);
			field.setAccessible(true);
			field.set(obj, value);
			field.setAccessible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Object createObject(Class cls, Class[] type_array, Object[] value_array) {
		try {
			Constructor ctor = cls.getDeclaredConstructor(type_array);
			ctor.setAccessible(true);
			Object ret = ctor.newInstance(value_array);
			ctor.setAccessible(false);
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private static void patchClassLoader(ClassLoader cl, File apk_path) throws IOException {
		// get cl.pathList
		Object pathList = getFieldObject(DexClassLoader.class.getSuperclass(), cl, "pathList");
		// get pathList.dexElements
		Object[] dexElements = (Object[])getFieldObject(pathList.getClass(), pathList, "dexElements");
		// Element type
		Class<?> Element_class = dexElements.getClass().getComponentType();
		// Create an array to replace the original array
		Object[] DexElements_new = (Object[])Array.newInstance(Element_class, dexElements.length + 1);
		// use this constructor: ElementDexFile.class(DexFile dexFile, File file)
		Class[] type_array = {DexFile.class, File.class};
		Object[] value_array = {DexFile.loadDex(apk_path.getCanonicalPath(), null, 0), apk_path};
		Object new_element = createObject(Element_class, type_array, value_array);
		Object[] new_element_wrapper_array = new Object[] {new_element};
		// Copy the original elements
		System.arraycopy(dexElements, 0, DexElements_new, 0, dexElements.length);
		// The element of the plugin is copied in
		System.arraycopy(new_element_wrapper_array, 0, DexElements_new, dexElements.length, new_element_wrapper_array.length);
		// replace
		setFieldObject(pathList.getClass(), pathList, "dexElements", DexElements_new);
	}
}

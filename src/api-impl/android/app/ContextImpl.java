package android.app;

import android.annotation.Nullable;
import android.annotation.UnsupportedAppUsage;
import android.app.SearchManager;
import android.app.job.JobScheduler;
import android.atl.ATLLoadedApp;
import android.atl.ATLLoadedAppManager;
import android.bluetooth.BluetoothManager;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.hardware.display.ColorDisplayManager;
import android.hardware.display.DisplayManager;
import android.hardware.input.InputManager;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.*;
import android.os.storage.StorageManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.WindowManagerImpl;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.CaptioningManager;
import android.view.inputmethod.InputMethodManager;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public final class ContextImpl extends Context {
	private static final int ATL_FLAG_NO_CODE = 0x00000001;
	private static final int ATL_FLAG_RESTRICTED = 0x00000002;
	private static final int ATL_FLAG_PROTECTED_STORAGE = 0x00000004;
	private final static String TAG = "ContextImpl";
	private final Resources resources;
	private final ATLLoadedApp atl_loaded_app;
	/**
	 * While not being part of the official Android API, some applications use it to reset/reload the theme in the context.
	 */
	@UnsupportedAppUsage
	private Resources.Theme mTheme;
	/**
	 * While not being part of the official Android API, some application use it to get the theme resource ID.
	 */
	@UnsupportedAppUsage
	private int mThemeResource;

	private final LayoutInflater layout_inflater = new LayoutInflater(this);
	private final JobScheduler job_scheduler = new JobScheduler(this);

	private final int atl_flags;

	public ContextImpl(Resources resources, ATLLoadedApp loadedApp, Resources.Theme theme) {
		this(resources, loadedApp, 0, 0);
		getTheme().setTo(theme);
	}

	private ContextImpl(Resources resources, ATLLoadedApp loadedApp, Resources.Theme theme, int flags) {
		this(resources, loadedApp, 0, flags);
		getTheme().setTo(theme);
	}

	public ContextImpl(Resources resources, ATLLoadedApp loadedApp, int themeResource) {
		this(resources, loadedApp, themeResource, 0);
	}

	private ContextImpl(Resources resources, ATLLoadedApp loadedApp, int themeResource, int flags) {
		this.resources = resources;
		this.atl_loaded_app = loadedApp;
		mThemeResource = themeResource;
		this.atl_flags = flags;
	}

	@Nullable
	private ATLLoadedApp atl_get_intent_target(Intent intent) {
		ATLLoadedApp targetApp;
		String targetPackage = intent.getComponent() != null ? intent.getComponent().getPackageName() : null;
		if (targetPackage != null || (targetPackage = intent.getPackage()) != null) {
			if (targetPackage.equals(this.atl_loaded_app.pkg.packageName)) {
				targetApp = this.atl_loaded_app;
			} else {
				targetApp = ATLLoadedAppManager.getAppFromPackageName(targetPackage);
			}
		} else {
			targetApp = this.atl_loaded_app;
		}
		return targetApp;
	}

	@Override
	public ApplicationInfo getApplicationInfo() {
		return atl_loaded_app.pkg.applicationInfo;
	}

	@Override
	public void setTheme(int resId) {
		mThemeResource = resId;
		if (mTheme != null) {
			mTheme.applyStyle(resId, true);
		}
	}

	@Override
	public boolean isRestricted() {
		return (this.atl_flags & ATL_FLAG_RESTRICTED) != 0;
	}

	@Override
	public boolean stopService(Intent intent) {
		ATLLoadedApp targetApp = this.atl_get_intent_target(intent);
		if (targetApp == null) {
			return false;
		}
		return targetApp.stopService(intent);
	}

	@Override
	public Resources.Theme getTheme() {
		if (mTheme == null) {
			mTheme = resources.newTheme();
			if (mThemeResource != 0) {
				mTheme.applyStyle(mThemeResource, true);
			}
		}
		return mTheme;
	}

	@Override
	public Object getSystemService(String name) {
		switch (name) {
			case "window":
				return new WindowManagerImpl();
			case "clipboard":
				return new ClipboardManager();
			case "sensor":
				return new SensorManager();
			case "connectivity":
				return new ConnectivityManager();
			case "keyguard":
				return new KeyguardManager();
			case "phone":
				return new TelephonyManager();
			case "audio":
				return new AudioManager();
			case "activity":
				return new ActivityManager();
			case "usb":
				return new UsbManager();
			case "vibrator":
				return (vibrator != null) ? vibrator : (vibrator = new Vibrator());
			case "power":
				return new PowerManager();
			case "display":
				return new DisplayManager();
			case "media_router":
				return new MediaRouter();
			case "notification":
				return new NotificationManager();
			case "alarm":
				return new AlarmManager();
			case "input":
				return new InputManager();
			case "location":
				return new LocationManager();
			case "uimode":
				return new UiModeManager();
			case "input_method":
				return new InputMethodManager();
			case "accessibility":
				return new AccessibilityManager();
			case "layout_inflater":
				return layout_inflater;
			case "wifi":
				return new WifiManager();
			case "bluetooth":
				return new BluetoothManager();
			case "jobscheduler":
				return job_scheduler;
			case "appops":
				return new AppOpsManager();
			case "user":
				return new UserManager();
			case "captioning":
				return new CaptioningManager();
			case "statusbar":
				return new StatusBarManager();
			case "camera":
				return new CameraManager();
			case "color_display":
				return new ColorDisplayManager();
			case "search":
				return new SearchManager();
			case "storage":
				return new StorageManager();
			default:
				Slog.e(TAG, "!!!!!!! getSystemService: case >" + name + "< is not implemented yet");
				return null;
		}
	}

	@Override
	public Object getSystemService(Class<?> serviceClass) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		if (serviceClass == LayoutInflater.class)
			return layout_inflater;
		if (serviceClass == JobScheduler.class)
			return job_scheduler;
		return serviceClass.getConstructors()[0].newInstance();
	}

	@Override
	public Resources getResources() {
		return resources;
	}

	@Override
	public ClassLoader getClassLoader() {
		if ((this.atl_flags & ATL_FLAG_NO_CODE) != 0) {
			return null;
		}
		return this.atl_loaded_app.class_loader;
	}

	public ComponentName startService(Intent intent) {
		// Newer applications use a Messenger instead of a BroadcastReceiver for the GCM token return Intent.
		// To support new and old apps with a common interface, we wrap the Messenger in a BroadcastReceiver
		if ("com.google.android.c2dm.intent.REGISTER".equals(intent.getAction()) && intent.getParcelableExtra("google.messenger") instanceof Messenger) {
			final Messenger messenger = (Messenger)intent.getParcelableExtra("google.messenger");
			this.registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent resultIntent) {
					try {
						messenger.send(Message.obtain(null, 0, resultIntent));
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}, new IntentFilter("com.google.android.c2dm.intent.REGISTRATION"));
		}
		ATLLoadedApp targetApp = this.atl_get_intent_target(intent);
		if (targetApp == null) {
			// External package. Try to start using DBus Action
			nativeStartExternalService(intent);
			return null;
		}
		return targetApp.startOrBindService(intent, null);
	}

	@Override
	public boolean bindService(Intent intent, ServiceConnection serviceConnection, int flags) {
		ATLLoadedApp targetApp = this.atl_get_intent_target(intent);
		if (targetApp == null) {
			return false;
		}
		return targetApp.startOrBindService(intent, null) != null;
	}

	@Override
	public void startActivity(Intent intent) {
		Slog.i(TAG, "startActivity(" + intent + ") called");
		if (intent.getAction() != null && intent.getAction().equals("android.intent.action.CHOOSER")) {
			intent = (Intent)intent.getExtras().get("android.intent.extra.INTENT");
		}

		String className = null;
		if (intent.getComponent() != null) {
			className = intent.getComponent().getClassName();
		} else {
			if (intent.getAction() != null && intent.getAction().equals("android.intent.action.SEND")) {
				Slog.i(TAG, "sharing intent via composeMail: " + intent);
				String text = intent.getStringExtra("android.intent.extra.TEXT");
				ParcelFileDescriptor fd = null;
				if (intent.hasExtra(Intent.EXTRA_STREAM)) {
					try {
						fd = getContentResolver().openFileDescriptor((Uri)intent.getParcelableExtra(Intent.EXTRA_STREAM), "r");
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
				final ParcelFileDescriptor fd_final = fd;
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						nativeShareFile(text, fd_final != null ? fd_final.getFd() : -1);
						if (fd_final != null) {
							try {
								fd_final.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				};
				if (Looper.myLooper() == Looper.getMainLooper()) {
					runnable.run();
				} else {
					new Handler(Looper.getMainLooper()).post(runnable);
				}
				return;
			} else if (intent.getData() != null) {
				Slog.i(TAG, "starting extern activity with intent: " + intent);
				if (intent.getData().getScheme().equals("content")) {
					try (ParcelFileDescriptor fd = getContentResolver().openFileDescriptor(intent.getData(), "r")) {
						if (fd != null) {
							nativeOpenFile(fd.getFd());
							return;
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				Activity.nativeOpenURI(String.valueOf(intent.getData()));
				return;
			}
			for (PackageParser.Activity activity : this.atl_loaded_app.pkg.activities) {
				for (PackageParser.IntentInfo intentInfo : activity.intents) {
					if (intentInfo.matchAction(intent.getAction())) {
						className = activity.className;
						break;
					}
				}
			}
		}
		if (className == null) {
			Slog.w(TAG, "startActivity: intent could not be handled.");
			return;
		}
		final String className_ = className;
		final Intent intent_ = intent;
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				try {
					if ((intent_.getFlags() & Intent.FLAG_ACTIVITY_CLEAR_TOP) != 0 && intent_.getComponent() != null) {
						boolean found = Activity.nativeResumeActivity(
						    ContextImpl.this.atl_loaded_app.loadClass(intent_.getComponent().getClassName())
							.asSubclass(Activity.class),
						    intent_);
						if (found)
							return;
					}
					Activity activity = Activity.internalCreateActivity(
					    className_, ContextImpl.this.atl_loaded_app.getNativeWindow(), intent_);
					Activity.nativeStartActivity(activity);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public Context createPackageContext(String packageName, int flags) {
		if (packageName.equals(atl_loaded_app.pkg.packageName)) {
			return this;
		}
		ATLLoadedApp primaryApplication = ATLLoadedApp.getPrimaryApplication();
		if (primaryApplication.pkg.packageName.equals(packageName)) {
			return primaryApplication.getApplication();
		}
		if (packageName.equals("android")) {
			// Shortcut for system application
			ATLLoadedApp system = ATLLoadedApp.getSystemApplication();
			return new ContextImpl(Resources.getSystem(), system,
			                       Resources.selectDefaultTheme(0, Build.VERSION.SDK_INT));
		}
		ATLLoadedApp loadedApplication = ATLLoadedAppManager.getAppFromPackageName(packageName);
		if (loadedApplication != null) {
			int atl_flags = 0;
			if ((flags & Context.CONTEXT_INCLUDE_CODE) == 0) {
				atl_flags |= ATL_FLAG_NO_CODE;
			}
			if ((flags & Context.CONTEXT_RESTRICTED) != 0) {
				atl_flags |= ATL_FLAG_RESTRICTED;
			}
			return new ContextImpl(loadedApplication.createDefaultResources(),
			                       loadedApplication, loadedApplication.pkg.applicationInfo.theme,
			                       atl_flags);
		}
		// Return the application context as a fallback
		Log.e(TAG, "!!!!!!! createPackageContext: case >" + packageName + "< is not implemented yet");
		return primaryApplication.getApplication();
	}

	@Override
	public Context createConfigurationContext(Configuration configuration) {
		return this.atl_loaded_app.createContext(null, configuration, getTheme());
	}

	@Override
	public Context createDisplayContext(Display display) {
		return new ContextImpl(getResources(), this.atl_loaded_app, getTheme(), this.atl_flags);
	}

	@Override
	public boolean isDeviceProtectedStorage() {
		return (this.atl_flags & ATL_FLAG_PROTECTED_STORAGE) != 0;
	}

	@Override
	public Context createDeviceProtectedStorageContext() {
		return new ContextImpl(getResources(), this.atl_loaded_app, getTheme(),
		                       this.atl_flags | ATL_FLAG_PROTECTED_STORAGE);
	}

	@Override
	public int getThemeResId() {
		return mThemeResource;
	}

	@Override
	public ATLLoadedApp get_atl_loaded_app() {
		return atl_loaded_app;
	}
}

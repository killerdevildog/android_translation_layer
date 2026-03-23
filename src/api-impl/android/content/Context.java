package android.content;

import android.R;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.Application;
import android.app.ContextImpl;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.Service;
import android.app.SharedPreferencesImpl;
import android.app.StatusBarManager;
import android.app.UiModeManager;
import android.app.job.JobScheduler;
import android.bluetooth.BluetoothManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.ShortcutManager;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.ParcelFileDescriptor;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.UserManager;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.WindowManagerImpl;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.CaptioningManager;
import android.view.inputmethod.InputMethodManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Context {
	private final static String TAG = "Context";

	public static final int MODE_PRIVATE = 0;
	public static final String ACTIVITY_SERVICE = "activity";
	public static final String AUDIO_SERVICE = "audio";
	public static final String CLIPBOARD_SERVICE = "clipboard";
	public static final String DISPLAY_SERVICE = "display";
	public static final String INPUT_METHOD_SERVICE = "input";
	public static final String LOCATION_SERVICE = "location";
	public static final String MEDIA_ROUTER_SERVICE = "media_router";
	public static final String POWER_SERVICE = "power";
	public static final String VIBRATOR_SERVICE = "vibrator";
	public static final String WINDOW_SERVICE = "window";

	public static Vibrator vibrator;

	static AssetManager assets;
	static DisplayMetrics dm;
	public static Resources r;
	static ApplicationInfo application_info;
	private static Map<Class<? extends Service>, Service> runningServices = new HashMap<>();
	public static PackageParser.Package pkg;
	public static PackageManager package_manager;

	public /*← FIXME?*/ static Application this_application;

	File data_dir = null;
	File prefs_dir = null;
	File files_dir = null;
	File obb_dir = null;
	File cache_dir = null;
	File nobackup_dir = null;

	private static Map<IntentFilter, BroadcastReceiver> receiverMap = new ConcurrentHashMap<IntentFilter, BroadcastReceiver>();

	static {
		assets = new AssetManager();
		dm = new DisplayMetrics();
		Configuration config = new Configuration();
		native_updateConfig(config);
		r = new Resources(assets, dm, config);
		application_info = new ApplicationInfo();
		try (XmlResourceParser parser = assets.openXmlResourceParser("AndroidManifest.xml")) {
			PackageParser packageParser = new PackageParser(native_get_apk_path());
			String[] parseError = new String[1];
			pkg = packageParser.parsePackage(r, parser, 0, parseError);
			if (parseError[0] != null) {
				Slog.e(TAG, parseError[0]);
				System.exit(1);
			}

			packageParser.collectCertificates(pkg, 0);
			application_info = pkg.applicationInfo;
		} catch (Exception e) {
			e.printStackTrace();
		}
		application_info.dataDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		application_info.nativeLibraryDir = (new File(Environment.getExternalStorageDirectory(), "lib")).getAbsolutePath();
		application_info.sourceDir = native_get_apk_path();
		package_manager = new PackageManager();

		Provider provider = new Provider("AndroidKeyStore", 1.0, "Android KeyStore provider") {};
		provider.put("KeyStore.AndroidKeyStore", "android.security.keystore.AndroidKeyStore");
		provider.put("KeyGenerator.AES", "android.security.keystore.KeyGenerator$AES");
		provider.put("KeyGenerator.HmacSHA512", "android.security.keystore.KeyGenerator$HmacSHA512");
		Security.addProvider(provider);

		r.applyPackageQuirks(application_info.minSdkVersion);

		for (PackageParser.Activity receiver : pkg.receivers) {
			for (PackageParser.ActivityIntentInfo intent : receiver.intents) {
				if (intent.matchAction("org.unifiedpush.android.connector.MESSAGE")) {
					nativeExportUnifiedPush(application_info.packageName);
					break;
				}
			}
		}
	}

	private static native String native_get_apk_path();
	protected static native void native_updateConfig(Configuration config);
	private static native void nativeOpenFile(int fd);
	private static native void nativeShareFile(String text, int fd);
	private static native void nativeExportUnifiedPush(String packageName);
	private static native void nativeRegisterUnifiedPush(String token, String application);
	private static native void nativeStartExternalService(Intent service);

	static Application createApplication(long native_window) throws Exception {
		Application application;

		if (pkg.applicationInfo.className != null) {
			Class<? extends Application> cls = Class.forName(pkg.applicationInfo.className).asSubclass(Application.class);
			Constructor<? extends Application> constructor = cls.getConstructor();
			application = constructor.newInstance();
		} else {
			application = new Application();
		}
		application.native_window = native_window;
		this_application = application;
		application.attachBaseContext(new ContextImpl(r, application_info, pkg.applicationInfo.theme));
		// HACK: Set WhatsApp's custom logging mechanism to verbose for easier debugging. Should be removed again once WhatsApp is fully supported
		try {
			Class.forName("com.whatsapp.util.Log").getField("level").setInt(null, 5);
		} catch (Exception e) {
		} // ignore for other apps
		return application;
	}

	public Context() {
		Slog.v(TAG, "new Context! this one is: " + this);
	}

	public int checkPermission(String permission, int pid, int uid) {
		return getPackageManager().checkPermission(permission, getPackageName());
	}

	public abstract Resources.Theme getTheme();

	public abstract ApplicationInfo getApplicationInfo();

	public Context getApplicationContext() {
		return (Context)this_application;
	}

	public ContentResolver getContentResolver() {
		return new ContentResolver();
	}

	public abstract Object getSystemService(String name);

	public abstract Object getSystemService(Class<?> serviceClass) throws InstantiationException, IllegalAccessException, InvocationTargetException;

	public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
		if (receiver == null)
			return null;
		receiverMap.put(filter, receiver);
		return new Intent();
	}

	public Looper getMainLooper() {
		return Looper.getMainLooper();
	}

	public String getPackageName() {
		return getApplicationInfo().packageName;
	}

	public String getPackageCodePath() {
		return getApplicationInfo().sourceDir;
	}

	public int getColor(int resId) {
		return r.getColor(resId);
	}

	public final String getString(int resId) {
		return r.getString(resId);
	}

	public final String getString(int resId, Object... formatArgs) {
		return r.getString(resId, formatArgs);
	}

	public PackageManager getPackageManager() {
		return package_manager;
	}

	public abstract Resources getResources();

	public AssetManager getAssets() {
		return getResources().getAssets();
	}

	private File makeFilename(File base, String name) {
		if (name.indexOf(File.separatorChar) < 0) {
			return new File(base, name);
		}
		throw new IllegalArgumentException(
		    "File " + name + " contains a path separator");
	}

	private File getDataDirFile() {
		if (data_dir == null) {
			data_dir = android.os.Environment.getExternalStorageDirectory();
		}
		return data_dir;
	}

	public File getDataDir() {
		return getDataDirFile();
	}

	public File getFilesDir() {
		if (files_dir == null) {
			files_dir = new File(getDataDirFile(), "files");
		}
		if (!files_dir.exists()) {
			if (!files_dir.mkdirs()) {
				if (files_dir.exists()) {
					// spurious failure; probably racing with another process for this app
					return files_dir;
				}
				Slog.w(TAG, "Unable to create files directory " + files_dir.getPath());
				return null;
			}
		}
		return files_dir;
	}

	public File getExternalFilesDir(String type) {
		return getFilesDir();
	}

	public File[] getExternalFilesDirs(String type) {
		return new File[] {getExternalFilesDir(type)};
	}

	public File getObbDir() {
		if (obb_dir == null) {
			obb_dir = new File(getDataDirFile(), "Android/obb/" + getPackageName());
		}
		if (!obb_dir.exists()) {
			if (!obb_dir.mkdirs()) {
				if (obb_dir.exists()) {
					// spurious failure; probably racing with another process for this app
					return obb_dir;
				}
				Slog.w(TAG, "Unable to create obb directory >" + obb_dir.getPath() + "<");
				return null;
			}
		}
		return obb_dir;
	}

	public File[] getObbDirs() {
		return new File[] {getObbDir()};
	}

	public File getCacheDir() {
		if (cache_dir == null) {
			cache_dir = new File("/tmp/atl_cache/" + getPackageName());
		}
		if (!cache_dir.exists()) {
			if (!cache_dir.mkdirs()) {
				if (cache_dir.exists()) {
					// spurious failure; probably racing with another process for this app
					return cache_dir;
				}
				Slog.w(TAG, "Unable to create cache directory >" + cache_dir.getPath() + "<");
				return null;
			}
		}
		return cache_dir;
	}

	public File getExternalCacheDir() {
		return getCacheDir();
	}

	public File[] getExternalCacheDirs() {
		return new File[] {getCacheDir()};
	}

	public File getNoBackupFilesDir() {
		if (nobackup_dir == null) {
			nobackup_dir = new File(getDataDirFile(), "no_backup/" + getPackageName());
		}
		if (!nobackup_dir.exists()) {
			if (!nobackup_dir.mkdirs()) {
				if (nobackup_dir.exists()) {
					// spurious failure; probably racing with another process for this app
					return nobackup_dir;
				}
				Slog.w(TAG, "Unable to create no_backup directory >" + nobackup_dir.getPath() + "<");
				return null;
			}
		}
		return nobackup_dir;
	}

	private File getPreferencesDir() {
		if (prefs_dir == null) {
			prefs_dir = new File(getDataDirFile(), "shared_prefs");
		}
		return prefs_dir;
	}

	public File[] getExternalMediaDirs() {
		return getExternalFilesDirs("media");
	}

	public File getDir(String name, int mode) {
		File dir = new File(getFilesDir(), name);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				if (dir.exists()) {
					// spurious failure; probably racing with another process for this app
					return dir;
				}
				Slog.w(TAG, "Unable to create directory >" + dir.getPath() + "<");
				return null;
			}
		}
		return dir;
	}

	public File getFileStreamPath(String name) {
		return makeFilename(getFilesDir(), name);
	}

	public File getSharedPrefsFile(String name) {
		return makeFilename(getPreferencesDir(), name + ".xml");
	}

	private static Map<String, SharedPreferences> sharedPrefs = new HashMap<String, SharedPreferences>();

	public SharedPreferences getSharedPreferences(String name, int mode) {
		Slog.v(TAG, "\n\n...> getSharedPreferences(" + name + ")\n\n");
		if (sharedPrefs.containsKey(name)) {
			return sharedPrefs.get(name);
		} else {
			File prefsFile = getSharedPrefsFile(name);
			SharedPreferences prefs = new SharedPreferencesImpl(prefsFile, mode);
			sharedPrefs.put(name, prefs);
			return prefs;
		}
	}

	public ClassLoader getClassLoader() {
		// not perfect, but it's what we use for now as well, and it works
		return ClassLoader.getSystemClassLoader();
	}

	public ComponentName startService(Intent intent) {
		ComponentName component = intent.getComponent();
		if (component == null) {
			int priority = Integer.MIN_VALUE;
			for (PackageParser.Service service : pkg.services) {
				for (PackageParser.IntentInfo intentInfo : service.intents) {
					if (intentInfo.matchAction(intent.getAction()) && intentInfo.priority > priority) {
						component = new ComponentName(pkg.packageName, service.className);
						priority = intentInfo.priority;
						break;
					}
				}
			}
		}
		// Newer applications use a Messenger instead of a BroadcastReceiver for the GCM token return Intent.
		// To support new and old apps with a common interface, we wrap the Messenger in a BroadcastReceiver
		if ("com.google.android.c2dm.intent.REGISTER".equals(intent.getAction()) && intent.getParcelableExtra("google.messenger") instanceof Messenger) {
			final Messenger messenger = (Messenger)intent.getParcelableExtra("google.messenger");
			receiverMap.put(new IntentFilter("com.google.android.c2dm.intent.REGISTRATION"), new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent resultIntent) {
					try {
						messenger.send(Message.obtain(null, 0, resultIntent));
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			});
		}
		if (intent.getPackage() != null && !intent.getPackage().equals(getPackageName())) {
			// External package. Try to start using DBus Action
			nativeStartExternalService(intent);
			return null;
		}
		if (component == null) {
			Slog.w(TAG, "startService: no matching service found for intent: " + intent);
			return null;
		}
		final String className = component.getClassName();

		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				try {
					Class<? extends Service> cls = Class.forName(className).asSubclass(Service.class);
					if (!runningServices.containsKey(cls)) {
						Service service = cls.getConstructor().newInstance();
						service.attachBaseContext(new ContextImpl(getResources(), getApplicationInfo(), getTheme()));
						service.onCreate();
						runningServices.put(cls, service);
					}

					runningServices.get(cls).onStartCommand(intent, 0, 0);
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
		});

		return component;
	}

	// TODO: do these both work? make them look more alike
	public FileInputStream openFileInput(String name) throws FileNotFoundException {
		Slog.v(TAG, "openFileInput called for: '" + name + "'");
		File file = new File(getFilesDir(), name);

		return new FileInputStream(file);
	}

	public FileOutputStream openFileOutput(String name, int mode) throws java.io.FileNotFoundException {
		Slog.v(TAG, "openFileOutput called for: '" + name + "'");
		return new FileOutputStream(android.os.Environment.getExternalStorageDirectory().getPath() + "/files/" + name);
	}

	public int checkCallingOrSelfPermission(String permission) {
		return getPackageManager().checkPermission(permission, getPackageName());
	}

	public int checkSelfPermission(String permission) {
		return checkCallingOrSelfPermission(permission);
	}

	public void registerComponentCallbacks(ComponentCallbacks callbacks) {}

	public void unregisterComponentCallbacks(ComponentCallbacks callbacks) {}

	public boolean bindService(final Intent intent, final ServiceConnection serviceConnection, int flags) {
		if (intent.getComponent() == null) {
			for (PackageParser.Service s : pkg.services) {
				for (PackageParser.IntentInfo ii : s.intents) {
					if (ii.matchAction(intent.getAction())) {
						intent.setComponent(new ComponentName(pkg.packageName, s.className));
						break;
					}
				}
			}
		}
		if (intent.getComponent() == null) {
			Slog.w(TAG, "Context.bindService(" + intent + ", " + serviceConnection + ", " + flags + "): intent.getComponent() is null");
			return false;
		}

		new Handler(Looper.getMainLooper()).post(new Runnable() { // run this asynchron so the caller can finish its setup before onServiceConnected is called
			@Override
			public void run() {
				try {
					Class<? extends Service> cls = Class.forName(intent.getComponent().getClassName()).asSubclass(Service.class);
					if (!runningServices.containsKey(cls)) {
						Service service = cls.getConstructor().newInstance();
						service.attachBaseContext(new ContextImpl(getResources(), getApplicationInfo(), getTheme()));
						service.onCreate();
						runningServices.put(cls, service);
					}
					serviceConnection.onServiceConnected(intent.getComponent(), runningServices.get(cls).onBind(intent));
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
		});
		return true;
	}

	/* For use from native code */
	static Activity resolveActivityInternal(Intent intent) throws ReflectiveOperationException {
		String className = null;
		if (intent.getComponent() != null) {
			className = intent.getComponent().getClassName();
		} else {
			int best_score = -5;
			for (PackageParser.Activity activity : pkg.activities) {
				for (PackageParser.IntentInfo intentInfo : activity.intents) {
					int score = intentInfo.match(intent.getAction(), intent.getType(), intent.getScheme(), intent.getData(), intent.getCategories(), "Context");
					if (score > best_score && score > 0) {
						className = activity.className;
						best_score = score;
					}
				}
			}
		}
		if (className != null) {
			return Activity.internalCreateActivity(className, this_application.native_window, intent);
		} else {
			return null;
		}
	}

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
			for (PackageParser.Activity activity : pkg.activities) {
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
						boolean found = Activity.nativeResumeActivity(Class.forName(intent_.getComponent().getClassName()).asSubclass(Activity.class), intent_);
						if (found)
							return;
					}
					Activity activity = Activity.internalCreateActivity(className_, this_application.native_window, intent_);
					Activity.nativeStartActivity(activity);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void startActivity(Intent intent, Bundle options) {
		startActivity(intent);
	}

	public final TypedArray obtainStyledAttributes(AttributeSet set, int[] attrs) {
		return getTheme().obtainStyledAttributes(set, attrs, 0, 0);
	}
	public final TypedArray obtainStyledAttributes(AttributeSet set, int[] attrs, int defStyleAttr, int defStyleRes) {
		return getTheme().obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes);
	}
	public final TypedArray obtainStyledAttributes(int resid, int[] attrs) {
		return getTheme().obtainStyledAttributes(resid, attrs);
	}
	public final TypedArray obtainStyledAttributes(int[] attrs) {
		return getTheme().obtainStyledAttributes(attrs);
	}

	public abstract void setTheme(int resId);

	public final CharSequence getText(int resId) {
		return getResources().getText(resId);
	}

	public final ColorStateList getColorStateList(int id) {
		return getResources().getColorStateList(id);
	}

	public final Drawable getDrawable(int resId) {
		return getResources().getDrawable(resId, getTheme());
	}

	public boolean isRestricted() { return false; }

	public File getDatabasePath(String dbName) {
		File databaseDir = new File(getDataDirFile(), "databases");
		if (!databaseDir.exists())
			databaseDir.mkdirs();
		return new File(databaseDir, dbName);
	}

	public void sendBroadcast(Intent intent) {
		if ("org.unifiedpush.android.distributor.REGISTER".equals(intent.getAction())) {
			nativeRegisterUnifiedPush(intent.getStringExtra("token"), intent.getStringExtra("application"));
		}
		for (IntentFilter filter : receiverMap.keySet()) {
			if (filter.matchAction(intent.getAction())) {
				receiverMap.get(filter).onReceive(this, intent);
			}
		}
		for (PackageParser.Activity receiver : pkg.receivers) {
			for (PackageParser.IntentInfo intentInfo : receiver.intents) {
				if (intentInfo.matchAction(intent.getAction())) {
					try {
						Class<? extends BroadcastReceiver> cls = Class.forName(receiver.className).asSubclass(BroadcastReceiver.class);
						BroadcastReceiver receiverInstance = cls.newInstance();
						receiverInstance.onReceive(this, intent);
					} catch (ReflectiveOperationException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public boolean stopService(Intent intent) throws ClassNotFoundException {
		Class<? extends Service> cls = Class.forName(intent.getComponent().getClassName()).asSubclass(Service.class);
		Service service = runningServices.remove(cls);
		if (service != null) {
			service.onDestroy();
			return true;
		}
		return false;
	}

	public void unbindService(ServiceConnection serviceConnection) {}

	public void unregisterReceiver(BroadcastReceiver receiver) {
		while (receiverMap.values().remove(receiver))
			;
	}

	public abstract Context createPackageContext(String packageName, int flags);

	public void grantUriPermission(String dummy, Uri dummy2, int dummy3) {
		System.out.println("grantUriPermission(" + dummy + ", " + dummy2 + ", " + dummy3 + ") called");
	}

	public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
		return openOrCreateDatabase(name, mode, factory, null);
	}

	public SQLiteDatabase openOrCreateDatabase(String filename, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
		int flags = SQLiteDatabase.CREATE_IF_NECESSARY;
		if ((mode & (1 << 3) /*MODE_ENABLE_WRITE_AHEAD_LOGGING*/) != 0) {
			flags |= SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING;
		}
		SQLiteDatabase db = SQLiteDatabase.openDatabase(filename, factory, flags, errorHandler);
		return db;
	}

	public boolean deleteDatabase(String name) {
		File dbFile = getDatabasePath(name);
		return dbFile.delete();
	}

	public abstract Context createConfigurationContext(Configuration configuration);

	public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver, Handler handler, int flags, String extra, Bundle options) {
		System.out.println("sendOrderedBroadcast(" + intent + ", " + receiverPermission + ", " + resultReceiver + ", " + handler + ", " + flags + ", " + extra + ", " + options + ") called");
	}

	public abstract Context createDisplayContext(Display display);

	public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
		return registerReceiver(receiver, filter);
	}

	public String[] fileList() {
		return new String[0];
	}

	public void revokeUriPermission(Uri uri, int mode) {
		System.out.println("revokeUriPermission(" + uri + ", " + mode + ") called");
	}

	public String getAttributionTag() {
		return null;
	}
	public boolean isDeviceProtectedStorage() {
		return false;
	}

	public Drawable getWallpaper() {
		return null;
	}

	public String[] databaseList() {
		File databaseDir = new File(getDataDirFile(), "databases");
		if (databaseDir.exists()) {
			return databaseDir.list();
		} else {
			return new String[0];
		}
	}

	public abstract Context createDeviceProtectedStorageContext();

	public boolean moveSharedPreferencesFrom(Context sourceContext, String name) {
		File sourceFile = sourceContext.getSharedPrefsFile(name);
		if (!sourceFile.exists()) {
			return true;
		}
		deleteSharedPreferences(name);
		return sourceFile.renameTo(getSharedPrefsFile(name));
	}

	public boolean deleteSharedPreferences(String name) {
		getSharedPrefsFile(name).delete();
		sharedPrefs.remove(name);
		return true;
	}

	public String getPackageResourcePath() {
		return native_get_apk_path();
	}

	public abstract int getThemeResId();
}

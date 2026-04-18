package android.os;

import java.util.Properties;

public class SystemProperties {
	private static final Properties properties = new Properties();

	static {
		String SDK_INT_str = System.getProperty("Build.VERSION.SDK_INT");
		if (SDK_INT_str == null)
			SDK_INT_str = "" + Build.VERSION_CODES.GINGERBREAD;
		properties.put("ro.build.version.sdk", SDK_INT_str);
		properties.put("ro.product.brand", "google");
		properties.put("ro.build.tags", "release-keys");
		properties.put("ro.build.type", "user");
		// TODO how to actually get the system's supported abis?
		if (System.getProperty("os.arch").equals("x86_64")) {
			properties.put("ro.product.cpu.abi", "x86_64");
			properties.put("ro.product.cpu.abi2", "x86");
			properties.put("ro.product.cpu.abilist", "x86_64,x86");
		}
	}

	public static String get(String prop) {
		android.util.Log.i("SystemProperties", "Grabbing String prop " + prop);
		return properties.getProperty(prop);
	}

	public static String get(String prop, String def) {
		android.util.Log.i("SystemProperties", "Grabbing String prop " + prop + ", default " + def);
		return properties.getProperty(prop, def);
	}

	public boolean getBoolean(String prop, boolean def) {
		android.util.Log.i("SystemProperties", "Grabbing boolean prop " + prop + ", default " + def);
		String val = properties.getProperty(prop);
		return val == null ? def : Boolean.parseBoolean(val);
	}

	public static int getInt(String prop, int def) {
		android.util.Log.i("SystemProperties", "Grabbing int prop " + prop + ", default " + def);
		String val = properties.getProperty(prop);
		return val == null ? def : Integer.parseInt(val);
	}

	public static long getLong(String prop, long def) {
		android.util.Log.i("SystemProperties", "Grabbing long prop " + prop + ", default " + def);
		String val = properties.getProperty(prop);
		return val == null ? def : Long.parseLong(val);
	}
}

package android.atl;

import java.io.File;

public final class ATLPaths {
	public static final File app_data_dir_base;
	public static final File installed_apks_dir;
	public static final File api_impl_jar;
	public static final File gstub_jar;

	static {
		// The main executable set api-impl.jar as the first element of "java.class.path"
		String cp = System.getProperty("java.class.path");
		api_impl_jar = new File(cp.substring(0, cp.indexOf(':'))).getAbsoluteFile();
		// gstub.jar is always next to api-impl.jar both when installed and when running from builddir
		gstub_jar = new File(ATLPaths.api_impl_jar.getParentFile(), "gstub.jar");
		// Mirror how main.c calculates app_data_dir_base
		String ANDROID_APP_DATA_DIR = System.getenv("ANDROID_APP_DATA_DIR");
		if (ANDROID_APP_DATA_DIR != null && !ANDROID_APP_DATA_DIR.isEmpty()) {
			app_data_dir_base = new File(ANDROID_APP_DATA_DIR).getAbsoluteFile();
		} else {
			// Mirror g_get_user_data_dir(); behavior
			String user_data_dir = System.getenv("XDG_DATA_HOME");
			if (user_data_dir == null || user_data_dir.isEmpty()) {
				user_data_dir = System.getenv("HOME") + "/.local/share";
			}
			app_data_dir_base = new File(user_data_dir + "/android_translation_layer").getAbsoluteFile();
		}
		installed_apks_dir = new File(app_data_dir_base, "_installed_apks_");
	}
}

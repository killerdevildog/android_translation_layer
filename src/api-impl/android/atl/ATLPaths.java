package android.atl;

import java.io.File;

public final class ATLPaths {
	public static final File app_data_dir_base;
	public static final File installed_apks_dir;

	static {
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

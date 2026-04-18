package android.webkit;

import android.content.Context;

public class CookieSyncManager {

	public static CookieSyncManager createInstance(Context context) {
		return new CookieSyncManager();
	}

	public void sync() {}
}

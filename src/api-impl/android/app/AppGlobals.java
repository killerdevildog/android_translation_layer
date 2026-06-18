package android.app;

import android.atl.ATLLoadedApp;

public class AppGlobals {

	public static Application getInitialApplication() {
		return ATLLoadedApp.getPrimaryApplication().getApplication();
	}
}

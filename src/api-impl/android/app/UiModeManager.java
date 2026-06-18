package android.app;

import android.content.Context;
import android.content.res.Configuration;

public class UiModeManager {

	public int getCurrentModeType() {
		return Context.sys_config.uiMode & Configuration.UI_MODE_TYPE_MASK;
	}

	public int getNightMode() {
		return Context.sys_config.uiMode & Configuration.UI_MODE_NIGHT_MASK;
	}
}

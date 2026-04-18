package android.app;

import android.content.ComponentName;
import android.content.Context;

public class ApplicationErrorReport {
	public static ComponentName getErrorReportReceiver(Context context, String packageName, int appFlags) {
		return new ComponentName(context, packageName);
	}
}

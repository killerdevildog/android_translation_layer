package android.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import java.util.List;

public class ActivityThread {
	public static ActivityThread currentActivityThread() {
		return new ActivityThread();
	}
	public static String currentPackageName() {
		return Context.this_application.getPackageName();
	}
	public static String currentProcessName() {
		return Application.getProcessName();
	}
	public static Application currentApplication() {
		return Context.this_application;
	}
	public Application getApplication() {
		return Context.this_application;
	}

	public Configuration getConfiguration() {
		return Context.this_application.getResources().getConfiguration();
	}
}

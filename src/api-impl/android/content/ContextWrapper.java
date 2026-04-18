package android.content;

import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.Display;
import java.lang.reflect.InvocationTargetException;

public class ContextWrapper extends Context {
	private Context baseContext;

	public ContextWrapper(Context baseContext) {
		this.baseContext = baseContext;
	}

	public Context getBaseContext() {
		return baseContext;
	}

	protected void attachBaseContext(Context baseContext) {
		this.baseContext = baseContext;
	}

	@Override
	public Resources.Theme getTheme() {
		return this.baseContext.getTheme();
	}

	@Override
	public ApplicationInfo getApplicationInfo() {
		return this.baseContext.getApplicationInfo();
	}

	@Override
	public Object getSystemService(String name) {
		return this.baseContext.getSystemService(name);
	}

	@Override
	public Object getSystemService(Class<?> serviceClass) throws InstantiationException, IllegalAccessException, InvocationTargetException {
		return this.baseContext.getSystemService(serviceClass);
	}

	@Override
	public Resources getResources() {
		return this.baseContext.getResources();
	}

	@Override
	public void setTheme(int resId) {
		this.baseContext.setTheme(resId);
	}

	@Override
	public Context createPackageContext(String packageName, int flags) {
		return this.baseContext.createPackageContext(packageName, flags);
	}

	@Override
	public Context createConfigurationContext(Configuration configuration) {
		return this.baseContext.createConfigurationContext(configuration);
	}

	@Override
	public Context createDisplayContext(Display display) {
		return this.baseContext.createDisplayContext(display);
	}

	@Override
	public Context createDeviceProtectedStorageContext() {
		return this.baseContext.createDeviceProtectedStorageContext();
	}

	@Override
	public int getThemeResId() {
		return this.baseContext.getThemeResId();
	}
}

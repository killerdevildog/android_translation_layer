package android.appwidget;

import android.content.ComponentName;
import android.content.Context;

public class AppWidgetManager {

	public static AppWidgetManager getInstance(Context context) {
		return new AppWidgetManager();
	}

	public int[] getAppWidgetIds(ComponentName provider) {
		return new int[0];
	}

	public void notifyAppWidgetViewDataChanged(int appWidgetId, int viewId) {}

	public void notifyAppWidgetViewDataChanged(int[] appWidgetIds, int viewId) {}
}

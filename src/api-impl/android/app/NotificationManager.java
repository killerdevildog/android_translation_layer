package android.app;

import android.app.Notification.MediaStyle;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import java.util.Collections;
import java.util.List;

public class NotificationManager {

	private static int mpris_notification_id = -1;

	public void cancelAll() {}

	public void notify(String tag, int id, Notification notification) {
		if (notification.style instanceof MediaStyle) { // MPRIS content is handled by MediaSession implementation
			if (mpris_notification_id == -1) {
				nativeShowMPRIS(Context.this_application.getPackageName(), Context.this_application.get_app_label());
				mpris_notification_id = id;
			}
			return;
		}

		System.out.println("notify(" + tag + ", " + id + ", " + notification + ") called");
		long builder = nativeInitBuilder();
		for (Notification.Action action : notification.actions) {
			int intentType = -1;
			Intent intent = null;
			if (action.intent != null) {
				intentType = action.intent.type;
				intent = action.intent.intent;
			}
			nativeAddAction(builder, action.title, intentType, intent);
		}
		int intentType = -1;
		Intent intent = null;
		if (notification.intent != null) {
			intentType = notification.intent.type;
			intent = notification.intent.intent;
		}
		nativeShowNotification(builder, id, notification.title, notification.text, notification.iconPath, notification.ongoing, intentType, intent);
	}

	public void notify(int id, Notification notification) {
		notify(null, id, notification);
	}

	public void cancel(String tag, final int id) {
		// remove_notification doesn't work reliably when sent directly after add_notification in GNOME session.
		// So we give some extra delay here.
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
			@Override
			public void run() {
				if (mpris_notification_id == id) {
					mpris_notification_id = -1;
					nativeCancelMPRIS();
				} else {
					nativeCancel(id);
				}
			}
		}, 100);
	}

	public void cancel(int id) {
		cancel(null, id);
	}

	public void createNotificationChannel(NotificationChannel channel) {}

	protected native long nativeInitBuilder();
	protected native void nativeAddAction(long builder, String title, int intentType, Intent intent);
	protected native void nativeShowNotification(long builder, int id, String title, String text, String iconPath, boolean ongoing, int intentType, Intent intent);
	protected native void nativeShowMPRIS(String packageName, String identiy);
	protected native void nativeCancel(int id);
	protected native void nativeCancelMPRIS();

	public void createNotificationChannelGroup(NotificationChannelGroup v) {}

	public List<NotificationChannel> getNotificationChannels() {
		return Collections.emptyList();
	}

	public List<NotificationChannelGroup> getNotificationChannelGroups() {
		return Collections.emptyList();
	}

	public boolean areNotificationsEnabled() {
		return true;
	}

	public void deleteNotificationChannel(String channelId) {}

	public void createNotificationChannelGroups(List<NotificationChannelGroup> groups) {}

	public void createNotificationChannels(List<NotificationChannel> channels) {}

	public StatusBarNotification[] getActiveNotifications() {
		return new StatusBarNotification[0];
	}

	public NotificationChannel getNotificationChannel(String channel) {
		return null;
	}
}

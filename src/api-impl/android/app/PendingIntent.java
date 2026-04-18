package android.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.GestureDetector;

public class PendingIntent implements Parcelable {

	private int requestCode;
	Intent intent;
	int type; // 0: activity, 1: service, 2: broadcast

	private PendingIntent(int requestCode, Intent intent, int type) {
		this.requestCode = requestCode;
		this.intent = intent;
		this.type = type;
	}
	public static PendingIntent getBroadcast(Context context, int requestCode, Intent intent, int flags) {
		return new PendingIntent(requestCode, intent, 2);
	}

	public IntentSender getIntentSender() {
		return null;
	}

	public void send(Context context, int code, Intent intent) {}

	public void send() {
		Context context = Context.this_application;
		if (type == 0) { // type Activity
			context.startActivity(intent);
		} else if (type == 1) { // type Service
			context.startService(intent);
		} else if (type == 2) { // type Broadcast
			context.sendBroadcast(intent);
		}
	}

	public static PendingIntent getActivity(Context context, int requestCode, Intent intent, int flags) {
		return new PendingIntent(requestCode, intent, 0);
	}

	public static PendingIntent getService(Context context, int requestCode, Intent intent, int flags) {
		return new PendingIntent(requestCode, intent, 1);
	}

	public static PendingIntent getActivities(Context context, int requestCode, Intent[] intents, int flags, Bundle options) {
		return new PendingIntent(requestCode, intents[0], 0);
	}

	public String toString() {
		return "PendingIntent [requestCode=" + requestCode + ", intent=" + intent + ", type="
		     + new String[] {"activity", "service", "broadcast"}[type] + "]";
	}

	public void cancel() {}

	public class CanceledException extends Exception {
	}

	public String getCreatorPackage() {
		return Context.this_application.getPackageName();
	}

	public int getCreatorUid() {
		return Context.this_application.getApplicationInfo().uid;
	}

	public static PendingIntent getForegroundService(Context context, int requestCode, Intent intent, int flags) {
		return getService(context, requestCode, intent, flags);
	}
}

package android.widget;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.view.View;

public class Toast {

	public Toast(Context context) {
		/* TODO */
	}

	private String text;

	public static Toast makeText(Context context, int resId, int duration) {
		return makeText(context, context.getString(resId), duration);
	}

	public static Toast makeText(Context context, CharSequence text, int duration) {
		Toast toast = new Toast(context);
		toast.text = String.valueOf(text);
		return toast;
	}

	public void show() {
		System.out.println("showing toast: " + text);
		Notification notification = new Notification.Builder(Context.this_application).setContentText(text).build();
		NotificationManager manager = (NotificationManager)Context.this_application.getSystemService("notification");
		int id = hashCode();
		manager.notify(id, notification);
		new Handler().postDelayed(new Runnable() {
			public void run() {
				manager.cancel(id);
			}
		}, 2000);
	}

	public void setView(View view) {}
}

package android.app;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.Slog;

public class AlarmManager {
	private static final String TAG = "AlarmManager";

	public void cancel(PendingIntent operation) {
		Slog.i(TAG, "cancel(" + operation + ") called");
	}

	public void cancel(OnAlarmListener listener) {
		Slog.i(TAG, "cancel(" + listener + ") called");
	}

	public void setInexactRepeating(int type, long triggerTime, long interval, PendingIntent operation) {
		Slog.i(TAG, "setInexactRepeating(" + type + ", " + triggerTime + ", " + interval + ", " + operation + ") called");
		long delay = triggerTime - ((type == 2 || type == 3) ? SystemClock.elapsedRealtime() : System.currentTimeMillis());
		Slog.i(TAG, "setInexactRepeating() delay: " + DateUtils.formatElapsedTime(delay) + " interval: " + DateUtils.formatElapsedTime(interval));
		Handler handler = new Handler(Looper.getMainLooper());
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Slog.i(TAG, "delivering repeating alarm: " + operation);
				operation.send();
				handler.postDelayed(this, interval);
			}
		}, delay);
	}

	public void setExact(int type, long triggerTime, PendingIntent operation) {
		Slog.i(TAG, "setExact(" + type + ", " + triggerTime + ", " + operation + ") called");
		long delay = triggerTime - ((type == 2 || type == 3) ? SystemClock.elapsedRealtime() : System.currentTimeMillis());
		Slog.i(TAG, "setExact() delay: " + DateUtils.formatElapsedTime(delay));
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
			@Override
			public void run() {
				Slog.i(TAG, "delivering alarm: " + operation);
				operation.send();
			}
		}, delay);
	}

	public void set(int type, long triggerTime, PendingIntent operation) {
		setExact(type, triggerTime, operation);
	}

	public void setExactAndAllowWhileIdle(int type, long triggerAtMillis, PendingIntent operation) {
		setExact(type, triggerAtMillis, operation);
	}

	public void setAlarmClock(AlarmClockInfo info, PendingIntent operation) {
	}

	public void setAndAllowWhileIdle(int type, long triggerAtMillis, PendingIntent operation) {
		setExact(type, triggerAtMillis, operation);
	}

	public boolean canScheduleExactAlarms() {
		return true;
	}

	public static class AlarmClockInfo implements Parcelable {
		private long mTriggerTime;
		private PendingIntent mPendingIntent;
		public AlarmClockInfo(long triggerTime, PendingIntent showIntent) {
			mTriggerTime = triggerTime;
			mPendingIntent = showIntent;
		}

		public long getTriggerTime() {
			return mTriggerTime;
		}

		public PendingIntent getShowIntent() {
			return mPendingIntent;
		}
	}

	public static interface OnAlarmListener {
		void onAlarm();
	}
}

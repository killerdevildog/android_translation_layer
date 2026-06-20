package android.app;

import android.content.Intent;
import android.os.IBinder;

public abstract class IntentService extends Service {
	public IntentService(String name) {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		this.onHandleIntent(intent);
		return 0;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	protected abstract void onHandleIntent(Intent intent);
}

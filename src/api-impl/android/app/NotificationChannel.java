package android.app;

import android.media.AudioAttributes;
import android.net.Uri;

public class NotificationChannel {

	public NotificationChannel(String id, CharSequence name, int importance) {}

	public void setLockscreenVisibility(int a) {}
	public void setShowBadge(boolean a) {}
	public void setGroup(String grp) {}
	public void enableLights(boolean en) {}
	public void setLightColor(int color) {}
	public void setVibrationPattern(long[] pattern) {}
	public void enableVibration(boolean en) {}
	public void setSound(Uri uri, AudioAttributes attrs) {}
	public boolean shouldShowLights() { return false; }
	public int getLightColor() { return 0; }
	public boolean shouldVibrate() { return false; }
	public Uri getSound() { return null; }
	public void setDescription(String description) {}
	public void setBypassDnd(boolean bypassDnd) {}
}

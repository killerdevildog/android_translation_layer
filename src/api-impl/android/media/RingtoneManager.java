package android.media;

import android.content.Context;
import android.net.Uri;

public class RingtoneManager {
	public static Ringtone getRingtone(Context context, Uri uri) {
		return new Ringtone();
	}

	public static Uri getDefaultUri(int type) {
		return new Uri.Builder().build();
	}
}

package android.text.format;

import android.content.Context;
import java.util.Date;

public class DateUtils {

	public static CharSequence getRelativeTimeSpanString(Context context, long millis, boolean withPreposition) {
		return new Date(millis).toString();
	}

	public static boolean isToday(long millis) {
		Date d1 = new Date(millis);
		Date d2 = new Date();

		return d1.getYear() == d2.getYear() && d1.getMonth() == d2.getMonth() && d1.getDate() == d2.getDate();
	}

	public static String formatElapsedTime(long elapsedSeconds) {
		final long days = elapsedSeconds / (24 * 60 * 60L);
		final long hours = elapsedSeconds / (60 * 60L) % 24L;
		final long minutes = elapsedSeconds / (60L) % 60L;
		final long seconds = elapsedSeconds % 60L;

		if (elapsedSeconds < 0) {
			return "0:00";
		} else if (days > 0) {
			return String.format("%d:%02d:%02d:%02d", days, hours, minutes, seconds);
		} else if (hours > 0) {
			return String.format("%d:%02d:%02d", hours, minutes, seconds);
		} else {
			return String.format("%d:%02d", minutes, seconds);
		}
	}

	public static String formatDateTime(Context context, long millis, int flags) {
		return new Date(millis).toString();
	}

	public static String formatDateRange(Context context, long fromMillis, long toMillis, int flags) {
		if (fromMillis == toMillis)
			return formatDateTime(context, fromMillis, flags);
		else
			return formatDateTime(context, fromMillis, flags) + " - " + formatDateTime(context, toMillis, flags);
	}

	public static CharSequence getRelativeTimeSpanString(long time, long now, long minResolutionMillis, int flags) {
		return new Date(time).toString();
	}
}

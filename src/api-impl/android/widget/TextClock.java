package android.widget;

import android.content.Context;
import android.util.AttributeSet;

public class TextClock extends TextView {
	private CharSequence mFormat12Hour = "HH:mm";
	private CharSequence mFormat24Hour = "HH:mm";
	private String mTimeZone = "";

	public TextClock(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
	}

	public TextClock(Context context) {
		super(context);
	}

	public TextClock(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr, 0);
	}

	public TextClock(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public CharSequence getFormat12Hour() {
		return mFormat12Hour;
	}

	public CharSequence getFormat24Hour() {
		return mFormat24Hour;
	}

	public String getTimeZone() {
		return mTimeZone;
	}

	public boolean is24HourModeEnabled() {
		return true;
	}

	public void onVisibilityAggregated(boolean isVisible) {}

	public void refreshTime() {}

	public void setFormat12Hour(CharSequence format) {
		mFormat12Hour = format;
	}

	public void setFormat24Hour(CharSequence format) {
		mFormat24Hour = format;
	}

	public void setTimeZone(String timeZone) {
		mTimeZone = timeZone;
	}
}

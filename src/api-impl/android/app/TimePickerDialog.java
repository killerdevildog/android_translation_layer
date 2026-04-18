package android.app;

import android.content.Context;
import android.util.TypedValue;
import android.widget.TimePicker;
import com.android.internal.R;

public class TimePickerDialog extends AlertDialog implements TimePicker.OnTimeChangedListener {

	private TimePicker time_picker;
	private OnTimeSetListener time_set_listener = null;

	public TimePickerDialog(Context context, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
		this(context, 0, listener, hourOfDay, minute, is24HourView);
	}

	public TimePickerDialog(Context context, int themeResId, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView) {
		super(context, resolveDialogTheme(context, themeResId));

		time_set_listener = listener;
		setTitle("Time Picker");
		time_picker = new TimePicker(context);
		time_picker.setHour(hourOfDay);
		time_picker.setMinute(minute);
		time_picker.setOnTimeChangedListener(this);
		setView(time_picker);
	}

	public void updateTime(int hourOfDay, int minuteOfHour) {
		time_picker.setHour(hourOfDay);
		time_picker.setMinute(minuteOfHour);
	}

	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		if (time_set_listener != null) {
			time_set_listener.onTimeSet(time_picker, time_picker.getHour(), time_picker.getMinute());
		}
	}

	public static interface OnTimeSetListener {
		void onTimeSet(TimePicker view, int hourOfDay, int minute);
	}

	/* function ported from AOSP - Copyright 2007, The Android Open Source Project */
	static int resolveDialogTheme(Context context, int resId) {
		if (resId == 0) {
			final TypedValue outValue = new TypedValue();
			context.getTheme().resolveAttribute(R.attr.timePickerDialogTheme, outValue, true);
			return outValue.resourceId;
		} else {
			return resId;
		}
	}
}

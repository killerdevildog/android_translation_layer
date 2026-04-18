package android.widget;

import android.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class TimePicker extends FrameLayout {

	// pointers
	private long hour_spin_widget;
	private long minute_spin_widget;
	private long btn_widget;

	private OnTimeChangedListener on_time_changed_listener = null;

	public TimePicker(Context context) {
		this(context, null);
	}

	public TimePicker(Context context, AttributeSet attributeSet) {
		this(context, attributeSet, 0);
	}

	public TimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public TimePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public Integer getCurrentHour() {
		return getHour();
	}

	public Integer getCurrentMinute() {
		return getMinute();
	}

	public int getHour() {
		return nativeGetSpinBtnValue(hour_spin_widget);
	}

	public int getMinute() {
		return nativeGetSpinBtnValue(minute_spin_widget);
	}

	public boolean is24HourView() {
		return true;
	}

	public boolean isEnabled() {
		return true;
	}

	public void setEnabled(boolean enabled) {
	}

	public void setCurrentHour(Integer currentHour) {
		setHour(currentHour);
	}

	public void setCurrentMinute(Integer currentMinute) {
		setMinute(currentMinute);
	}

	public void setHour(int hour) {
		nativeSetSpinBtnValue(hour_spin_widget, hour);
	}

	public void setMinute(int minute) {
		nativeSetSpinBtnValue(minute_spin_widget, minute);
	}

	public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
		nativeSetOnTimeChangedListener(btn_widget);
		on_time_changed_listener = onTimeChangedListener;
	}

	private void onTimeChange() {
		if (on_time_changed_listener != null) {
			on_time_changed_listener.onTimeChanged(this, getCurrentHour(), getCurrentMinute());
		}
	}

	protected native void nativeSetOnTimeChangedListener(long widget);

	@Override
	protected native long native_constructor(Context context, AttributeSet attrs);

	private native void nativeSetSpinBtnValue(long widget, int value);
	private native int nativeGetSpinBtnValue(long widget);

	public static interface OnTimeChangedListener {
		void onTimeChanged(TimePicker view, int hourOfDay, int minute);
	}
}

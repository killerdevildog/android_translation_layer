package android.widget;

import android.content.Context;
import android.util.AttributeSet;

public class DatePicker extends FrameLayout {

	private OnDateChangedListener on_date_changed_listener = null;

	public interface OnDateChangedListener {
		void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth);
	}

	public DatePicker(Context context) {
		this(context, null);
	}

	public DatePicker(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DatePicker(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public DatePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public void setMinDate(long minDate) {}

	public void setMaxDate(long maxDate) {}

	public void init(int year, int monthOfYear, int dayOfMonth, OnDateChangedListener listener) {
		updateDate(year, monthOfYear, dayOfMonth);
		setOnDateChangedListener(listener);
	}

	public void setFirstDayOfWeek(int dayOfWeek) {}

	public void setOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
		nativeSetOnDateChangedListener(widget);
		on_date_changed_listener = onDateChangedListener;
	}

	private void onDateChange() {
		if (on_date_changed_listener != null) {
			on_date_changed_listener.onDateChanged(this, getYear(), getMonth(), getDayOfMonth());
		}
	}

	public int getYear() {
		return nativeGetYear(widget);
	}

	public int getMonth() {
		return nativeGetMonth(widget);
	}

	public int getDayOfMonth() {
		return nativeGetDay(widget);
	}

	public void updateDate(int year, int month, int dayOfMonth) {
		nativeUpdateDate(widget, year, month, dayOfMonth);
	}

	@Override
	protected native long native_constructor(Context context, AttributeSet attrs);
	protected native void nativeSetOnDateChangedListener(long widget);

	protected native int nativeGetYear(long widget);
	protected native int nativeGetMonth(long widget);
	protected native int nativeGetDay(long widget);

	protected native void nativeUpdateDate(long widget, int year, int month, int dayOfMonth);
}

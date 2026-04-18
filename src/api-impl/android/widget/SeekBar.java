package android.widget;

import android.content.Context;
import android.util.AttributeSet;

public class SeekBar extends AbsSeekBar {

	public SeekBar(Context context) {
		super(context);
	}

	public SeekBar(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	@Override
	protected native long native_constructor(Context context, AttributeSet attrs);
	@Override
	protected native void native_setProgress(long widget, float fraction);
	protected native int native_getProgress(long widget);
	protected native void native_setMax(long widget, int max);
	@Override
	public void native_setIndeterminate(boolean indeterminate) {}

	@Override
	public void setMax(int max) {
		this.max = max;
		native_setMax(widget, max);
	}
	@Override
	public void setProgress(int progress) {
		this.progress = progress;
		native_setProgress(widget, progress);
	}

	public native void setOnSeekBarChangeListener(final OnSeekBarChangeListener l);

	@Override
	public int getProgress() {
		return native_getProgress(widget);
	}

	public static interface OnSeekBarChangeListener {
		public void onStartTrackingTouch(SeekBar seekBar);

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser);

		public void onStopTrackingTouch(SeekBar seekBar);
	}
}

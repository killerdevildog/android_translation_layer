package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class ProgressBar extends View {

	protected int max = 100;
	protected int progress = 0;
	private boolean indeterminate = false;
	private Drawable indeterminateDrawable;
	private Drawable progressDrawable = new Drawable();

	public ProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		haveCustomMeasure = false;
		TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.ProgressBar, defStyle, 0);
		setIndeterminateDrawable(a.getDrawable(com.android.internal.R.styleable.ProgressBar_indeterminateDrawable));
		Drawable progressDrawable = a.getDrawable(com.android.internal.R.styleable.ProgressBar_progressDrawable);
		if (progressDrawable != null)
			setProgressDrawable(progressDrawable);
		indeterminate = a.getBoolean(com.android.internal.R.styleable.ProgressBar_indeterminate, false)
		             || a.getBoolean(com.android.internal.R.styleable.ProgressBar_indeterminateOnly, false);
		native_setIndeterminate(indeterminate);
		/* FIXME hack: NewPipe expects this to not be null, but for some reason it is */
		if (indeterminateDrawable == null)
			indeterminateDrawable = new Drawable() {
				@Override
				public void draw(Canvas canvas) {
					// TODO Auto-generated method stub
					throw new UnsupportedOperationException("Unimplemented method 'draw'");
				}
			};
		a.recycle();
	}

	public ProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ProgressBar(Context context) {
		this(context, null, 0);
	}

	@Override
	protected native long native_constructor(Context context, AttributeSet attrs);
	protected native void native_setProgress(long widget, float fraction);

	public boolean isIndeterminate() {
		return indeterminate;
	}

	public void setIndeterminate(boolean indeterminate) {
		native_setIndeterminate(indeterminate);
		indeterminate = true;
	}

	public Drawable getProgressDrawable() {
		return progressDrawable;
	}

	public Drawable getIndeterminateDrawable() {
		return indeterminateDrawable;
	}

	public void setMax(int max) {
		this.max = max;
		native_setProgress(widget, progress / (float)max);
	}

	public int getMax() {
		return max;
	}

	public void setProgress(int progress) {
		if (progress > max)
			progress = max;
		else if (progress < 0)
			progress = 0;
		this.progress = progress;
		native_setProgress(widget, progress / (float)max);
	}

	public void setProgress(int progress, boolean animate) {
		setProgress(progress);
	}

	public void setSecondaryProgress(int secondaryProgress) {}

	public int getProgress() {
		return progress;
	}

	public void setIndeterminateDrawable(Drawable indeterminateDrawable) {
		this.indeterminateDrawable = indeterminateDrawable;
	}

	public void setProgressDrawable(Drawable progressDrawable) {
		this.progressDrawable = progressDrawable;
	}

	public native void native_setIndeterminate(boolean indeterminate);

	public void incrementProgressBy(int diff) {
		setProgress(progress + diff);
	}

	public int getSecondaryProgress() {
		return 0;
	}

	public void setIndeterminateTintList(ColorStateList tint) {}

	public void setIndeterminateTintMode(PorterDuff.Mode tintMode) {}

	public void setProgressTintList(ColorStateList tint) {}
}

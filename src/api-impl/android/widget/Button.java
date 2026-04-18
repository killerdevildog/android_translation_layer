package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class Button extends TextView {

	private Drawable compoundDrawableLeft;

	public Button(Context context) {
		this(context, null);
	}

	public Button(Context context, AttributeSet attributeSet) {
		this(context, attributeSet, 0);
	}

	public Button(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public Button(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);

		TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.TextView, 0, 0);
		if (a.hasValue(com.android.internal.R.styleable.TextView_text)) {
			setText(a.getText(com.android.internal.R.styleable.TextView_text));
		}

		if (getBackground() != null) {
			native_addClass(widget, "ATL-no-border");
		}
		a.recycle();
	}

	@Override
	protected native long native_constructor(Context context, AttributeSet attrs);
	public native void native_setText(long widget, String text);
	@Override
	protected native void nativeSetOnClickListener(long widget);
	protected native void native_setCompoundDrawables(long widget, long paintable);

	@Override
	public void setText(CharSequence text) {
		native_setText(widget, String.valueOf(text));
	}

	@Override
	public native CharSequence getText();

	@Override
	public void setTextSize(float size) {}

	@Override
	public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {
		compoundDrawableLeft = left;
		native_setCompoundDrawables(widget, left != null ? left.paintable : 0);
	}
}

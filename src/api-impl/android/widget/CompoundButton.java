package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public abstract class CompoundButton extends Button implements Checkable {
	Drawable button_drawable = null;
	public Drawable mButtonDrawable; // directly accessed by androidx

	public CompoundButton(Context context) {
		this(context, null);
	}

	public CompoundButton(Context context, AttributeSet attributeSet) {
		this(context, attributeSet, 0);
	}

	public CompoundButton(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}

	public CompoundButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	protected native long native_constructor(Context context, AttributeSet attrs);
	@Override
	public native void native_setText(long widget, String text);

	public static interface OnCheckedChangeListener {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked);
	}

	public native void setOnCheckedChangeListener(OnCheckedChangeListener listener);

	@Override
	public native void setChecked(boolean checked);

	@Override
	public native boolean isChecked();

	public void toggle() {
		setChecked(!isChecked());
	}

	// following methods are overridden to prevent calling incompatible methods from superclasses
	@Override
	public void setOnClickListener(final OnClickListener l) {}
	@Override
	public void setTextColor(int color) {}
	@Override
	public void setTextSize(float size) {}
	@Override
	public CharSequence getText() {
		return "FIXME CompoundButton.getText()";
	}

	public void setButtonTintList(ColorStateList list) {
	}

	public void setButtonDrawable(Drawable drawable) {
		button_drawable = drawable;
	}

	public Drawable getButtonDrawable() {
		return button_drawable;
	}

	public ColorStateList getButtonTintList() {
		return null;
	}

	@Override
	public void setCompoundDrawables(Drawable left, Drawable top, Drawable right, Drawable bottom) {}

	public PorterDuff.Mode getButtonTintMode() {
		return null;
	}
}

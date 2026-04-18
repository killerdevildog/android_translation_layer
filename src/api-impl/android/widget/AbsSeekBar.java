package android.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public abstract class AbsSeekBar extends ProgressBar {

	public AbsSeekBar(Context context) {
		super(context);
	}

	public AbsSeekBar(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	public Drawable getThumb() {
		return new Drawable();
	}

	public void setKeyProgressIncrement(int keyProgressIncrement) {}

	public int getKeyProgressIncrement() { return 0; }

	public int getThumbOffset() { return 0; }

	public void setThumbTintList(ColorStateList tint) {}
}

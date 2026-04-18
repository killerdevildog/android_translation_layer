package android.widget;

import android.content.Context;
import android.util.AttributeSet;

public class Switch extends CompoundButton {

	public Switch(Context context) {
		super(context);
	}

	public Switch(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);
	}

	public void setTextOn(CharSequence text) {}
	public void setTextOff(CharSequence text) {}
}

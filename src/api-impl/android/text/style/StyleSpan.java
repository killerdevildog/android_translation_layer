package android.text.style;

import android.text.ParcelableSpan;

public class StyleSpan extends MetricAffectingSpan implements ParcelableSpan {

	private int style;

	public StyleSpan(int style) {
		this.style = style;
	}

	public int getStyle() {
		return style;
	}
}

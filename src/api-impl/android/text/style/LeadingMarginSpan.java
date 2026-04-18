package android.text.style;

import android.text.ParcelableSpan;

public interface LeadingMarginSpan {

	public static class Standard implements LeadingMarginSpan, ParcelableSpan {

		public Standard(int indent) {}

		public Standard(int first_indent, int rest_indent) {}
	}
}

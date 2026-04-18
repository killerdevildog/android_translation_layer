package android.text;

public class StaticLayout extends Layout {

	public StaticLayout(CharSequence source, int bufstart, int bufend,
	                    TextPaint paint, int outerwidth,
	                    Alignment align, TextDirectionHeuristic textDir,
	                    float spacingmult, float spacingadd,
	                    boolean includepad,
	                    TextUtils.TruncateAt ellipsize, int ellipsizedWidth, int maxLines) {
		super(source.toString(), paint, outerwidth, align, spacingmult, spacingadd);
	}

	public StaticLayout(CharSequence source, TextPaint paint, int outerwidth, Alignment align, float spacingmult, float spacingadd, boolean includepad) {
		super(source.toString(), paint, outerwidth, align, spacingmult, spacingadd);
	}

	public StaticLayout(CharSequence source, int start, int end, TextPaint paint, int outerwidth, Alignment align, float spacingmult, float spacingadd, boolean includepad, TextUtils.TruncateAt ellipsize, int ellipsizedWidth) {
		super(source.toString(), paint, outerwidth, align, spacingmult, spacingadd);
		if (ellipsize != null)
			native_set_ellipsize(layout, ellipsize == null ? 0 : ellipsize.ordinal() + 1, ellipsizedWidth);
	}

	public StaticLayout(CharSequence source, int start, int end, TextPaint paint, int outerwidth, Alignment align, float spacingmult, float spacingadd, boolean includepad) {
		super(source.toString(), paint, outerwidth, align, spacingmult, spacingadd);
	}

	public static class Builder {
		private StaticLayout layout;

		public static Builder obtain(CharSequence source, int bufstart, int bufend, TextPaint paint, int outerwidth) {
			Builder builder = new Builder();
			builder.layout = new StaticLayout(source, bufstart, bufend, paint, outerwidth, null, null, 0, 0, false, null, 0, 0);
			return builder;
		}

		public Builder setTextDirection(TextDirectionHeuristic textDir) { return this; }

		public Builder setAlignment(Alignment align) { return this; }

		public Builder setMaxLines(int maxLines) { return this; }

		public Builder setEllipsize(TextUtils.TruncateAt ellipsize) { return this; }

		public Builder setEllipsizedWidth(int ellipsizedWidth) { return this; }

		public Builder setLineSpacing(float add, float mult) { return this; }

		public Builder setIncludePad(boolean includepad) { return this; }

		public Builder setBreakStrategy(int strategy) { return this; }

		public Builder setHyphenationFrequency(int hyphenationFrequency) { return this; }

		public Builder setIndents(int[] indents, int[] widths) { return this; }

		public Builder setJustificationMode(int mode) { return this; }

		public StaticLayout build() { return layout; }
	}
}

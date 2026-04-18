package android.text;

public class BoringLayout extends Layout {

	public BoringLayout(CharSequence source, TextPaint paint, int outerwidth, Layout.Alignment align, float spacingMult, float spacingAdd, BoringLayout.Metrics metrics, boolean includePad) {
		super(source, paint, outerwidth, align, spacingMult, spacingAdd);
	}

	public static class Metrics {};

	public static Metrics isBoring(CharSequence source, TextPaint paint, Metrics metrics) {
		return metrics;
	}
}

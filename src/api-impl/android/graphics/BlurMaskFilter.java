package android.graphics;

public class BlurMaskFilter extends MaskFilter {

	public static enum Blur {
		NORMAL,
	}
	;

	public BlurMaskFilter(float radius, Blur blur) {}
}

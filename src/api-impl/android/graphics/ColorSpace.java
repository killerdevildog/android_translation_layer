package android.graphics;

public class ColorSpace {

	public static enum Named {
		SRGB,
	}

	public static ColorSpace get(Named named) {
		return new ColorSpace();
	}
}

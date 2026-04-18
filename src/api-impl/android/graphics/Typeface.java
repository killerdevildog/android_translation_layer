package android.graphics;

import android.content.res.AssetManager;

public class Typeface {

	/**
	 * The default NORMAL typeface object
	 */
	public static final Typeface DEFAULT = create((String)null, 0);
	/**
	 * The default BOLD typeface object. Note: this may be not actually be
	 * bold, depending on what fonts are installed. Call getStyle() to know
	 * for sure.
	 */
	public static final Typeface DEFAULT_BOLD = create((String)null, 0);
	/**
	 * The NORMAL style of the default sans serif typeface.
	 */
	public static final Typeface SANS_SERIF = create((String)null, 0);
	/**
	 * The NORMAL style of the default serif typeface.
	 */
	public static final Typeface SERIF = create((String)null, 0);
	/**
	 * The NORMAL style of the default monospace typeface.
	 */
	public static final Typeface MONOSPACE = create((String)null, 0);

	// Style
	public static final int NORMAL = 0;
	public static final int BOLD = 1;
	public static final int ITALIC = 2;
	public static final int BOLD_ITALIC = 3;

	public long native_instance = 0; // directly accessed by androidx

	public static Typeface createFromAsset(AssetManager mgr, String path) {
		return DEFAULT;
	}

	public static Typeface create(String family_name, int style) {
		Typeface ret = new Typeface();
		return ret;
	}

	public static Typeface create(Typeface typeface, int style) {
		return typeface != null ? typeface : DEFAULT;
	}

	public static Typeface createFromFile(String path) {
		return DEFAULT;
	}

	public int getStyle() {
		return 0;
	}

	public static Typeface defaultFromStyle(int style) {
		return create((String)null, style);
	}

	public static Typeface createFromFamiliesWithDefault(FontFamily[] families) {
		return DEFAULT;
	}

	public static Typeface createFromFamiliesWithDefault(FontFamily[] families, int dummy1, int dummy2) {
		return createFromFamiliesWithDefault(families);
	}

	public static class Builder {
		public Builder(AssetManager mgr, String path) {}

		public Builder setFontVariationSettings(String settings) {
			return this;
		}

		public Typeface build() {
			return DEFAULT;
		}
	}
}

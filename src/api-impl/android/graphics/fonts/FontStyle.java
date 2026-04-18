package android.graphics.fonts;

public class FontStyle {
	public static final int FONT_SLANT_ITALIC = 1;
	public static final int FONT_SLANT_UPRIGHT = 0;
	public static final int FONT_WEIGHT_BLACK = 900;
	public static final int FONT_WEIGHT_BOLD = 700;
	public static final int FONT_WEIGHT_EXTRA_BOLD = 800;
	public static final int FONT_WEIGHT_EXTRA_LIGHT = 200;
	public static final int FONT_WEIGHT_LIGHT = 300;
	public static final int FONT_WEIGHT_MAX = 1000;
	public static final int FONT_WEIGHT_MEDIUM = 500;
	public static final int FONT_WEIGHT_MIN = 1;
	public static final int FONT_WEIGHT_NORMAL = 400;
	public static final int FONT_WEIGHT_SEMI_BOLD = 600;
	public static final int FONT_WEIGHT_THIN = 100;
	public static final int FONT_WEIGHT_UNSPECIFIED = -1;

	private int slant;
	private int weight;
	public FontStyle() {
		this(FONT_WEIGHT_MEDIUM, FONT_SLANT_UPRIGHT);
	}

	public FontStyle(int weight, int slant) {
		this.weight = weight;
		this.slant = slant;
	}

	public int getSlant() {
		return this.slant;
	}

	public int getWeight() {
		return this.weight;
	}
}

package android.graphics;

import java.util.Locale;

public class Paint {
	public static final int ANTI_ALIAS_FLAG = (1 << 0);
	public static final int FILTER_BITMAP_FLAG = (1 << 1);
	public static final int DITHER_FLAG = (1 << 2);
	public static final int UNDERLINE_TEXT_FLAG = (1 << 3);
	public static final int STRIKE_THRU_TEXT_FLAG = (1 << 4);
	public static final int FAKE_BOLD_TEXT_FLAG = (1 << 5);
	public static final int LINEAR_TEXT_FLAG = (1 << 6);
	public static final int SUBPIXEL_TEXT_FLAG = (1 << 7);
	public static final int DEV_KERN_TEXT_FLAG = (1 << 8);
	public static final int LCD_RENDER_TEXT_FLAG = (1 << 9);
	public static final int EMBEDDED_BITMAP_TEXT_FLAG = (1 << 10);
	public static final int AUTO_HINTING_TEXT_FLAG = (1 << 11);
	public static final int VERTICAL_TEXT_FLAG = (1 << 12);

	public long paint; // native paint
	private Xfermode xfermode;
	private Shader shader;
	private Align align = Align.CENTER;
	private ColorFilter color_filter;

	public Paint() {
		paint = native_create();
	}

	public Paint(int flags) {
		this();
		setFlags(flags);
	}

	public Paint(Paint paint) {
		this.paint = native_clone(paint.paint);
	}

	public void setColor(int color) {
		native_set_color(paint, color);
	}

	public void setARGB(int a, int r, int g, int b) {
		setColor(Color.argb(a, r, g, b));
	}

	public int getColor() {
		return native_get_color(paint);
	}

	public void setAlpha(int a) {
		native_set_alpha(paint, a);
	}

	public int getAlpha() {
		return native_get_alpha(paint);
	}

	public void setAntiAlias(boolean aa) {
	}

	public boolean setFontVariationSettings(String fvs) {
		return true;
	}

	public void setStrokeWidth(float width) {
		native_set_stroke_width(paint, width);
	}
	public void setTextSize(float size) {
		native_set_text_size(paint, size);
	}

	public Typeface setTypeface(Typeface typeface) {
		return typeface;
	}
	public void getTextBounds(String text, int start, int end, Rect bounds) {
		if (end > text.length())
			end = text.length();
		native_get_text_bounds(paint, text.substring(start, end), bounds);
	}
	public void getTextBounds(char[] text, int index, int count, Rect bounds) {
		native_get_text_bounds(paint, new String(text, index, count), bounds);
	}
	public int getTextWidths(String text, int start, int end, float[] widths) {
		Rect bounds = new Rect();
		native_get_text_bounds(paint, text.substring(start, end), bounds);
		return bounds.width();
	}
	public void setFilterBitmap(boolean filter) {}

	public void setFlags(int flags) {
		if ((flags & ANTI_ALIAS_FLAG) != 0)
			setAntiAlias(true);
	}

	public void setStyle(Style style) {
		native_set_style(paint, style.ordinal());
	}

	public float ascent() {
		return -getTextSize();
	}

	public float measureText(char[] text, int index, int count) { return 10; }
	public float measureText(String text, int start, int end) {
		return (end - start) * getTextSize() * .6f;
	}
	public float measureText(String text) {
		return measureText(text, 0, text.length());
	}
	public float measureText(CharSequence text, int start, int end) {
		return measureText(text.toString(), start, end);
	}

	public ColorFilter setColorFilter(ColorFilter colorFilter) {
		if (colorFilter instanceof PorterDuffColorFilter) {
			PorterDuffColorFilter porterDuff = (PorterDuffColorFilter)colorFilter;
			native_set_color_filter(paint, porterDuff.getMode().ordinal(), porterDuff.getColor());
		} else {
			native_set_color_filter(paint, -1, 0);
		}
		color_filter = colorFilter;
		return colorFilter;
	}

	public Object getColorFilter() {
		return color_filter;
	}

	public Shader setShader(Shader shader) {
		this.shader = shader;
		return shader;
	}

	public enum Style {
		/**
		 * Geometry and text drawn with this style will be filled, ignoring all
		 * stroke-related settings in the paint.
		 */
		FILL,
		/**
		 * Geometry and text drawn with this style will be stroked, respecting
		 * the stroke-related fields on the paint.
		 */
		STROKE,
		/**
		 * Geometry and text drawn with this style will be both filled and
		 * stroked at the same time, respecting the stroke-related fields on
		 * the paint. This mode can give unexpected results if the geometry
		 * is oriented counter-clockwise. This restriction does not apply to
		 * either FILL or STROKE.
		 */
		FILL_AND_STROKE;

		public static final Style values[] = Style.values();
	}

	public static class FontMetrics {
		/**
		 * The maximum distance above the baseline for the tallest glyph in
		 * the font at a given text size.
		 */
		public float top;
		/**
		 * The recommended distance above the baseline for singled spaced text.
		 */
		public float ascent;
		/**
		 * The recommended distance below the baseline for singled spaced text.
		 */
		public float descent;
		/**
		 * The maximum distance below the baseline for the lowest glyph in
		 * the font at a given text size.
		 */
		public float bottom;
		/**
		 * The recommended additional space to add between lines of text.
		 */
		public float leading;
	}

	public static class FontMetricsInt {
		public int top;
		public int ascent;
		public int descent;
		public int bottom;
		public int leading;

		@Override
		public String toString() {
			return "FontMetricsInt: top=" + top + " ascent=" + ascent + " descent=" + descent + " bottom=" + bottom + " leading=" + leading;
		}
	}

	public /*native*/ int getFlags() { return 0; }

	public /*native*/ int getHinting() { return 0; }
	public /*native*/ void setHinting(int mode) {}

	public /*native*/ void setDither(boolean dither) {}
	public boolean isDither() {
		return false;
	}

	public /*native*/ void setLinearText(boolean linearText) {}
	public /*native*/ void setSubpixelText(boolean subpixelText) {}
	public /*native*/ void setUnderlineText(boolean underlineText) {}
	public /*native*/ void setStrikeThruText(boolean strikeThruText) {}
	public /*native*/ void setFakeBoldText(boolean fakeBoldText) {}

	public float getStrokeWidth() {
		return native_get_stroke_width(paint);
	}

	public /*native*/ float getStrokeMiter() { return 0; }
	public /*native*/ void setStrokeMiter(float miter) {}
	public /*native*/ float getTextSize() {
		return native_get_text_size(paint);
	}

	public /*native*/ float getTextScaleX() { return 0; }
	public /*native*/ void setTextScaleX(float scaleX) {}
	public /*native*/ float getTextSkewX() { return 0; }
	public /*native*/ void setTextSkewX(float skewX) {}

	public /*native*/ float descent() { return 0; }
	public /*native*/ float getFontMetrics(FontMetrics metrics) { return 0; }
	public /*native*/ int getFontMetricsInt(FontMetricsInt fmi) { return 0; }

	public void setShadowLayer(float radius, float dx, float dy, int color) {}

	public Xfermode setXfermode(Xfermode xfermode) {
		this.xfermode = xfermode;
		return xfermode;
	}

	public Xfermode getXfermode() {
		return xfermode;
	}

	public void setBlendMode(BlendMode blendmode) {
		setXfermode(blendmode == null ? null : blendmode.getXfermode());
	}

	public BlendMode getBlendMode() {
		if (this.xfermode instanceof PorterDuffXfermode) {
			return BlendMode.fromValue(((PorterDuffXfermode)this.xfermode).porterDuffMode);
		}
		return null;
	}

	public void setLetterSpacing(float spacing) {}

	public enum Cap {
		/**
		 * The stroke ends with the path, and does not project beyond it.
		 */
		BUTT,
		/**
		 * The stroke projects out as a semicircle, with the center at the
		 * end of the path.
		 */
		ROUND,
		/**
		 * The stroke projects out as a square, with the center at the end
		 * of the path.
		 */
		SQUARE;

		public static final Cap values[] = Cap.values();
	}

	public enum Join {
		/**
		 * The outer edges of a join meet at a sharp angle
		 */
		MITER,
		/**
		 * The outer edges of a join meet in a circular arc.
		 */
		ROUND,
		/**
		 * The outer edges of a join meet with a straight line
		 */
		BEVEL;

		public static final Join values[] = Join.values();
	}

	public enum Align {
		LEFT,
		CENTER,
		RIGHT,
	}

	public void setStrokeCap(Cap cap) {
		native_set_stroke_cap(paint, cap.ordinal());
	}

	public void setStrokeJoin(Join join) {
		native_set_stroke_join(paint, join.ordinal());
	}

	public Typeface getTypeface() {
		return new Typeface();
	}

	public void setTextAlign(Align align) {
		this.align = align;
		native_set_text_align(paint, align.ordinal());
	}

	public Shader getShader() {
		return shader;
	}

	public PathEffect getPathEffect() {
		return new PathEffect();
	}

	public PathEffect setPathEffect(PathEffect effect) {
		return effect;
	}

	public int breakText(char[] text, int index, int count, float maxWidth, float[] measuredWidth) { return 10; }
	public int breakText(String text, boolean measureForwards, float maxWidth, float[] measuredWidth) { return 10; }
	public int breakText(CharSequence text, int start, int end, boolean measureForwards, float maxWidth, float[] measuredWidth) { return 10; }

	public void clearShadowLayer() {}

	public boolean hasShadowLayer() {
		return false;
	}

	public FontMetrics getFontMetrics() {
		return new FontMetrics();
	}

	public void setFontMetricsInt(FontMetricsInt fmi) {}

	public FontMetricsInt getFontMetricsInt() {
		return new FontMetricsInt();
	}

	public void set(Paint paint) {
		native_recycle(this.paint);
		this.paint = native_clone(paint.paint);
	}

	public boolean isFilterBitmap() { return false; }

	public Cap getStrokeCap() {
		return Cap.values[native_get_stroke_cap(paint)];
	}

	public Join getStrokeJoin() {
		return Join.values[native_get_stroke_join(paint)];
	}

	public Locale getTextLocale() { return Locale.getDefault(); }

	public float getLetterSpacing() { return 1.0f; }

	public Style getStyle() {
		return Style.values[native_get_style(paint)];
	}

	public Align getTextAlign() {
		return align;
	}

	public boolean hasGlyph(String text) { return false; }

	public MaskFilter setMaskFilter(MaskFilter filter) { return filter; }

	public boolean isFakeBoldText() {
		return false;
	}

	private static native long native_create();
	private static native long native_clone(long paint);
	private static native void native_recycle(long paint);
	private static native void native_set_color(long paint, int color);
	private static native int native_get_color(long paint);
	private static native void native_set_alpha(long paint, int alpha);
	private static native int native_get_alpha(long paint);
	private static native void native_set_style(long paint, int style);
	private static native int native_get_style(long paint);
	private static native void native_set_stroke_width(long paint, float width);
	private static native float native_get_stroke_width(long paint);
	private static native void native_set_stroke_cap(long paint, int cap);
	private static native int native_get_stroke_cap(long paint);
	private static native void native_set_stroke_join(long paint, int join);
	private static native int native_get_stroke_join(long paint);
	private static native void native_set_text_size(long paint, float size);
	private static native float native_get_text_size(long paint);
	private static native void native_set_color_filter(long paint, int mode, int color);
	private static native void native_get_text_bounds(long paint, String text, Rect bounds);
	private static native void native_set_text_align(long paint, int align);
}

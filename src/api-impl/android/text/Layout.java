package android.text;

import android.atl.GskCanvas;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

public class Layout {

	public enum Alignment {
		ALIGN_NORMAL,
		ALIGN_OPPOSITE,
		ALIGN_CENTER,
		ALIGN_LEFT,
		ALIGN_RIGHT,
	}

	public class Directions {}

	long layout; // native PangoLayout
	private CharSequence text;
	private TextPaint paint;
	private float spacing_mult;
	private float spacing_add;
	private Alignment align;

	protected Layout(CharSequence text, TextPaint paint, int width, Layout.Alignment align, float spacingMult, float spacingAdd) {
		this.text = text;
		this.paint = paint;
		this.spacing_mult = spacingMult;
		this.spacing_add = spacingAdd;
		this.align = align;
		layout = native_constructor(text != null ? text.toString() : "", paint.paint, width);
	}

	public int getLineCount() {
		return native_get_line_count(layout);
	}

	public float getLineWidth(int line) {
		if (line < 0 || line >= getLineCount())
			throw new ArrayIndexOutOfBoundsException();
		return native_get_line_width(layout, line);
	}

	public TextPaint getPaint() {
		return paint;
	}

	public int getEllipsisCount(int line) {
		if (line < 0 || line >= getLineCount())
			throw new ArrayIndexOutOfBoundsException();
		return native_get_ellipsis_count(layout, line);
	}

	public CharSequence getText() { return text; }

	public int getWidth() {
		return native_get_width(layout);
	}

	public int getHeight() {
		return native_get_height(layout);
	}

	public void draw(Canvas canvas) {
		if (canvas instanceof GskCanvas)
			native_draw(layout, ((GskCanvas)canvas).snapshot, paint.paint);
		else
			native_draw_custom_canvas(layout, canvas, paint);
	}

	public int getParagraphDirection(int line) {
		if (line < 0 || line >= getLineCount())
			throw new ArrayIndexOutOfBoundsException();
		return /*DIR_LEFT_TO_RIGHT*/ 1;
	}

	public static float getDesiredWidth(CharSequence source, int start, int end, TextPaint paint) {
		return getDesiredWidth(source.subSequence(start, end), paint);
	}

	public static float getDesiredWidth(CharSequence source, TextPaint paint) {
		long layout = native_constructor(source != null ? source.toString() : "", paint.paint, -1);
		float width = native_get_desired_width(layout);
		native_free(layout);
		return width;
	}

	public int getLineBaseline(int line) {
		if (line < 0 || line >= getLineCount())
			throw new ArrayIndexOutOfBoundsException();
		return native_get_line_baseline(layout, line);
	}

	public int getLineAscent(int line) {
		if (line < 0 || line >= getLineCount())
			throw new ArrayIndexOutOfBoundsException();
		return native_get_line_ascent(layout, line);
	}

	public int getLineDescent(int line) {
		if (line < 0 || line >= getLineCount())
			throw new ArrayIndexOutOfBoundsException();
		return native_get_line_descent(layout, line);
	}

	public int getTopPadding() { return -5; }

	public int getBottomPadding() { return 5; }

	public boolean isRtlCharAt(int offset) { return false; }

	public float getSecondaryHorizontal(int offset) {
		if (getLineDirections(0) == null)
			throw new NullPointerException();
		return native_get_secondary_horizontal(layout, offset);
	}

	public int getLineForVertical(int y) {
		return native_get_line_for_vertical(layout, y);
	}

	public int getOffsetForHorizontal(int line, float x) {
		if (getLineDirections(0) == null)
			throw new NullPointerException();
		return native_get_offset_for_horizontal(layout, line, x);
	}

	public float getPrimaryHorizontal(int offset) {
		if (getLineDirections(0) == null)
			throw new NullPointerException();
		return native_get_primary_horizontal(layout, offset);
	}

	public int getLineForOffset(int offset) {
		return native_get_line_for_offset(layout, offset);
	}

	public int getLineTop(int line) {
		if (line < 0 || line >= getLineCount())
			throw new ArrayIndexOutOfBoundsException();
		return native_get_line_top(layout, line);
	}

	public int getLineBottom(int line) {
		if (line < 0 || line >= getLineCount())
			throw new ArrayIndexOutOfBoundsException();
		return native_get_line_bottom(layout, line);
	}

	public float getLineLeft(int line) {
		if (line < 0 || line >= getLineCount())
			throw new ArrayIndexOutOfBoundsException();
		return native_get_line_left(layout, line);
	}

	public float getLineRight(int line) {
		if (line < 0 || line >= getLineCount())
			throw new ArrayIndexOutOfBoundsException();
		return native_get_line_right(layout, line);
	}

	public int getLineStart(int line) {
		if (line < 0 || line >= getLineCount())
			throw new ArrayIndexOutOfBoundsException();
		return native_get_line_start(layout, line);
	}

	public int getLineEnd(int line) {
		if (line < 0 || line >= getLineCount())
			throw new ArrayIndexOutOfBoundsException();
		return native_get_line_end(layout, line);
	}

	public boolean isSpanned() {
		return text instanceof Spanned;
	}

	public void increaseWidthTo(int width) {
		if (width < getWidth())
			throw new RuntimeException("cannot decrease width");
		native_set_width(layout, width);
	}

	public float getSpacingMultiplier() {
		return spacing_mult;
	}

	public float getSpacing_add() {
		return spacing_add;
	}

	public void getSelectionPath(int start, int end, Path path) {
		if (start != end && getLineDirections(0) == null)
			throw new NullPointerException();
	}

	public int getParagraphRight(int line) {
		return getWidth();
	}

	public int getParagraphLeft(int line) {
		return 0;
	}

	public Alignment getParagraphAlignment(int line) {
		return align;
	}

	public int getOffsetToRightOf(int offset) {
		if (getLineDirections(0) == null)
			throw new NullPointerException();
		return offset;
	}

	public int getOffsetToLeftOf(int offset) {
		if (getLineDirections(0) == null)
			throw new NullPointerException();
		return offset;
	}

	public Directions getLineDirections(int line) {
		if (line < 0 || line >= getLineCount())
			throw new ArrayIndexOutOfBoundsException();
		return new Directions();
	}

	public int getLineVisibleEnd(int line) {
		return getLineEnd(line);
	}

	public boolean getLineContainsTab(int line) { return text.toString().split("\n")[line].contains("\t"); }

	public int getEllipsizedWidth() {
		return getWidth();
	}

	public int getEllipsisStart(int line) {
		if (line < 0 || line >= getLineCount())
			throw new ArrayIndexOutOfBoundsException();
		return 0;
	}

	protected static native long native_constructor(String text, long paint, int width);
	protected native void native_set_width(long layout, int width);
	protected native int native_get_width(long layout);
	protected native int native_get_height(long layout);
	protected native int native_get_line_count(long layout);
	protected native int native_get_line_start(long layout, int line);
	protected native int native_get_line_end(long layout, int line);
	protected native int native_get_line_top(long layout, int line);
	protected native int native_get_line_bottom(long layout, int line);
	protected native int native_get_line_left(long layout, int line);
	protected native int native_get_line_right(long layout, int line);
	protected native int native_get_line_width(long layout, int line);
	protected native int native_get_line_baseline(long layout, int line);
	protected native int native_get_line_ascent(long layout, int line);
	protected native int native_get_line_descent(long layout, int line);
	protected native int native_get_line_for_vertical(long layout, int y);
	protected native void native_set_ellipsize(long layout, int ellipsize_mode, float ellipsize_width);
	protected native int native_get_ellipsis_count(long layout, int line);
	protected native void native_draw(long layout, long snapshot, long paint);
	protected native void native_draw_custom_canvas(long layout, Canvas canvas, Paint paint);
	protected native int native_get_line_for_offset(long layout, int offset);
	protected native float native_get_primary_horizontal(long layout, int offset);
	protected native float native_get_secondary_horizontal(long layout, int offset);
	protected native int native_get_offset_for_horizontal(long layout, int line, float x);
	protected static native float native_get_desired_width(long layout);
	protected static native void native_free(long layout);
}

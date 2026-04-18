package android.graphics;

import android.annotation.UnsupportedAppUsage;
import android.util.Log;

/*
 * Path is implemented as a GskPath or a GskPathBuilder. It can only be one of the two at a time.
 * The methods getGskPath() and getBuilder() automatically convert between the two as needed.
 */
public class Path {

	public enum FillType {
		WINDING,
		EVEN_ODD,
		INVERSE_WINDING,
		INVERSE_EVEN_ODD,
	}

	public enum Direction {
		CW,
		CCW,
	}

	public enum Op {
		DIFFERENCE,
		INTERSECT,
		UNION,
		XOR,
		REVERSE_DIFFERENCE,
	}

	private long builder;
	private long path;
	private FillType fillType = FillType.WINDING;

	public Path() {}

	public Path(Path path) {
		this.path = native_ref_path(path.getGskPath());
	}

	private long getBuilder() {
		if (builder == 0 || path != 0) {
			builder = native_create_builder(path, builder);
			path = 0;
		}
		return builder;
	}

	@UnsupportedAppUsage
	public long getGskPath() {
		if (path == 0) {
			path = native_create_path(builder);
		}
		return path;
	}

	public void reset() {
		native_reset(path, builder);
		path = 0;
		builder = 0;
	}

	public void rewind() {
		reset();
	}

	public void close() {
		native_close(getBuilder());
	}

	public void setFillType(FillType fillType) {
		this.fillType = fillType;
	}

	public FillType getFillType() {
		return fillType;
	}

	public void moveTo(float x, float y) {
		native_move_to(getBuilder(), x, y);
	}

	public void lineTo(float x, float y) {
		native_line_to(getBuilder(), x, y);
	}

	public void arcTo(RectF oval, float startAngle, float sweepAngle, boolean forceMoveTo) {
		arcTo(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, forceMoveTo);
	}

	public void arcTo(RectF oval, float startAngle, float sweepAngle) {
		arcTo(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, false);
	}

	public void arcTo(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean forceMoveTo) {
		native_arc_to(getBuilder(), left, top, right, bottom, startAngle, sweepAngle, forceMoveTo);
	}

	public void cubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
		native_cubic_to(getBuilder(), x1, y1, x2, y2, x3, y3);
	}

	public void quadTo(float x1, float y1, float x2, float y2) {
		native_quad_to(getBuilder(), x1, y1, x2, y2);
	}

	public void rMoveTo(float x, float y) {
		native_rel_move_to(getBuilder(), x, y);
	}

	public void rLineTo(float x, float y) {
		native_rel_line_to(getBuilder(), x, y);
	}

	public void rCubicTo(float x1, float y1, float x2, float y2, float x3, float y3) {
		native_rel_cubic_to(getBuilder(), x1, y1, x2, y2, x3, y3);
	}

	public void rQuadTo(float x1, float y1, float x2, float y2) {
		native_rel_quad_to(getBuilder(), x1, y1, x2, y2);
	}

	public void addArc(RectF oval, float startAngle, float sweepAngle) {
		addArc(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle);
	}

	public void addArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle) {
		native_add_arc(getBuilder(), left, top, right, bottom, startAngle, sweepAngle);
	}

	public void addPath(Path path, Matrix matrix) {
		native_add_path(getBuilder(), path.getGskPath(), matrix.ni());
	}

	public void addPath(Path path, float deltaX, float deltaY) {
		Matrix matrix = new Matrix();
		matrix.setTranslate(deltaX, deltaY);
		addPath(path, matrix);
	}

	public void addPath(Path path) {
		addPath(path, Matrix.IDENTITY_MATRIX);
	}

	public void addRect(RectF rect, Direction direction) {
		addRect(rect.left, rect.top, rect.right, rect.bottom, direction);
	}

	public void addRect(float left, float top, float right, float bottom, Path.Direction dir) {
		native_add_rect(getBuilder(), left, top, right, bottom);
	}

	public void addRoundRect(float left, float top, float right, float bottom,
	                         float[] radii, Direction direction) {
		native_add_round_rect(getBuilder(), left, top, right, bottom, radii);
	}

	public void addRoundRect(RectF rect, float[] radii, Direction direction) {
		addRoundRect(rect.left, rect.top, rect.right, rect.bottom, radii, direction);
	}

	public void addRoundRect(RectF rect, float rx, float ry, Direction direction) {
		addRoundRect(rect.left, rect.top, rect.right, rect.bottom, rx, ry, direction);
	}

	public void addRoundRect(float left, float top, float right, float bottom,
	                         float rx, float ry, Direction direction) {
		addRoundRect(left, top, right, bottom, new float[] {rx, ry, rx, ry, rx, ry, rx, ry}, direction);
	}

	public void addOval(float left, float top, float right, float bottom, Direction direction) {
		float rx = (right - left) / 2;
		float ry = (bottom - top) / 2;
		addRoundRect(left, top, right, bottom, new float[] {rx, ry, rx, ry, rx, ry, rx, ry}, direction);
	}

	public void addOval(RectF rect, Direction direction) {
		addOval(rect.left, rect.top, rect.right, rect.bottom, direction);
	}

	public void addCircle(float x, float y, float radius, Direction direction) {
		Log.w("Path", "STUB: addCircle");
	}

	public void transform(Matrix matrix) {
		builder = native_transform(getGskPath(), matrix.ni());
		path = 0;
	}

	public void transform(Matrix matrix, Path out_path) {
		if (out_path == null)
			out_path = this;

		out_path.transform(matrix);
	}

	public void computeBounds(RectF bounds, boolean exact) {
		native_get_bounds(getGskPath(), bounds);
	}

	public boolean op(Path path, Op op) {
		Log.w("Path", "STUB: op");
		return false;
	}

	public boolean op(Path path, Path dst, Op op) {
		Log.w("Path", "STUB: op");
		return false;
	}

	public boolean isEmpty() {
		return path == 0 && builder == 0;
	}

	public void incReserve(int additionalPoints) {}

	public boolean isConvex() {
		Log.w("Path", "STUB: isConvex");
		return false;
	}

	public void set(Path src) {
		reset();
		addPath(src);
	}

	public void offset(float dx, float dy) {
		Matrix matrix = new Matrix();
		matrix.setTranslate(dx, dy);
		transform(matrix);
	}

	public void setLastPoint(float x, float y) {
		Log.w("Path", "STUB: setLastPoint");
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void finalize() throws Throwable {
		try {
			reset();
		} finally {
			super.finalize();
		}
	}

	private static native long native_create_builder(long path, long builder);
	private static native long native_create_path(long builder);
	private static native long native_ref_path(long path);
	private static native void native_reset(long path, long builder);
	private static native void native_close(long builder);
	private static native void native_move_to(long builder, float x, float y);
	private static native void native_line_to(long builder, float x, float y);
	private static native void native_cubic_to(long builder, float x1, float y1, float x2, float y2, float x3, float y3);
	private static native void native_quad_to(long builder, float x1, float y1, float x2, float y2);
	private static native void native_arc_to(long builder, float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean forceMoveTo);
	private static native void native_rel_move_to(long builder, float x, float y);
	private static native void native_rel_line_to(long builder, float x, float y);
	private static native void native_rel_cubic_to(long builder, float x1, float y1, float x2, float y2, float x3, float y3);
	private static native void native_rel_quad_to(long builder, float x1, float y1, float x2, float y2);
	private static native void native_add_arc(long builder, float left, float top, float right, float bottom, float startAngle, float sweepAngle);
	private static native void native_add_path(long builder, long path, long matrix);
	private static native void native_add_rect(long builder, float left, float top, float right, float bottom);
	private static native void native_add_round_rect(long builder, float left, float top, float right, float bottom, float[] radii);
	private static native void native_get_bounds(long path, RectF rect);
	private static native long native_transform(long path, long matrix);
}

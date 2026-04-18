package android.graphics.drawable;

import android.atl.GskCanvas;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.LayoutDirection;
import android.util.TypedValue;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class Drawable {
	public static interface Callback {
		public void invalidateDrawable(Drawable drawable);
		public void scheduleDrawable(Drawable drawable, Runnable runnable, long time);
		public void unscheduleDrawable(Drawable drawable, Runnable runnable);
	}

	static final BlendMode DEFAULT_BLEND_MODE = BlendMode.SRC_IN;
	static final PorterDuff.Mode DEFAULT_TINT_MODE = PorterDuff.Mode.SRC_IN;

	private Rect mBounds = new Rect();
	private int[] mStateSet = new int[0];
	public long paintable;

	private Callback callback = null;

	public Drawable() {
		this.paintable = native_constructor();
	}

	public Drawable(long paintable) {
		native_ref(paintable);
		this.paintable = paintable;
	}

	protected void setPaintable(long paintable) {
		if (this.paintable != 0)
			native_unref(this.paintable);
		if (paintable != 0)
			native_ref(paintable);
		this.paintable = paintable;
	}

	public int getChangingConfigurations() {
		return 0;
	}

	public void setChangingConfigurations(int bitmap) {}

	public ConstantState getConstantState() {
		return new ConstantState() {
			@Override
			public Drawable newDrawable(Resources res) {
				return Drawable.this;
			}

			@Override
			public Drawable newDrawable() {
				return Drawable.this;
			}

			@Override
			public int getChangingConfigurations() {
				return Drawable.this.getChangingConfigurations();
			}
		};
	}

	public static abstract class ConstantState {

		public abstract Drawable newDrawable(Resources res);

		public abstract Drawable newDrawable();

		public Drawable newDrawable(Resources res, Theme theme) {
			return newDrawable(res);
		}

		public abstract int getChangingConfigurations();
	}

	public void setBounds(int left, int top, int right, int bottom) {
		boolean changed = left != mBounds.left || top != mBounds.top || right != mBounds.right || bottom != mBounds.bottom;
		mBounds.set(left, top, right, bottom);
		if (changed)
			onBoundsChange(mBounds);
	}

	public void setBounds(Rect bounds) {
		setBounds(bounds.left, bounds.top, bounds.right, bounds.bottom);
	}

	public final Rect getBounds() {
		return mBounds;
	}

	public void draw(Canvas canvas) {
		if (canvas instanceof GskCanvas) {
			if (mBounds.left != 0 || mBounds.top != 0)
				canvas.translate(mBounds.left, mBounds.top);
			native_draw(paintable, ((GskCanvas)canvas).snapshot, mBounds.width(), mBounds.height());
			if (mBounds.left != 0 || mBounds.top != 0)
				canvas.translate(-mBounds.left, -mBounds.top);
		}
	}

	public boolean setState(int[] stateSet) {
		if (!Arrays.equals(this.mStateSet, stateSet)) {
			this.mStateSet = stateSet;
			return onStateChange(stateSet);
		}
		return false;
	}

	public int[] getState() {
		return mStateSet;
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	public Callback getCallback() {
		return this.callback;
	}

	public void invalidateSelf() {
		native_invalidate(paintable);

		/* this shouldn't ever be needed with Gtk, but let's play it safe for now */
		if (this.callback != null) {
			callback.invalidateDrawable(this);
		}
	}

	public void scheduleSelf(Runnable runnable, long time) {
		if (this.callback != null) {
			callback.scheduleDrawable(this, runnable, time);
		}
	}

	public void unscheduleSelf(Runnable runnable) {
		if (this.callback != null) {
			callback.unscheduleDrawable(this, runnable);
		}
	}

	public boolean isVisible() {
		return false;
	}

	public boolean setVisible(boolean visible, boolean restart) {
		return false;
	}

	protected static TypedArray obtainAttributes(Resources r, Theme theme, AttributeSet set, int[] attrs) {
		if (theme != null)
			return theme.obtainStyledAttributes(set, attrs, 0, 0);
		else
			return r.obtainAttributes(set, attrs);
	}

	public void clearColorFilter() {}

	public final int getLevel() { return 0; }
	public final boolean setLevel(int level) { return false; }

	public void setColorFilter(int color, PorterDuff.Mode mode) {
		setColorFilter(new PorterDuffColorFilter(color, mode));
	}
	public void setColorFilter(ColorFilter filter) {}

	public Drawable mutate() {
		return this;
	}

	public int getIntrinsicWidth() { return -1; }
	public int getIntrinsicHeight() { return -1; }

	public void setTintList(ColorStateList tint) {}

	public void setTint(int tint) {
		setTintList(ColorStateList.valueOf(tint));
	}

	public static BlendMode parseBlendMode(int value, BlendMode defaultMode) {
		return defaultMode;
	}

	/* Copyright (C) 2006 The Android Open Source Project */
	BlendModeColorFilter updateBlendModeFilter(BlendModeColorFilter blendFilter, ColorStateList tint, BlendMode blendMode) {
		if (tint == null || blendMode == null) {
			return null;
		}

		final int color = tint.getColorForState(getState(), Color.TRANSPARENT);
		if (blendFilter == null || blendFilter.getColor() != color
		    || blendFilter.getMode() != blendMode) {
			return new BlendModeColorFilter(color, blendMode);
		}
		return blendFilter;
	}

	/* Copyright (C) 2006 The Android Open Source Project */
	PorterDuffColorFilter updateTintFilter(PorterDuffColorFilter tintFilter, ColorStateList tint, PorterDuff.Mode tintMode) {
		System.out.println("updateTintFilter(" + tintFilter + ", " + tint + ", " + tintMode + ")");
		if (tint == null || tintMode == null) {
			return null;
		}
		final int color = tint.getColorForState(getState(), Color.TRANSPARENT);
		if (tintFilter == null || tintFilter.getColor() != color || tintFilter.getMode() != tintMode) {
			return new PorterDuffColorFilter(color, tintMode);
		}
		return tintFilter;
	}

	public boolean isStateful() {
		return false;
	}

	public void setTintMode(PorterDuff.Mode tintMode) {}

	public boolean isProjected() { return false; }

	public static Drawable createFromXml(Resources resources, XmlResourceParser parser) throws XmlPullParserException, IOException {
		return createFromXml(resources, parser, null);
	}

	public static Drawable createFromXml(Resources resources, XmlResourceParser parser, Theme theme) throws XmlPullParserException, IOException {
		int type;
		while ((type = parser.next()) != XmlPullParser.START_TAG && type != XmlPullParser.END_DOCUMENT)
			;
		if (type != XmlPullParser.START_TAG)
			throw new XmlPullParserException("No start tag found");

		return createFromXmlInner(resources, parser, parser, theme);
	}

	public static Drawable createFromXmlInner(Resources resources, XmlPullParser parser, AttributeSet attrs) throws XmlPullParserException, IOException {
		return createFromXmlInner(resources, parser, attrs, null);
	}

	public static Drawable createFromXmlInner(Resources resources, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
		switch (parser.getName()) {
			case "selector": {
				StateListDrawable drawable = new StateListDrawable();
				drawable.inflate(resources, parser, attrs, theme);
				return drawable;
			}
			case "shape": {
				GradientDrawable drawable = new GradientDrawable();
				drawable.inflate(resources, parser, attrs, theme);
				return drawable;
			}
			case "bitmap": {
				BitmapDrawable drawable = new BitmapDrawable();
				drawable.inflate(resources, parser, attrs, theme);
				return drawable;
			}
			case "transition": {
				return new Drawable();
			}
			case "ripple": {
				RippleDrawable drawable = new RippleDrawable();
				drawable.inflate(resources, parser, attrs, theme);
				return drawable;
			}
			case "vector": {
				VectorDrawable drawable = new VectorDrawable();
				drawable.inflate(resources, parser, attrs, theme);
				return drawable;
			}
			case "layer-list": {
				LayerDrawable drawable = new LayerDrawable();
				drawable.inflate(resources, parser, attrs, theme);
				return drawable;
			}
			case "nine-patch": {
				return new NinePatchDrawable(resources, null, null, null, null);
			}
			case "animation-list": {
				return new AnimationDrawable();
			}
			case "adaptive-icon": {
				AdaptiveIconDrawable drawable = new AdaptiveIconDrawable();
				drawable.inflate(resources, parser, attrs, theme);
				return drawable;
			}
			case "inset": {
				InsetDrawable drawable = new InsetDrawable();
				drawable.inflate(resources, parser, attrs, theme);
				return drawable;
			}
		}

		return null;
	}

	public static Drawable createFromResourceStream(Resources resources, TypedValue value, InputStream is, String file,
	                                                Object object) {
		if (!file.endsWith(".9.png")) {
			final Bitmap bm = BitmapFactory.decodeStream(is);
			if (bm == null)
				return null;
			return new BitmapDrawable(resources, bm);
		}
		Path path = Paths.get(android.os.Environment.getExternalStorageDirectory().getPath(), file);
		if (!Files.exists(path)) {
			try (InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(file)) {
				if (inputStream != null) {
					Files.createDirectories(path.getParent());
					Files.copy(inputStream, path);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new NinePatchDrawable(path.toString());
	}

	public static Drawable createFromPath(String path) {
		if (path == null)
			return null;

		if (path.endsWith(".9.png"))
			return new NinePatchDrawable(path);
		long paintable = native_paintable_from_path(path);
		return new Drawable(paintable);
	}

	protected boolean onStateChange(int[] stateSet) {
		return false;
	}

	public void setAlpha(int alpha) {}

	public boolean getPadding(Rect padding) {
		return false;
	}

	public void copyBounds(Rect bounds) {
		bounds.set(mBounds);
	}

	public int getMinimumWidth() {
		return 10; // FIXME
	}
	public int getMinimumHeight() {
		return 10; // FIXME
	}

	protected void onBoundsChange(Rect bounds) {}

	public void setDither(boolean dither) {}

	public void setAutoMirrored(boolean mirrored) {}

	public void jumpToCurrentState() {}

	public boolean setLayoutDirection(int layoutDirection) {
		return false;
	}

	public void setHotspot(float x, float y) {}

	public int getLayoutDirection() {
		return LayoutDirection.LTR;
	}

	static int resolveDensity(Resources r, int parentDensity) {
		final int densityDpi = r == null ? parentDensity : r.getDisplayMetrics().densityDpi;
		return densityDpi == 0 ? DisplayMetrics.DENSITY_DEFAULT : densityDpi;
	}

	public void setFilterBitmap(boolean filter) {}

	public void setHotspotBounds(int left, int top, int right, int bottom) {}

	@SuppressWarnings("removal")
	protected void finalize() throws Throwable {
		try {
			if (paintable != 0)
				native_unref(paintable);
			paintable = 0;
		} finally {
			super.finalize();
		}
	}

	protected static native long native_paintable_from_path(String path);
	protected native long native_constructor();
	protected native void native_invalidate(long paintable);
	protected native void native_draw(long paintable, long snapshot, int width, int height);
	protected native void native_ref(long paintable);
	protected native void native_unref(long paintable);
}

package android.graphics.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;

public class NinePatchDrawable extends Drawable {

	public NinePatchDrawable(Resources res, Bitmap bitmap, byte[] data, Rect padding, String name) {
		if (bitmap != null && data != null)
			setPaintable(nativeCreate(data, bitmap.getTexture()));
	}

	NinePatchDrawable(String path) {
		setPaintable(nativeCreate(path));
	}

	@Override
	public void setTint(int tint) {
		nativeSetTint(paintable, tint);
	}

	private native long nativeCreate(byte[] data, long texture);
	private native long nativeCreate(String path);
	private native void nativeSetTint(long paintable, int tint);
}

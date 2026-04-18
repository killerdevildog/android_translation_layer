package android.app;

import android.content.Context;
import android.graphics.Bitmap;

public class WallpaperManager {

	public static WallpaperManager getInstance(Context context) {
		return new WallpaperManager();
	}

	public void setBitmap(Bitmap bitmap) {
		set_bitmap(bitmap.getTexture());
	}

	private static native void set_bitmap(long texture);
}

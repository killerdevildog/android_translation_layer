package android.view;

import android.os.IBinder;

public interface WindowManager {
	public android.view.Display getDefaultDisplay();

	public void addView(View view, ViewGroup.LayoutParams params);

	public void updateViewLayout(View view, ViewGroup.LayoutParams params);

	public void removeView(View view);

	public void removeViewImmediate(View view);

	public class LayoutParams extends ViewGroup.LayoutParams {
		public static final int FLAG_KEEP_SCREEN_ON = 0;
		public static final int FLAG_DIM_BEHIND = 2;
		public static final int FLAG_NOT_FOCUSABLE = 8;

		public float screenBrightness = -1;
		public int softInputMode;
		public int x;
		public int y;
		public int windowAnimations;
		public int flags;
		public float alpha;
		public int type;
		public IBinder token;
		public int format;
		public int layoutInDisplayCutoutMode;

		public LayoutParams(int w, int h, int type, int flags, int format) {
			super(w, h);
		}

		public LayoutParams() {}

		public void setTitle(CharSequence title) {}
	}
}

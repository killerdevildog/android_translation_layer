package android.view.animation;

import android.os.Handler;

public class Animation {

	public interface AnimationListener {
		public void onAnimationEnd(Animation animation);
		public void onAnimationRepeat(Animation animation);
		public void onAnimationStart(Animation animation);
	}

	private AnimationListener listener;

	public void setDuration(long durationMillis) {}

	public void setInterpolator(Interpolator i) {}

	public void cancel() {}

	public void setFillBefore(boolean dummy) {}
	public void setFillAfter(boolean dummy) {}

	public void setStartOffset(long offset) {}

	public void setAnimationListener(AnimationListener l) {
		this.listener = l;
	}

	public void setRepeatCount(int count) {}

	public void setRepeatMode(int mode) {}

	public void initialize(int width, int height, int parentWidth, int parentHeight) {}

	public void applyTransformation(float interpolatedTime, Transformation t) {}

	public void reset() {}

	public void start() {
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				initialize(0, 0, 0, 0);
				if (listener != null)
					listener.onAnimationStart(Animation.this);
				applyTransformation(1, new Transformation());
				if (listener != null)
					listener.onAnimationEnd(Animation.this);
			}
		});
	}

	public boolean hasStarted() {
		return false;
	}
}

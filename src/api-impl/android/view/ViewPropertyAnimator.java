package android.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import java.util.ArrayList;
import java.util.List;

public class ViewPropertyAnimator {

	private View view;
	private Animator.AnimatorListener listener;
	private long startDelay;
	private long duration = 300;
	private Runnable startAction;
	private Runnable endAction;
	private TimeInterpolator interpolator;
	private List<PropertyValuesHolder> values = new ArrayList<>();
	private ObjectAnimator animator;
	private boolean start_pending = false;

	public ViewPropertyAnimator(View view) {
		this.view = view;
	}

	private void scheduleAutoStart() {
		if (!start_pending) {
			start_pending = true;
			view.postOnAnimation(new Runnable() {
				@Override
				public void run() {
					if (start_pending)
						start();
				}
			});
		}
	}

	public void cancel() {
		if (animator != null)
			animator.cancel();
		start_pending = false;
	}

	public ViewPropertyAnimator setInterpolator(TimeInterpolator interpolator) {
		this.interpolator = interpolator;
		return this;
	}

	public ViewPropertyAnimator setListener(Animator.AnimatorListener listener) {
		this.listener = listener;
		return this;
	}

	public ViewPropertyAnimator alpha(float alpha) {
		values.add(PropertyValuesHolder.ofFloat(View.ALPHA, alpha));
		scheduleAutoStart();
		return this;
	}

	public ViewPropertyAnimator setDuration(long duration) {
		this.duration = duration;
		return this;
	}

	public ViewPropertyAnimator setStartDelay(long startDelay) {
		this.startDelay = startDelay;
		return this;
	}

	public ViewPropertyAnimator x(float x) {
		values.add(PropertyValuesHolder.ofFloat("x", x));
		scheduleAutoStart();
		return this;
	}

	public ViewPropertyAnimator y(float y) {
		values.add(PropertyValuesHolder.ofFloat("y", y));
		scheduleAutoStart();
		return this;
	}

	public ViewPropertyAnimator rotation(float rotation) {
		values.add(PropertyValuesHolder.ofFloat("rotation", rotation));
		scheduleAutoStart();
		return this;
	}

	public ViewPropertyAnimator translationX(float translationX) {
		values.add(PropertyValuesHolder.ofFloat(View.TRANSLATION_X, translationX));
		scheduleAutoStart();
		return this;
	}

	public ViewPropertyAnimator translationY(float translationY) {
		values.add(PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, translationY));
		scheduleAutoStart();
		return this;
	}

	public ViewPropertyAnimator scaleX(float scaleX) {
		values.add(PropertyValuesHolder.ofFloat(View.SCALE_X, scaleX));
		scheduleAutoStart();
		return this;
	}

	public ViewPropertyAnimator scaleY(float scaleY) {
		values.add(PropertyValuesHolder.ofFloat(View.SCALE_Y, scaleY));
		scheduleAutoStart();
		return this;
	}

	public ViewPropertyAnimator translationXBy(float translationX) {
		values.add(PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.getTranslationX() + translationX));
		scheduleAutoStart();
		return this;
	}

	public ViewPropertyAnimator rotationBy(float rotation) {
		values.add(PropertyValuesHolder.ofFloat("rotation", view.getRotation() + rotation));
		scheduleAutoStart();
		return this;
	}

	public void start() {
		start_pending = false;
		if (animator != null)
			animator.cancel();
		animator = ObjectAnimator.ofPropertyValuesHolder(view, values.toArray(new PropertyValuesHolder[0]));
		values.clear();
		if (listener != null)
			animator.addListener(listener);
		if (startAction != null || endAction != null) {
			animator.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animation) {
					if (startAction != null)
						startAction.run();
				}
				@Override
				public void onAnimationEnd(Animator animation) {
					if (endAction != null)
						endAction.run();
				}
				@Override
				public void onAnimationCancel(Animator animation) {}
				@Override
				public void onAnimationRepeat(Animator animation) {}
			});
		}
		animator.setDuration(duration);
		animator.setStartDelay(startDelay);
		if (interpolator != null)
			animator.setInterpolator(interpolator);
		animator.start();
	}

	public ViewPropertyAnimator withEndAction(Runnable runnable) {
		this.endAction = runnable;
		return this;
	}

	public ViewPropertyAnimator withStartAction(Runnable runnable) {
		this.startAction = runnable;
		return this;
	}

	public ViewPropertyAnimator withLayer() {
		return this;
	}
}

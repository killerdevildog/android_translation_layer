package android.view.animation;

import android.animation.TimeInterpolator;

public class OvershootInterpolator implements TimeInterpolator {

	private float overshoot;

	public OvershootInterpolator(float overshoot) {
		this.overshoot = overshoot;
	}

	@Override
	public float getInterpolation(float input) {
		return (input - 1) * (input - 1) * ((overshoot + 1) * (input - 1) + overshoot) + 1;
	}
}

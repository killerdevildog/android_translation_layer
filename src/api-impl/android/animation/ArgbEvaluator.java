package android.animation;

import android.graphics.Color;

public class ArgbEvaluator implements TypeEvaluator<Integer> {

	public static ArgbEvaluator getInstance() {
		return null;
	}

	@Override
	public Integer evaluate(float fraction, Integer startValue, Integer endValue) {
		return Color.argb(
		    (int)(fraction * Color.alpha(endValue) + (1 - fraction) * Color.alpha(startValue) + .5f),
		    (int)(fraction * Color.red(endValue) + (1 - fraction) * Color.red(startValue) + .5f),
		    (int)(fraction * Color.green(endValue) + (1 - fraction) * Color.green(startValue) + .5f),
		    (int)(fraction * Color.blue(endValue) + (1 - fraction) * Color.blue(startValue) + .5f));
	}
}

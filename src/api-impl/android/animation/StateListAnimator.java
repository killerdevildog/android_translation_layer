package android.animation;

import android.R;

public class StateListAnimator {

	public Animator enabledAnimator;

	public void addState(int[] specs, Animator animator) {
		if (specs.length == 1 && specs[0] == R.attr.state_enabled) {
			enabledAnimator = animator;
		}
	}
}

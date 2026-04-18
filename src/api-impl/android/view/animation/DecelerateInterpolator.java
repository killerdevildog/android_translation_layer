package android.view.animation;

public class DecelerateInterpolator extends BaseInterpolator {

	private float factor = 1.0f;

	public DecelerateInterpolator() {}

	public DecelerateInterpolator(float value) {
		factor = value;
	}

	@Override
	public float getInterpolation(float input) {
		float result;
		if (factor == 1.0f) {
			result = 1.0f - (1.0f - input) * (1.0f - input);
		} else {
			result = 1.0f - (float)Math.pow((1.0f - input), 2 * factor);
		}
		return result;
	}
}

package android.view;

import android.content.Context;

public class OrientationEventListener {

	public OrientationEventListener(Context context) {}
	public OrientationEventListener(Context context, int rate) {}

	public boolean canDetectOrientation() {
		return false;
	}

	public void disable() {}

	public void enable() {}
}

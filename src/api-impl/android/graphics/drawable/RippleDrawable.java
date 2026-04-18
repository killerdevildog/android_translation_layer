package android.graphics.drawable;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import com.android.internal.R;

public class RippleDrawable extends LayerDrawable {

	public RippleDrawable(ColorStateList colorStateList, Drawable drawable, Drawable drawable2) {
		super(drawable == null ? new Drawable[] {} : new Drawable[] {drawable});
	}

	RippleDrawable() {}

	public void setColor(ColorStateList colorStateList) {}

	public void setRadius(int radius) {}

	@Override
	public void draw(Canvas canvas) {
		final ChildDrawable[] array = mLayerState.mChildren;
		final int N = mLayerState.mNum;
		for (int i = 0; i < N; i++) {
			if (array[i].mId != R.id.mask)
				array[i].mDrawable.draw(canvas);
		}
	}
}

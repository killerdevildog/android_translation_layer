package android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ScrollView extends ViewGroup {
	public ScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public ScrollView(Context context) {
		super(context);
	}

	@Override
	protected native long native_constructor(Context context, AttributeSet attrs);
	@Override
	protected native void native_addView(long widget, long child, int index, LayoutParams params);
	@Override
	protected native void native_removeView(long widget, long child);

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = 0;
		int height = 0;
		if (getChildCount() > 0) {
			View child = getChildAt(0);
			LayoutParams lp = child.getLayoutParams();
			int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, lp.width);
			int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
			width = child.getMeasuredWidth();
			height = child.getMeasuredHeight();
		}
		setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));
	}

	public void setFillViewport(boolean fillViewport) {}

	public boolean fullScroll(int direction) {
		return true;
	}
}

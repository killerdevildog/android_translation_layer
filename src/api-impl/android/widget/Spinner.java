package android.widget;

import android.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class Spinner extends AbsSpinner {
	private Observer observer;
	private Drawable popupBackground;

	public Spinner(Context context) {
		this(context, null, 0);
		haveCustomMeasure = false;
	}

	public Spinner(Context context, AttributeSet attributeSet) {
		this(context, attributeSet, 0);
		haveCustomMeasure = false;
	}

	public Spinner(Context context, AttributeSet attributeSet, int defStyle) {
		super(context, attributeSet, defStyle);
		haveCustomMeasure = false;

		TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.Spinner, defStyle, 0);
		popupBackground = a.getDrawable(R.styleable.Spinner_popupBackground);
		a.recycle();
	}

	@Override
	protected native long native_constructor(Context context, AttributeSet attrs);
	protected native void native_setAdapter(long widget, SpinnerAdapter adapter);
	@Override
	protected native void native_setBackgroundDrawable(long widget, long paintable);
	@Override
	protected native void native_setBackgroundColor(long widget, int color);

	public void setAdapter(SpinnerAdapter adapter) {
		if (observer == null)
			observer = new Observer();
		SpinnerAdapter oldAdapter = getAdapter();
		if (oldAdapter != null)
			oldAdapter.unregisterDataSetObserver(observer);
		super.setAdapter(adapter);
		if (adapter != null)
			adapter.registerDataSetObserver(observer);
		native_setAdapter(this.widget, adapter);
	}

	public SpinnerAdapter getAdapter() {
		return (SpinnerAdapter)super.getAdapter();
	}

	@Override
	public native void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener);

	private class Observer extends DataSetObserver {

		@Override
		public void onChanged() {
			Spinner.this.native_setAdapter(widget, getAdapter());
		}
		@Override
		public void onInvalidated() {
			Spinner.this.native_setAdapter(widget, getAdapter());
		}
	}

	@Override
	void layout(int delta, boolean animate) {}

	public int getDropDownWidth() { return 100; }

	public void setDropDownHorizontalOffset(int offset){};
}

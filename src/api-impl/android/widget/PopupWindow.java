package android.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

public class PopupWindow {

	int input_method_mode = 0;

	public PopupWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		popover = native_constructor();
	}

	public PopupWindow(Context context) {
		this(context, null, 0, 0);
	}

	public PopupWindow() {
		popover = native_constructor();
	}

	public PopupWindow(View contentView, int width, int height, boolean focusable) {
		popover = native_constructor();
		setContentView(contentView);
		setWidth(width);
		setHeight(height);
		setFocusable(focusable);
	}

	public PopupWindow(View contentView, int width, int height) {
		this(contentView, width, height, true);
	}

	private View contentView;
	private Drawable background;
	private long popover; // native pointer to GtkPopover

	public interface OnDismissListener {
		public void onDismiss();
	}

	public void setBackgroundDrawable(Drawable background) {
		this.background = background;
		/* FIXME: use a decorview? */
		if (contentView != null) {
			contentView.setBackgroundDrawable(background);
		}
	}

	public void setInputMethodMode(int mode) {
		input_method_mode = mode;
	}

	public int getInputMethodMode() {
		return input_method_mode;
	}

	public boolean isShowing() {
		return native_isShowing(popover);
	}

	public void setFocusable(boolean focusable) {}

	public Drawable getBackground() {
		return background;
	}

	public void setContentView(View view) {
		contentView = view;
		if (contentView != null) {
			contentView.setBackground(getBackground());
		}
		native_setContentView(popover, view == null ? 0 : view.widget);
	}

	public int getMaxAvailableHeight(View anchor, int yOffset) { return 500; }

	public int getMaxAvailableHeight(View anchor, int yOffset, boolean ignoreKeyboard) { return 500; }

	public void setOutsideTouchable(boolean touchable) {
		/* FIXME: the semantics are different, this seems to specifically exist for cases
		 * where the popup is *not* modal, so that in addition to the window behind getting
		 * the real event, the popup gets a special MotionEvent.ACTION_OUTSIDE event */
		native_setTouchModal(popover, touchable);
	}

	public void setTouchInterceptor(View.OnTouchListener listener) {}

	public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
		native_showAsDropDown(popover, anchor.widget, xoff, yoff, gravity);
	}

	public View getContentView() {
		return contentView;
	}

	public void setTouchable(boolean touchable) {
		native_setTouchable(popover, touchable);
	}

	public void showAsDropDown(View anchor, int xoff, int yoff) {
		if (!anchor.isAttachedToWindow()) {
			Log.e("PopupWindow", "anchor is not attached to window");
			return;
		}
		native_showAsDropDown(popover, anchor.widget, xoff, yoff, Gravity.NO_GRAVITY);
	}

	public void showAtLocation(View parent, int gravity, int x, int y) {
		native_showAsDropDown(popover, parent.widget, x, y, gravity);
	}

	public void dismiss() {
		native_dismiss(popover);
	}

	public void setAnimationStyle(int animationStyle) {}

	public void setTouchModal(boolean touchModal) {
		native_setTouchModal(popover, touchModal);
	}

	public void setElevation(float elevation) {}

	public void update(View anchor, int xoff, int yoff, int width, int height) {
		native_update(popover, anchor.widget, xoff, yoff, width, height);
	}

	public void setWindowLayoutType(int type) {}

	public void setIsClippedToScreen(boolean isClippedToScreen) {}

	public void setEpicenterBounds(Rect bounds) {}

	public void setClippingEnabled(boolean enabled) {}

	/* TODO: handle LayoutParams.WRAP_CONTENT and LayoutParams.MATCH_PARENT */
	public void setWidth(int width) {
		if (width < 0)
			return;

		native_setWidth(popover, width);
	}

	public void setHeight(int height) {
		if (height < 0)
			return;

		native_setHeight(popover, height);
	}

	public int getWidth() {
		return native_getWidth(popover);
	}

	public int getHeight() {
		return native_getHeight(popover);
	}

	public void update(int x, int y, int width, int height) {}

	public void setWindowLayoutMode(int widthSpec, int heightSpec) {}

	public boolean isTouchable() {
		return native_isTouchable(popover);
	}

	public void setOverlapAnchor(boolean overlap) {
	}

	public void setSoftInputMode(int mode) {}

	protected native long native_constructor();
	protected native void native_setContentView(long widget, long contentView);
	protected native void native_showAsDropDown(long widget, long anchor, int xoff, int yoff, int gravity);
	protected native boolean native_isShowing(long widget);
	protected native void native_setTouchable(long widget, boolean touchable);
	protected native void native_setTouchModal(long widget, boolean touchable);
	protected native void native_dismiss(long widget);
	protected native void native_update(long widget, long anchor, int xoff, int yoff, int width, int height);
	public native void setOnDismissListener(OnDismissListener listener);
	public native void native_setWidth(long widget, int width);
	public native void native_setHeight(long widget, int height);
	public native int native_getWidth(long widget);
	public native int native_getHeight(long widget);
	public native boolean native_isTouchable(long widget);
}

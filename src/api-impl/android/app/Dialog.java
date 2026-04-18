package android.app;

import android.R;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

public class Dialog implements Window.Callback, DialogInterface {
	protected long nativePtr;

	protected native long nativeInit();
	private native void nativeSetTitle(long ptr, String title);
	private native void nativeSetContentView(long ptr, long widget);
	private native void nativeShow(long ptr);
	private native void nativeClose(long ptr);
	private native boolean nativeIsShowing(long ptr);

	private Context context;
	private Window window;
	private OnDismissListener onDismissListener;
	private OnShowListener onShowListener;

	public Dialog(Context context, int themeResId) {
		this.context = context;
		window = new Window(context, this);
		nativePtr = nativeInit();

		window.set_native_window(nativePtr);
	}

	public Dialog(Context context) {
		this(context, 0);
	}

	public final boolean requestWindowFeature(int featureId) {
		return false;
	}

	public Context getContext() {
		return context;
	}

	public void setContentView(View view) {
		getWindow().setContentView(view);
	}

	public void setContentView(int layoutResId) {
		setContentView(LayoutInflater.from(context).inflate(layoutResId, null));
	}

	public void setTitle(CharSequence title) {
		nativeSetTitle(nativePtr, String.valueOf(title));
	}

	public void setTitle(int titleId) {
		nativeSetTitle(nativePtr, context.getString(titleId));
	}

	public void setOwnerActivity(Activity activity) {}

	public void setCancelable(boolean cancelable) {}

	public void setOnCancelListener(OnCancelListener onCancelListener) {}

	public void setOnDismissListener(OnDismissListener onDismissListener) {
		this.onDismissListener = onDismissListener;
	}

	public View findViewById(int id) {
		return window.findViewById(id);
	}

	public void show() {
		System.out.println("showing the Dialog " + this);
		Runnable action = new Runnable() {
			@Override
			public void run() {
				onCreate(null);
				// Read the size of the main window. Floating dialogs should be smaller as specified in the windowMinWidth* attributes.
				// Non-floating are typically constructed with MATCH_PARENT layout params and thus get the exact size of the main window.
				// Most non-floating dialogs are technically dialogs, but are expected to behave more like full size activities.
				Rect displayFrame = new Rect();
				getWindow().getDecorView().getWindowVisibleDisplayFrame(displayFrame);

				TypedArray a = context.obtainStyledAttributes(R.styleable.Window);
				float windowWidthFraction = 1;
				if (a.getBoolean(R.styleable.Window_windowIsFloating, false)) {
					if (displayFrame.width() > displayFrame.height())
						windowWidthFraction = a.getFraction(R.styleable.Window_windowMinWidthMajor, 1, 1, 1);
					else
						windowWidthFraction = a.getFraction(R.styleable.Window_windowMinWidthMinor, 1, 1, 1);
				}
				a.recycle();

				LayoutParams lp = getWindow().getAttributes();
				int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(lp.width >= 0 ? lp.width : (int)(displayFrame.width() * windowWidthFraction),
				                                                        lp.width == LayoutParams.WRAP_CONTENT ? View.MeasureSpec.AT_MOST : View.MeasureSpec.EXACTLY);

				int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(lp.height >= 0 ? lp.height : (int)(displayFrame.height()),
				                                                         lp.height == LayoutParams.WRAP_CONTENT ? View.MeasureSpec.AT_MOST : View.MeasureSpec.EXACTLY);

				getWindow().getDecorView().internalSetDefaultMeasureSpec(widthMeasureSpec, heightMeasureSpec);

				nativeShow(nativePtr);
				if (onShowListener != null)
					onShowListener.onShow(Dialog.this);
			}
		};
		if (Looper.myLooper() == Looper.getMainLooper()) {
			action.run();
		} else {
			new Handler(Looper.getMainLooper()).post(action);
		}
	}

	public boolean isShowing() {
		return nativeIsShowing(nativePtr);
	}

	public void dismiss() {
		System.out.println("dismissing the Dialog " + Dialog.this);
		// HACK: dismissing the Dialog takes some time in AOSP, as the request goes back and forth between the application
		// and the system server. We replicate this behavior by adding 10 ms delay.
		// This Hack is required for NewPipe RouterActivity which has a race condition. It subscribes an rxJava observable
		// and immediately calls Dialog.dismiss(). The OnDismissListener would unsubscribes the observable again.
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
			@Override
			public void run() {
				nativeClose(nativePtr);
				if (onDismissListener != null)
					onDismissListener.onDismiss(Dialog.this);
			}
		}, 10);
	}

	public Window getWindow() {
		return window;
	}

	public void setCanceledOnTouchOutside(boolean cancel) {}

	public class Builder {
		public Builder(Context context) {
			System.out.println("making a Dialog$Builder");
		}
	}

	@Override
	public void onContentChanged() {
	}
	@Override
	public boolean onCreatePanelMenu(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'onCreatePanelMenu'");
	}
	@Override
	public View onCreatePanelView(int featureId) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'onCreatePanelView'");
	}
	@Override
	public boolean onPreparePanel(int featureId, View view, Menu menu) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'onPreparePanel'");
	}
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'onMenuItemSelected'");
	}
	@Override
	public void onPanelClosed(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'onPanelClosed'");
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'onMenuOpened'");
	}

	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("- onCreate - Dialog!");
	}

	public void hide() {
		System.out.println("hiding the Dialog " + this);
		nativeClose(nativePtr);
	}

	@Override
	public void cancel() {
		dismiss();
	}

	public void setOnShowListener(OnShowListener onShowListener) {
		this.onShowListener = onShowListener;
	}

	public void setCancelMessage(Message msg) {}

	public void setDismissMessage(Message msg) {}

	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	public void setOnKeyListener(OnKeyListener onKeyListener) {}
}

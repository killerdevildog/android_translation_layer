package android.view.inputmethod;

import android.os.IBinder;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class InputMethodManager {

	private static long im_context = nativeInit();
	private static View activeView = null;

	private ArrayList<InputMethodInfo> input_method_list = new ArrayList<InputMethodInfo>();

	public boolean hideSoftInputFromWindow(IBinder windowToken, int flags) {
		nativeHideSoftInput(im_context);
		activeView = null;
		return false;
	}

	public boolean showSoftInput(View view, int flags) {
		if (view == activeView) {
			return nativeShowSoftInput(im_context, view.widget, null, 0);
		}
		EditorInfo outAttrs = new EditorInfo();
		InputConnection ic = view.onCreateInputConnection(outAttrs);
		if (ic != null) {
			activeView = view;
			return nativeShowSoftInput(im_context, view.widget, ic, outAttrs.inputType);
		}
		return false;
	}

	public boolean isFullscreenMode() { return false; }

	public boolean isActive(View view) {
		return activeView == view;
	}

	public List<InputMethodInfo> getEnabledInputMethodList() {
		return input_method_list;
	}

	public List<InputMethodInfo> getInputMethodList() {
		return input_method_list;
	}

	public void restartInput(View view) {}

	public void updateSelection(View view, int selStart, int selEnd, int candidatesStart, int candidatesEnd) {}

	public InputMethodSubtype getCurrentInputMethodSubtype() {
		return new InputMethodSubtype();
	}

	private static native long nativeInit();
	private native boolean nativeShowSoftInput(long im_context, long widget, InputConnection ic, int inputType);
	private native void nativeHideSoftInput(long im_context);
}

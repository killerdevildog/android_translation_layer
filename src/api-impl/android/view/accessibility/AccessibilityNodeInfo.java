package android.view.accessibility;

import java.lang.CharSequence;

public class AccessibilityNodeInfo {
	public static final class AccessibilityAction {
		public static final AccessibilityAction ACTION_CONTEXT_CLICK = new AccessibilityAction(0, null);
		public static final AccessibilityAction ACTION_SET_PROGRESS = new AccessibilityAction(0, null);
		public static final AccessibilityAction ACTION_SHOW_ON_SCREEN = new AccessibilityAction(0, null);
		public static final AccessibilityAction ACTION_SCROLL_TO_POSITION = new AccessibilityAction(0, null);
		public static final AccessibilityAction ACTION_SCROLL_UP = new AccessibilityAction(0, null);
		public static final AccessibilityAction ACTION_SCROLL_LEFT = new AccessibilityAction(0, null);
		public static final AccessibilityAction ACTION_SCROLL_RIGHT = new AccessibilityAction(0, null);
		public static final AccessibilityAction ACTION_SCROLL_DOWN = new AccessibilityAction(0, null);
		public static final AccessibilityAction ACTION_PAGE_UP = new AccessibilityAction(0, null);
		public static final AccessibilityAction ACTION_PAGE_DOWN = new AccessibilityAction(0, null);
		public static final AccessibilityAction ACTION_PAGE_LEFT = new AccessibilityAction(0, null);
		public static final AccessibilityAction ACTION_PAGE_RIGHT = new AccessibilityAction(0, null);
		public static final AccessibilityAction ACTION_MOVE_WINDOW = new AccessibilityAction(0, null);
		public static final AccessibilityAction ACTION_SHOW_TOOLTIP = new AccessibilityAction(0, null);
		public static final AccessibilityAction ACTION_HIDE_TOOLTIP = new AccessibilityAction(0, null);

		public AccessibilityAction(int actionId, CharSequence label) {}

		public int getId() { return 0; }
	}

	public class RangeInfo {
		public static final int RANGE_TYPE_INT = 0;
		public static final int RANGE_TYPE_FLOAT = 1;
		public static final int RANGE_TYPE_PERCENT = 2;
	}

	public static AccessibilityNodeInfo obtain() { return null; }

	public void setClassName(CharSequence className) {}

	public void addAction(int action) {}

	public void setCheckable(boolean checkable) {}

	public void setChecked(boolean checked) {}

	public void setClickable(boolean clickable) {}

	public void setContentDescription(CharSequence description) {}

	public void setEditable(boolean editable) {}

	public void setEnabled(boolean enabled) {}

	public void setFocusable(boolean focusable) {}

	public void setFocused(boolean focused) {}

	public void setScrollable(boolean scrollable) {}

	public void setVisibleToUser(boolean visible) {}

	public void setTextSelection(int start, int end) {}

	public void setRangeInfo(RangeInfo info) {}
}

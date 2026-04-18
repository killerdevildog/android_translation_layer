package android.view;

import android.graphics.drawable.Drawable;

public interface ContextMenu extends Menu {

	public interface ContextMenuInfo {}

	void clearHeader();
	ContextMenu setHeaderIcon(int iconRes);
	ContextMenu setHeaderIcon(Drawable icon);
	ContextMenu setHeaderTitle(int titleRes);
	ContextMenu setHeaderTitle(CharSequence title);
	ContextMenu setHeaderView(View view);
}

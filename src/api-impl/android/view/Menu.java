package android.view;

import android.content.ComponentName;
import android.content.Intent;
import android.view.MenuItem;

public interface Menu {

	public static final int NONE = 0;

	public MenuItem add(int groupId, int itemId, int order, CharSequence title);

	public MenuItem add(int groupId, int itemId, int order, int titleRes);

	public MenuItem findItem(int id);

	public MenuItem getItem(int id);

	public void clear();

	public void removeGroup(int groupId);

	public SubMenu addSubMenu(int id);

	public MenuItem add(int id);

	public MenuItem add(CharSequence text);

	public void setGroupCheckable(int group, boolean checkable, boolean exclusive);

	public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title);

	public void setGroupVisible(int group, boolean visible);

	public void removeItem(int id);

	public int size();

	public boolean hasVisibleItems();

	public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes);

	public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller, Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems);
}

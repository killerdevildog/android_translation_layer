/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.widget;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnTouchListener;
import com.android.internal.R;
import java.util.ArrayList;
import java.util.List;

/**
 * A PopupMenu displays a {@link Menu} in a modal popup window anchored to a
 * {@link View}. The popup will appear below the anchor view if there is room,
 * or above it if there is not. If the IME is visible the popup will not
 * overlap it until it is touched. Touching outside of the popup will dismiss
 * it.
 */

public class PopupMenu {
	private final Context mContext;
	private final View mAnchor;
	private OnMenuItemClickListener mMenuItemClickListener;
	private OnDismissListener mOnDismissListener;
	private OnTouchListener mDragListener;
	private long popover;
	private MenuImpl menu;

	/**
	 * Constructor to create a new popup menu with an anchor view.
	 *
	 * @param context Context the popup menu is running in, through which it
	 *        can access the current theme, resources, etc.
	 * @param anchor Anchor view for this popup. The popup will appear below
	 *        the anchor if there is room, or above it if there is not.
	 */
	public PopupMenu(Context context, View anchor) {
		this(context, anchor, Gravity.NO_GRAVITY);
	}
	/**
	 * Constructor to create a new popup menu with an anchor view and alignment
	 * gravity.
	 *
	 * @param context Context the popup menu is running in, through which it
	 *        can access the current theme, resources, etc.
	 * @param anchor Anchor view for this popup. The popup will appear below
	 *        the anchor if there is room, or above it if there is not.
	 * @param gravity The {@link Gravity} value for aligning the popup with its
	 *        anchor.
	 */
	public PopupMenu(Context context, View anchor, int gravity) {
		this(context, anchor, gravity, R.attr.popupMenuStyle, 0);
	}
	/**
	 * Constructor a create a new popup menu with a specific style.
	 *
	 * @param context Context the popup menu is running in, through which it
	 *        can access the current theme, resources, etc.
	 * @param anchor Anchor view for this popup. The popup will appear below
	 *        the anchor if there is room, or above it if there is not.
	 * @param gravity The {@link Gravity} value for aligning the popup with its
	 *        anchor.
	 * @param popupStyleAttr An attribute in the current theme that contains a
	 *        reference to a style resource that supplies default values for
	 *        the popup window. Can be 0 to not look for defaults.
	 * @param popupStyleRes A resource identifier of a style resource that
	 *        supplies default values for the popup window, used only if
	 *        popupStyleAttr is 0 or can not be found in the theme. Can be 0
	 *        to not look for defaults.
	 */
	public PopupMenu(Context context, View anchor, int gravity, int popupStyleAttr,
	                 int popupStyleRes) {
		mContext = context;
		mAnchor = anchor;
		menu = new MenuImpl();
		popover = native_buildPopover(menu.menu);
	}

	/**
	 * @return a {@link MenuInflater} that can be used to inflate menu items
	 *         from XML into the menu returned by {@link #getMenu()}
	 * @see #getMenu()
	 */
	public MenuInflater getMenuInflater() {
		return new MenuInflater(mContext);
	}

	/**
	 * Sets a listener that will be notified when the user selects an item from
	 * the menu.
	 *
	 * @param listener the listener to notify
	 */
	public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
		mMenuItemClickListener = listener;
	}

	/**
	 * Sets a listener that will be notified when this menu is dismissed.
	 *
	 * @param listener the listener to notify
	 */
	public void setOnDismissListener(OnDismissListener listener) {
		mOnDismissListener = listener;
	}

	protected native long native_init();
	protected native void native_insertItem(long menu, int position, String item, int id);
	protected native void native_insertSubmenu(long menu, int position, String item, long submenu);
	protected native void native_removeItem(long menu, int position);
	protected native long native_buildPopover(long menu);
	protected native void native_show(long popover, long anchor);

	// callback from native code
	protected void menuItemClickCallback(final int id) {
		if (mMenuItemClickListener != null) {
			mMenuItemClickListener.onMenuItemClick(getMenu().findItem(id));
		}
	}

	/**
	 * Inflate a menu resource into this PopupMenu. This is equivalent to
	 * calling {@code popupMenu.getMenuInflater().inflate(menuRes, popupMenu.getMenu())}.
	 *
	 * @param menuRes Menu resource to inflate
	 * @throws Exception
	 */
	public void inflate(int menuRes) throws Exception {
		getMenuInflater().inflate(menuRes, getMenu());
	}

	/**
	* Show the menu popup anchored to the view specified during construction.
	*
	* @see #dismiss()
	*/
	public void show() {
		System.out.println("PopupMenu.show() called");
		native_show(popover, mAnchor.widget);
	}

	public Menu getMenu() {
		return menu;
	}

	public void dismiss() {
		System.out.println("PopupMenu.dismiss() called");
	}

	/**
	 * Interface responsible for receiving menu item click events if the items
	 * themselves do not have individual item click listeners.
	 */
	public interface OnMenuItemClickListener {
		/**
		 * This method will be invoked when a menu item is clicked if the item
		 * itself did not already handle the event.
		 *
		 * @param item the menu item that was clicked
		 * @return {@code true} if the event was handled, {@code false}
		 *         otherwise
		 */
		boolean onMenuItemClick(MenuItem item);
	}

	/**
	 * Callback interface used to notify the application that the menu has closed.
	 */
	public interface OnDismissListener {
		/**
		 * Called when the associated menu has been dismissed.
		 *
		 * @param menu the popup menu that was dismissed
		 */
		void onDismiss(PopupMenu menu);
	}

	private class MenuImpl implements Menu {
		long menu = native_init();

		List<MenuItemImpl> items = new ArrayList<>();
		int numVisibleItems = 0;

		@Override
		public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
			MenuItemImpl item = new MenuItemImpl(itemId, this, String.valueOf(title), null);
			items.add(item);
			item.setVisible(true);
			return item;
		}

		@Override
		public MenuItem add(int groupId, int itemId, int order, int titleRes) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'add'");
		}

		@Override
		public MenuItem findItem(int id) {
			for (MenuItemImpl item : items) {
				if (item.id == id)
					return item;
				if (item.subMenu != null) {
					MenuItem found = item.subMenu.findItem(id);
					if (found != null)
						return found;
				}
			}
			return null;
		}

		@Override
		public MenuItem getItem(int index) {
			return items.get(index);
		}

		@Override
		public void clear() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'clear'");
		}

		@Override
		public void removeGroup(int groupId) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'removeGroup'");
		}

		@Override
		public SubMenu addSubMenu(int id) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'addSubMenu'");
		}

		@Override
		public MenuItem add(int id) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'add'");
		}

		@Override
		public MenuItem add(CharSequence text) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'add'");
		}

		@Override
		public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'setGroupCheckable'");
		}

		@Override
		public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
			SubMenuImpl submenu = new SubMenuImpl(itemId, this, String.valueOf(title));
			items.add(submenu.item);
			submenu.item.setVisible(true);
			return submenu;
		}

		@Override
		public void setGroupVisible(int group, boolean visible) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'setGroupVisible'");
		}

		@Override
		public void removeItem(int id) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'removeItem'");
		}

		@Override
		public int size() {
			return items.size();
		}

		@Override
		public boolean hasVisibleItems() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'hasVisibleItems'");
		}

		@Override
		public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'addSubMenu'");
		}

		@Override
		public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller, Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
			return 0;
		}
	}

	private class SubMenuImpl extends MenuImpl implements SubMenu {
		private MenuItemImpl item;

		public SubMenuImpl(int id, MenuImpl parent, String title) {
			item = new MenuItemImpl(id, parent, title, this);
		}

		@Override
		public MenuItem getItem() {
			return item;
		}

		@Override
		public void clearHeader() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'clearHeader'");
		}

		@Override
		public SubMenu setIcon(Drawable icon) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'setIcon'");
		}
	}

	private class MenuItemImpl implements MenuItem {
		private int id;
		private MenuImpl parent;
		private String title;
		SubMenuImpl subMenu;
		int position; // position in list of visible items, or -1 if not visible

		private MenuItemImpl(int id, MenuImpl parent, String title, SubMenuImpl subMenu) {
			this.id = id;
			this.parent = parent;
			this.position = -1;
			this.title = title;
			this.subMenu = subMenu;
		}

		@Override
		public MenuItem setIcon(int iconRes) {
			return this;
		}

		@Override
		public MenuItem setIcon(Drawable icon) {
			return this;
		}

		@Override
		public MenuItem setVisible(boolean visible) {
			// GMenu doesn't support invisible items, so we remove them while they're not visible
			if (!visible && isVisible()) {
				parent.numVisibleItems--;
				for (int i = parent.items.size() - 1; i >= 0; i--) {
					MenuItemImpl item = parent.items.get(i);
					if (item != this && item.isVisible())
						item.position--;
					else if (item == this)
						break;
				}
				native_removeItem(parent.menu, position);
				position = -1;
			} else if (visible && !isVisible()) {
				position = parent.numVisibleItems++;
				for (int i = parent.items.size() - 1; i >= 0; i--) {
					MenuItemImpl item = parent.items.get(i);
					if (item != this && item.isVisible())
						position = item.position++;
					else if (item == this)
						break;
				}
				if (subMenu != null)
					native_insertSubmenu(parent.menu, position, title, subMenu.menu);
				else
					native_insertItem(parent.menu, position, title, id);
			}
			return this;
		}

		@Override
		public MenuItem setChecked(boolean checked) {
			return this;
		}

		@Override
		public MenuItem setEnabled(boolean enabled) {
			return this;
		}

		@Override
		public MenuItem setCheckable(boolean checkable) {
			return this;
		}

		@Override
		public boolean isCheckable() {
			return false;
		}

		@Override
		public MenuItem setTitleCondensed(CharSequence titleCondensed) {
			return this;
		}

		@Override
		public MenuItem setTitle(CharSequence title) {
			this.title = String.valueOf(title);
			return this;
		}

		@Override
		public MenuItem setActionView(View actionView) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'setActionView'");
		}

		@Override
		public void setShowAsAction(int action) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'setShowAsAction'");
		}

		@Override
		public int getItemId() {
			return id;
		}

		@Override
		public int getGroupId() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'getGroupId'");
		}

		@Override
		public MenuItem setOnMenuItemClickListener(OnMenuItemClickListener listener) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'setOnMenuItemClickListener'");
		}

		@Override
		public MenuItem setTitle(int resId) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'setTitle'");
		}

		@Override
		public boolean isVisible() {
			return position >= 0;
		}

		@Override
		public Drawable getIcon() {
			return new Drawable();
		}

		@Override
		public SubMenu getSubMenu() {
			return subMenu;
		}

		@Override
		public MenuItem setActionView(int resId) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'setActionView'");
		}

		@Override
		public View getActionView() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'getActionView'");
		}

		@Override
		public boolean hasSubMenu() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'hasSubMenu'");
		}

		@Override
		public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'setOnActionExpandListener'");
		}

		@Override
		public boolean isChecked() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'isChecked'");
		}

		@Override
		public MenuItem setShowAsActionFlags(int action) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'setShowAsActionFlags'");
		}

		@Override
		public MenuItem setAlphabeticShortcut(char alphaChar) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'setAlphabeticShortcut'");
		}

		@Override
		public MenuItem setShortcut(char numeric, char alpha) { return this; }

		@Override
		public int getOrder() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'getOrder'");
		}

		@Override
		public boolean isEnabled() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'isEnabled'");
		}

		@Override
		public CharSequence getTitleCondensed() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'getTitleCondensed'");
		}

		@Override
		public CharSequence getTitle() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'getTitle'");
		}

		@Override
		public MenuItem setNumericShortcut(char numericChar) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'setNumericShortcut'");
		}

		@Override
		public boolean expandActionView() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'expandActionView'");
		}

		@Override
		public boolean isActionViewExpanded() {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'isActionViewExpanded'");
		}

		@Override
		public MenuItem setIntent(Intent intent) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("Unimplemented method 'setIntent'");
		}
	}
}

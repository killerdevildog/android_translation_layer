package android.content.pm;

import java.util.Collections;
import java.util.List;

public class ShortcutManager {
	public void removeAllDynamicShortcuts() {
	}

	public List getShortcuts(int matchFlags) {
		return Collections.emptyList();
	}
	public void removeLongLivedShortcuts(List<String> shortcutIds) {
	}

	public boolean setDynamicShortcuts(List<ShortcutInfo> shortcutInfoList) {
		return true;
	}
}

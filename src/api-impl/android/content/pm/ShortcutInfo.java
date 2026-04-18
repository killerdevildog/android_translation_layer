package android.content.pm;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Parcelable;

public class ShortcutInfo implements Parcelable {
	public static final Creator<ShortcutInfo> CREATOR = null;

	public static class Builder {
		public Builder(Context context, String id) {}

		public ShortcutInfo build() {
			return new ShortcutInfo();
		}

		public Builder setIcon(Icon icon) {
			return this;
		}

		public Builder setIntent(Intent intent) {
			return this;
		}

		public Builder setLongLabel(CharSequence longLabel) {
			return this;
		}

		public Builder setShortLabel(CharSequence shortLabel) {
			return this;
		}
	}
}

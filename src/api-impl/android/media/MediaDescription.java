package android.media;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

public class MediaDescription {

	public Uri iconUri;
	public CharSequence title;
	public CharSequence subtitle;

	public static class Builder {

		MediaDescription description = new MediaDescription();

		public Builder setMediaId(String mediaId) { return this; }

		public Builder setTitle(CharSequence title) {
			description.title = title;
			return this;
		}

		public Builder setSubtitle(CharSequence subtitle) {
			description.subtitle = subtitle;
			return this;
		}

		public Builder setDescription(CharSequence description) { return this; }

		public Builder setIconBitmap(Bitmap iconBitmap) { return this; }

		public Builder setIconUri(Uri iconUri) {
			description.iconUri = iconUri;
			return this;
		}

		public Builder setExtras(Bundle extras) { return this; }

		public MediaDescription build() {
			return description;
		}
	}
}

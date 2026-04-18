package android.media;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable.Creator;

public class MediaMetadata {

	public static final Creator<MediaMetadata> CREATOR = new Creator<MediaMetadata>() {
		@Override
		public MediaMetadata createFromParcel(Parcel source) {
			return new MediaMetadata();
		}

		@Override
		public MediaMetadata[] newArray(int size) {
			return new MediaMetadata[size];
		}
	};

	public static class Builder {

		public Builder putText(String key, CharSequence value) {
			return this;
		}

		public Builder putLong(String key, long value) {
			return this;
		}

		public Builder putBitmap(String key, Bitmap value) {
			return this;
		}

		public MediaMetadata build() {
			return new MediaMetadata();
		}
	}

	public void writeToParcel(Parcel dest, int flags) {}
}

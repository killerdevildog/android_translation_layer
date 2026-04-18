package android.content;

import android.net.Uri;

public class ClipData {

	public static class Item {

		public Item(Uri uri) {}
	}

	String text;

	public ClipData(ClipDescription description, Item item) {}

	public static ClipData newPlainText(CharSequence label, CharSequence text) {
		ClipData clip = new ClipData(new ClipDescription(label, null), null);
		clip.text = text.toString();
		return clip;
	}

	public static ClipData newRawUri(CharSequence label, Uri uri) {
		ClipData clip = new ClipData(new ClipDescription(label, null), new Item(uri));
		clip.text = uri.toString();
		return clip;
	}

	public void addItem(ContentResolver resolver, Item item) {
	}

	public Item getItemAt(int index) {
		return null;
	}

	public int getItemCount() {
		return 0;
	}
}

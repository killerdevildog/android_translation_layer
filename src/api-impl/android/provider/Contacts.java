package android.provider;

import android.net.Uri;

public class Contacts {
	public static class People {
		public static final Uri CONTENT_URI = Uri.parse("content://com.android.contacts/people");

		public static final Uri CONTENT_FILTER_URI = Uri.parse("content://com.android.contacts/people_filter");
	}
}

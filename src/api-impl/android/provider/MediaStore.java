package android.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.FileNotFoundException;

public class MediaStore {

	public static class Images {

		public static class Media {

			public static final Uri EXTERNAL_CONTENT_URI = Uri.parse("content://media/external/images/media");
			public static final Uri INTERNAL_CONTENT_URI = Uri.parse("content://media/internal/images/media");
		}

		public static class Thumbnails {

			public static Cursor queryMiniThumbnail(ContentResolver contentResolver, long id, int kind, String[] projection) {
				return null;
			}

			public static Bitmap getThumbnail(ContentResolver contentResolver, long imageId, long groupId, int kind, BitmapFactory.Options options) throws FileNotFoundException {
				ParcelFileDescriptor fd = contentResolver.openFileDescriptor(Media.EXTERNAL_CONTENT_URI.buildUpon().appendPath(String.valueOf(imageId)).build(), "r");
				return BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, options);
			}
		}
	}

	public static class Video {

		public static class Media {

			public static final Uri EXTERNAL_CONTENT_URI = Uri.parse("content://media/external/video/media");
			public static final Uri INTERNAL_CONTENT_URI = Uri.parse("content://media/internal/video/media");
		}
	}

	public static class Audio {

		public static class Media {

			public static final Uri EXTERNAL_CONTENT_URI = Uri.parse("content://media/external/audio/media");
		}

		public static class Artists {
			public static final Uri EXTERNAL_CONTENT_URI = Uri.parse("content://media/external/audio/artists");
		}

		public static class Albums {
			public static final Uri EXTERNAL_CONTENT_URI = Uri.parse("content://media/external/audio/albums");
		}

		public static class Genres {
			public static final Uri EXTERNAL_CONTENT_URI = Uri.parse("content://media/external/audio/genres");
		}
	}

	public static class Files {

		public static Uri getContentUri(String type) {
			return Uri.parse("content://media/files/" + type);
		}
	}
}

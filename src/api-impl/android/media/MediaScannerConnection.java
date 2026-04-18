package android.media;

import android.content.Context;

public class MediaScannerConnection {

	public interface OnScanCompletedListener {}

	public static void scanFile(Context context, String[] filePath, String[] mimeType, OnScanCompletedListener listener) {}
}

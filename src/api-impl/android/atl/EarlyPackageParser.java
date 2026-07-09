package android.atl;

import android.content.res.XmlBlock;
import android.content.res.XmlResourceParser;
import android.util.Slog;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.xmlpull.v1.XmlPullParser;

/**
 * Parse made to determine the default SDK_INT to use for a given apk,
 * so almost no Android classes can be used there.
 */
public final class EarlyPackageParser {
	private static final String TAG = "EarlyPackageParser";
	private static final String ANDROID_MANIFEST_FILENAME = "AndroidManifest.xml";

	private EarlyPackageParser() {}

	public static int parseMinSdkInt(File apk, int def) {
		try (ZipFile zipFile = new ZipFile(apk)) {
			ZipEntry zipEntry = zipFile.getEntry(ANDROID_MANIFEST_FILENAME);
			byte[] bytes;
			try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
				bytes = streamToByteArray(inputStream);
			}
			try (XmlBlock xmlBlock = new XmlBlock(bytes);
			     XmlResourceParser parser = xmlBlock.newParser()) {
				if (parser == null) {
					Slog.w(TAG, "Failed to pre-parse the package!");
					return def;
				}
				int eventType = parser.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG && parser.getName().equals("uses-sdk")) {
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							if (parser.getAttributeName(i).equals("minSdkVersion")) {
								return parser.getAttributeIntValue(i, def);
							}
						}
					}
					eventType = parser.next();
				}
			}
		} catch (Exception e) {
			Slog.w(TAG, "Failed to pre-parse the package!", e);
		}
		return def;
	}

	private static byte[] streamToByteArray(InputStream is) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] chunk = new byte[8192];
		int bytesRead;
		while ((bytesRead = is.read(chunk)) != -1) {
			outputStream.write(chunk, 0, bytesRead);
		}
		return outputStream.toByteArray();
	}
}

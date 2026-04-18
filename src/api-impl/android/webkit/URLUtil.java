package android.webkit;

public class URLUtil {

	public static String guessFileName(String url, String contentDisposition, String mimeType) {
		String filename = url.substring(url.lastIndexOf('/') + 1);
		if (filename.contains("?"))
			filename = filename.substring(0, filename.indexOf('?'));
		return filename;
	}

	/**
	 * @return {@code true} if the url is an https: url.
	 */
	public static boolean isHttpsUrl(String url) {
		return (null != url)
		    && (url.length() > 7)
		    && url.substring(0, 8).equalsIgnoreCase("https://");
	}

	public static boolean isHttpUrl(String url) {
		return (null != url)
		    && (url.length() > 6)
		    && url.substring(0, 7).equalsIgnoreCase("http://");
	}

	public static boolean isContentUrl(String url) {
		return url.startsWith("content://");
	}
}

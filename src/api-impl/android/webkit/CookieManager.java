package android.webkit;

import android.content.Context;
import android.webkit.WebView;

public class CookieManager {

	public static CookieManager getInstance() {
		// HACK: disable NewPipe's WebView based PoToken generator for now
		if (Context.this_application.getPackageName().equals("org.schabi.newpipe")) {
			throw new RuntimeException("CookieManager not yet fully implemented");
		}
		try { // also handle NewPipe forks which can have a different packagename
			Class.forName("org.schabi.newpipe.util.potoken.PoTokenWebView");
			throw new RuntimeException("CookieManager not yet fully implemented");
		} catch (ClassNotFoundException e) {
		}
		return new CookieManager();
	}

	public void removeAllCookies(ValueCallback callback) {}

	public void removeSessionCookies(ValueCallback callback) {}

	public void removeExpiredCookie() {}

	public void removeAllCookie() {}

	public void removeSessionCookie() {}

	public void flush() {}

	public String getCookie(String url) {
		return "";
	}

	public void setCookie(String url, String value) {}

	public void setAcceptCookie(boolean accept) {}

	public boolean acceptThirdPartyCookies(WebView webview) {
		return false;
	}

	public void setAcceptThirdPartyCookies(WebView webView, boolean accept) {}

	public static void setAcceptFileSchemeCookies(boolean accept) {}
}

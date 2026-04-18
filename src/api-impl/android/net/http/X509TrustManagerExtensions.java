package android.net.http;

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.X509TrustManager;

public class X509TrustManagerExtensions {

	public X509TrustManagerExtensions(X509TrustManager tm) {}

	public List<X509Certificate> checkServerTrusted(X509Certificate[] chain,
	                                                String authType, String host) {
		return Arrays.asList(chain);
	}
}

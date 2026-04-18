package android.net;

import android.os.Handler;

class ProxyInfo {}

public class ConnectivityManager {

	public class NetworkCallback {
		public void onAvailable(Network network) {}
		public void onLost(Network network) {}
	}

	public NetworkInfo getNetworkInfo(int networkType) {
		return new NetworkInfo(nativeGetNetworkAvailable());
	}

	public NetworkInfo getActiveNetworkInfo() {
		return new NetworkInfo(nativeGetNetworkAvailable());
	}

	public void registerNetworkCallback(NetworkRequest request, NetworkCallback callback) {
		nativeRegisterNetworkCallback(request, callback);
		if (nativeGetNetworkAvailable()) {
			callback.onAvailable(new Network());
		}
	}

	protected native void nativeRegisterNetworkCallback(NetworkRequest request, NetworkCallback callback);

	public void unregisterNetworkCallback(NetworkCallback callback) {}

	public native boolean isActiveNetworkMetered();

	protected native boolean nativeGetNetworkAvailable();

	public NetworkInfo[] getAllNetworkInfo() {
		return new NetworkInfo[] {getActiveNetworkInfo()};
	}

	public Network getActiveNetwork() {
		return new Network();
	}

	public Network[] getAllNetworks() {
		return new Network[] {getActiveNetwork()};
	}

	public NetworkCapabilities getNetworkCapabilities(Network network) {
		if (!nativeGetNetworkAvailable())
			return null;
		return new NetworkCapabilities();
	}

	public void registerDefaultNetworkCallback(NetworkCallback cb, Handler hdl) {
		registerDefaultNetworkCallback(cb);
	}

	public void registerDefaultNetworkCallback(NetworkCallback cb) {
		nativeRegisterNetworkCallback(null, cb);
		if (nativeGetNetworkAvailable()) {
			cb.onAvailable(new Network());
		}
	}

	public ProxyInfo getDefaultProxy() { return null; }
}

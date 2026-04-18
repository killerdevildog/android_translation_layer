package android.net.wifi;

public class WifiManager {

	public static final int WIFI_STATE_DISABLED = 1;
	public static final int WIFI_STATE_DISABLING = 0;
	public static final int WIFI_STATE_ENABLED = 3;
	public static final int WIFI_STATE_ENABLING = 2;
	public static final int WIFI_STATE_UNKNOWN = 4;

	public class WifiLock {

		public void setReferenceCounted(boolean referenceCounted) {}

		public void release() {}

		public void acquire() {}

		public boolean isHeld() { return false; }
	}

	public WifiLock createWifiLock(int lockType, String tag) {
		return new WifiLock();
	}

	public WifiInfo getConnectionInfo() {
		return new WifiInfo();
	}

	public int getWifiState() {
		return WIFI_STATE_UNKNOWN;
	}

	public boolean isWifiEnabled() {
		return false;
	}
}

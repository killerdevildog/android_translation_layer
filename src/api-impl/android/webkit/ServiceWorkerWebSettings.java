package android.webkit;

public class ServiceWorkerWebSettings {
	ServiceWorkerWebSettings() {}

	public void setCacheMode(int mode) {}

	public int getCacheMode() {
		return 0;
	}

	public void setAllowContentAccess(boolean allow) {}

	public boolean getAllowContentAccess() {
		return false;
	}

	public void setAllowFileAccess(boolean allow) {}

	public boolean getAllowFileAccess() {
		return false;
	}

	public void setBlockNetworkLoads(boolean flag) {}

	public boolean getBlockNetworkLoads() {
		return true;
	}
}

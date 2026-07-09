package android.content.pm;

import java.util.Collections;
import java.util.List;

public class PackageInstaller {

	public List getMySessions() {
		return Collections.emptyList();
	}

	public void registerSessionCallback(SessionCallback callback) {}

	public abstract static class SessionCallback {
		public abstract void onActiveChanged(int sessionId, boolean active);

		public abstract void onBadgingChanged(int sessionId);

		public abstract void onCreated(int sessionId);

		public abstract void onFinished(int sessionId, boolean success);

		public abstract void onProgressChanged(int sessionId, float progress);
	}
}

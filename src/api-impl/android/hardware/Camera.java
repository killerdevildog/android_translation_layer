package android.hardware;

public class Camera {

	public interface PreviewCallback {}

	public interface AutoFocusCallback {}

	public interface ErrorCallback {}

	public static int getNumberOfCameras() { return 0; }

	public static Camera open() {
		return null;
	}

	public static Camera open(int cameraId) {
		return null;
	}

	public void setErrorCallback(ErrorCallback callback) {}
}

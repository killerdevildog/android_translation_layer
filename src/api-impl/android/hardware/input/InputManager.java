package android.hardware.input;

import android.os.Handler;
import android.view.InputDevice;

public class InputManager {
	public static interface InputDeviceListener {
		abstract void onInputDeviceAdded(int deviceId);
		abstract void onInputDeviceChanged(int deviceId);
		abstract void onInputDeviceRemoved(int deviceId);
	}

	public void registerInputDeviceListener(InputManager.InputDeviceListener listener, Handler handler) {
	}

	public int[] getInputDeviceIds() {
		return InputDevice.getDeviceIds();
	}

	public InputDevice getInputDevice(int id) {
		return InputDevice.getDevice(id);
	}
}

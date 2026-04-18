package android.view;

import java.util.ArrayList;
import java.util.List;

public class InputDevice {

	public static final int SOURCE_CLASS_BUTTON = 0x00000001;
	public static final int SOURCE_CLASS_POINTER = 0x00000002;
	public static final int SOURCE_CLASS_JOYSTICK = 0x00000010;
	public static final int SOURCE_CLASS_GAMEPAD = 0x00000400 | SOURCE_CLASS_BUTTON;
	public static final int SOURCE_KEYBOARD = 0x00000100 | SOURCE_CLASS_BUTTON;
	public static final int SOURCE_TOUCHSCREEN = 0x00001000 | SOURCE_CLASS_POINTER;

	public static int[] getDeviceIds() {
		return new int[] {0}; // might work?
	}

	public static InputDevice getDevice(int id) {
		return new InputDevice();
	}

	public int getId() {
		return 0; // might work?
	}

	public int getProductId() {
		return 0x69;
	}

	public int getVendorId() {
		return 0x420;
	}

	public String getName() {
		return "FIXME-name";
	}

	public String getDescriptor() {
		return "FIXME-descriptor";
	}

	public boolean isVirtual() {
		return false;
	}

	public int getSources() {
		return 2 | 4098 /* SOURCE_CLASS_POINTER | SOURCE_TOUCHSCREEN */; // FIXME
	}

	/*
	 * FIXME: We pretend we can do literally everything here...
	 */

	public boolean[] hasKeys(int... keys) {
		boolean[] ret = new boolean[keys.length];
		for (int i = 0; i < keys.length; i++) {
			ret[i] = true;
		}
		return ret;
	}

	public List<InputDevice.MotionRange> getMotionRanges() {
		MotionRange[] ranges = new MotionRange[32];

		for (int i = 0; i < ranges.length; i++) {
			ranges[i] = new MotionRange(i);
		}

		return new ArrayList<InputDevice.MotionRange>();
	}

	public boolean supportsSource(int source) {
		return true;
	}

	public MotionRange getMotionRange(int axis, int source) {
		return new MotionRange(axis);
	}

	public class MotionRange {
		int axis;

		public MotionRange(int axis) {
			this.axis = axis;
		}

		public int getAxis() {
			return this.axis;
		}
	};
}

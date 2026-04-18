package android.media;

public class AudioManager {
	public static final String PROPERTY_OUTPUT_FRAMES_PER_BUFFER = "android.media.property.OUTPUT_FRAMES_PER_BUFFER";
	public static final String PROPERTY_OUTPUT_SAMPLE_RATE = "android.media.property.OUTPUT_SAMPLE_RATE";

	public static final int STREAM_MUSIC = 0x3;

	private native void nativeSetStreamVolume(int volume);

	public boolean isBluetoothA2dpOn() {
		return false;
	}

	public String getProperty(String name) {
		switch (name) {
			case PROPERTY_OUTPUT_FRAMES_PER_BUFFER:
				return "256"; // FIXME arbitrary
			case PROPERTY_OUTPUT_SAMPLE_RATE:
				return "44100"; // FIXME arbitrary
			default:
				System.out.println("AudioManager.getProperty: >" + name + "< not handled");
				return "";
		}
	}

	public interface OnAudioFocusChangeListener {
	}

	public int getRingerMode() {
		return 0;
	}

	public int getStreamVolume(int streamType) {
		return 0; // arbitrary, shouldn't matter too much?
	}

	public int getStreamMaxVolume(int streamType) {
		return 100;
	}

	public int requestAudioFocus(OnAudioFocusChangeListener listener, int streamType, int durationHint) {
		return /*AUDIOFOCUS_REQUEST_GRANTED*/ 1;
	}

	public int abandonAudioFocus(OnAudioFocusChangeListener listener) {
		return /*AUDIOFOCUS_REQUEST_GRANTED*/ 1;
	}

	public boolean isWiredHeadsetOn() {
		return false;
	}

	public void setStreamVolume(int streamType, int index, int flags) {
		nativeSetStreamVolume(index);
	}

	public boolean isStreamMute(int streamType) {
		return false;
	}

	public boolean isMusicActive() {
		return false;
	}

	public void setSpeakerphoneOn(boolean on) {}

	public boolean isSpeakerphoneOn() {
		return false;
	}

	public void setBluetoothScoOn(boolean on) {}

	public boolean isBluetoothScoOn() {
		return false;
	}

	public void stopBluetoothSco() {}

	public void setMode(int mode) {}

	public int getMode() {
		return /*MODE_NORMAL*/ 0;
	}

	public boolean isMicrophoneMute() {
		return false;
	}

	public void setMicrophoneMute(boolean on) {
		System.out.println("AudioManager.setMicrophoneMute(" + on + ")");
	}
	public void unloadSoundEffects() {}

	public int generateAudioSessionId() {
		return 0;
	}
}

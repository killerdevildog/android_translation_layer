package android.media.audiofx;

public class AudioEffect {
	public static final int SUCCESS = 0;

	public static class Descriptor {}

	public static Descriptor[] queryEffects() {
		return new Descriptor[0];
	}

	public boolean getEnabled() { return false; }

	public int setEnabled(boolean enable) { return SUCCESS; }
}

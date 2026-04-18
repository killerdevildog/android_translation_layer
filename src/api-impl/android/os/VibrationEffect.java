package android.os;

public class VibrationEffect {

	public static VibrationEffect createOneShot(long milliseconds, int amplitude) {
		return new VibrationEffect();
	}

	public static VibrationEffect createWaveform(long[] pattern, int repeat) {
		return new VibrationEffect();
	}
}

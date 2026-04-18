package android.media;

public class MediaCodecInfo {

	private String name;
	private String mime;

	public MediaCodecInfo(String name, String mime) {
		this.name = name;
		this.mime = mime;
	}

	public String getName() {
		return name;
	}

	public boolean isEncoder() {
		return false;
	}

	public String[] getSupportedTypes() {
		return new String[] {mime};
	}

	public CodecCapabilities getCapabilitiesForType(String type) {
		return new CodecCapabilities();
	}

	public static class CodecCapabilities {

		public CodecProfileLevel[] profileLevels;

		public boolean isFeatureSupported(String feature) {
			System.out.println("CodecCapabilities.isFeatureSupported(" + feature + ")");
			return false;
		}

		public boolean isFeatureRequired(String feature) {
			System.out.println("CodecCapabilities.isFeatureRequired(" + feature + ")");
			return false;
		}

		public AudioCapabilities getAudioCapabilities() {
			return new AudioCapabilities();
		}
	}

	public static class CodecProfileLevel {}

	public static class AudioCapabilities {

		public boolean isSampleRateSupported(int sampleRate) {
			return true;
		}

		public int getMaxInputChannelCount() {
			return 2;
		}
	}
}

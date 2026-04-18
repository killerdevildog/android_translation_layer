package android.media;

public class AudioFormat {

	int sampleRate = 44100;
	int channelMask;
	int encoding;

	public static class Builder {

		private AudioFormat audioFormat = new AudioFormat();

		public Builder setSampleRate(int sampleRate) {
			audioFormat.sampleRate = sampleRate;
			return this;
		}

		public Builder setChannelMask(int channelMask) {
			audioFormat.channelMask = channelMask;
			return this;
		}

		public Builder setEncoding(int encoding) {
			audioFormat.encoding = encoding;
			return this;
		}

		public AudioFormat build() {
			return audioFormat;
		}
	}
}

package android.media;

import android.content.Context;

public class MediaRecorder {
	public class AudioEncoder {
		public static final int AAC = 3;
		public static final int OPUS = 7;
		public static final int VORBIS = 7;
	}

	public class AudioSource {
		public static final int DEFAULT = 0;
		public static final int MIC = 1;
	}

	public class MetricsConstants {}
	public class OutputFormat {
		public static final int AAC_ADTS = 6;
		public static final int DEFAULT = 0;
		public static final int OGG = 11;
	}
	public class VideoEncoder {}
	public class VideoSource {}

	public interface OnErrorListener {}
	public interface OnInfoListener {}

	public MediaRecorder() {}
	public MediaRecorder(Context context) {}

	private int audioSource;
	private int outputFormat;
	private int audioEncoder;

	public void setAudioSource(int audioSource) {
		this.audioSource = audioSource;
	}

	public void setOutputFormat(int outputFormat) {
		this.outputFormat = outputFormat;
	}

	public void setAudioEncoder(int audioEncoder) {
		this.audioEncoder = audioEncoder;
	}

	public int getMaxAmplitude() {
		return 0;
	}

	public void setAudioEncodingBitRate(int audioEncodingBitrate) {}
	public void setAudioSamplingRate(int setAudioSamplingRate) {}
	public void setOutputFile(String filePath) {}
	public void prepare() {}
	public void start() {}
	public void stop() {}
	public void resume() {}
	public void pause() {}
	public void release() {}
}

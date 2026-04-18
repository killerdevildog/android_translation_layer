package android.media;

import java.nio.ByteBuffer;

public class AudioTrack {
	public interface OnPlaybackPositionUpdateListener {
		void onMarkerReached(AudioTrack track);
		void onPeriodicNotification(AudioTrack track);
	}

	public static final int ERROR_BAD_VALUE = -2; // basically EINVAL

	public static final int PLAYSTATE_STOPPED = 1;
	public static final int PLAYSTATE_PAUSED = 2;
	public static final int PLAYSTATE_PLAYING = 3;

	int streamType;
	int sampleRateInHz;
	int channelConfig;
	int audioFormat;
	int bufferSizeInBytes;
	int mode;
	private int sessionId;
	private int playbackState = PLAYSTATE_STOPPED;
	private int playbackHeadPosition = 0;
	private float volume = 1.f;

	// for native code's use
	long pcm_handle;
	long params;
	int channels;
	int period_time;
	// mostly
	static int frames;
	OnPlaybackPositionUpdateListener periodic_update_listener;

	native void native_constructor(int streamType, int sampleRateInHz, int num_channels, int audioFormat, int bufferSizeInBytes, int mode);
	public AudioTrack(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes, int mode) {
		this.streamType = streamType;
		this.sampleRateInHz = sampleRateInHz;
		this.channelConfig = channelConfig;
		this.audioFormat = audioFormat;
		this.bufferSizeInBytes = bufferSizeInBytes;
		this.mode = mode;

		System.out.println("\n\n\nAudioTrack(" + streamType + ", " + sampleRateInHz + ", " + channelConfig + ", " + audioFormat + ", " + bufferSizeInBytes + ", " + mode + "); called\n\n\n\n");
		native_constructor(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode);
	}

	public AudioTrack(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes, int mode, int sessionId) {
		this(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes, mode);
		this.sessionId = sessionId;
	}

	public AudioTrack(AudioAttributes attributes, AudioFormat format, int bufferSizeInBytes, int mode, int sessionId) {
		this(attributes.streamType, format.sampleRate, format.channelMask, format.encoding, bufferSizeInBytes, mode, sessionId);
	}

	public static native int getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat);

	public void setPlaybackPositionUpdateListener(OnPlaybackPositionUpdateListener listener) {
		this.periodic_update_listener = listener;
	}

	public int setPositionNotificationPeriod(int periodInFrames) {
		System.out.println("\n\nAudioTrack.nsetPositionNotificationPeriod(" + periodInFrames + "); called\n\n\n\n");
		return 0; // SUCCESS
	}

	public int getPositionNotificationPeriod() {
		return this.frames;
	}

	public void play() {
		System.out.println("calling AudioTrack.play()\n");
		playbackState = PLAYSTATE_PLAYING;
		native_play();
	}

	public void stop() {
		System.out.println("STUB: AudioTrack.stop()\n");
		playbackState = PLAYSTATE_STOPPED;
	}

	public void flush() {
		System.out.println("STUB: AudioTrack.flush()\n");
	}

	public void release() {
		System.out.println("calling AudioTrack.release()\n");
		native_release();
	}

	public int getState() {
		return 1; // TODO: fix up the native part and make this work properly
	}

	public int write(byte[] audioData, int offsetInBytes, int sizeInBytes) {
		/* sanity check the parameters before calling native_write */
		if ((audioData == null)
		    || (offsetInBytes < 0) || (sizeInBytes < 0)
		    || (offsetInBytes + sizeInBytes < 0)
		    || (offsetInBytes + sizeInBytes > audioData.length)) {
			return ERROR_BAD_VALUE;
		}

		int framesToWrite = sizeInBytes / channels / 2; // 2 means PCM16
		int ret = native_write(audioData, offsetInBytes, framesToWrite, volume);
		if (ret > 0) {
			playbackHeadPosition += ret;
		}
		return ret * channels * 2; // 2 means PCM16
	}

	public int write(ByteBuffer audioData, int sizeInBytes, int writeMode) {
		int ret = write(audioData.array(), audioData.arrayOffset() + audioData.position(), sizeInBytes);
		audioData.position(audioData.position() + ret);
		return ret;
	}

	public int write(short audioData[], int offsetInShorts, int sizeInShorts) {
		/* sanity check the parameters before calling native_write */
		if ((audioData == null)
		    || (offsetInShorts < 0) || (sizeInShorts < 0)
		    || (offsetInShorts + sizeInShorts < 0)
		    || (offsetInShorts + sizeInShorts > audioData.length)) {
			return ERROR_BAD_VALUE;
		}

		int framesToWrite = sizeInShorts / channels;
		int ret = native_write(audioData, offsetInShorts, framesToWrite, volume);
		if (ret > 0) {
			playbackHeadPosition += ret;
		}
		return ret * channels;
	}

	public int getAudioSessionId() {
		return sessionId;
	}

	public int getSampleRate() {
		return sampleRateInHz;
	}

	public int setStereoVolume(float leftVolume, float rightVolume) {
		this.volume = (leftVolume + rightVolume) / 2;
		return 0;
	}

	public int getPlayState() {
		return playbackState;
	}

	public void pause() {
		System.out.println("calling AudioTrack.pause()\n");
		playbackState = PLAYSTATE_PAUSED;
		native_pause();
	}

	public int getPlaybackHeadPosition() {
		return playbackHeadPosition - native_getPlaybackHeadPosition();
	}

	public int setVolume(float volume) {
		this.volume = volume;
		return 0;
	}

	public boolean getTimestamp(AudioTimestamp timestamp) {
		return false;
	}

	private native int native_getPlaybackHeadPosition();
	public native void native_play();
	public native void native_pause();
	private native int native_write(byte[] audioData, int offsetInBytes, int framesToWrite, float volume);
	private native int native_write(short[] audioData, int offsetInShorts, int framesToWrite, float volume);
	public native void native_release();

	public static int getNativeOutputSampleRate(int i) {
		return -1;
	}

	// nested classes
	public static class Builder {
		private AudioAttributes mAttributes;
		private AudioFormat mFormat;
		private int mBufferSizeInBytes;
		private int mTransferMode;

		public Builder() {}

		public AudioTrack build() {
			return new AudioTrack(mAttributes, mFormat, mBufferSizeInBytes, mTransferMode, 0);
		}

		public Builder setAudioAttributes(AudioAttributes attributes) {
			mAttributes = attributes;
			return this;
		}

		public Builder setAudioFormat(AudioFormat format) {
			mFormat = format;
			return this;
		}

		public Builder setBufferSizeInBytes(int bufferSizeInBytes) {
			mBufferSizeInBytes = bufferSizeInBytes;
			return this;
		}

		public Builder setTransferMode(int mode) {
			mTransferMode = mode;
			return this;
		}

		public Builder setPerformanceMode(int performanceMode) {
			return this;
		}
	}
}

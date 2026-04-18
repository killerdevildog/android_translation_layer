package android.media;

public class AudioRecord {

	public static final int RECORDSTATE_STOPPED = 1;
	public static final int RECORDSTATE_RECORDING = 3;

	public static final int ERROR_BAD_VALUE = -2;

	private long pcm_handle;
	private int channels; // set by native constructor
	private int recordingState = RECORDSTATE_STOPPED;
	private int sampleRateInHz;

	private native long native_constructor(int streamType, int sampleRateInHz, int num_channels, int audioFormat, int bufferSizeInBytes);
	private native void native_record(long pcm_handle);
	private native void native_stop(long pcm_handle);
	private native int native_read(long pcm_handle, short[] audioData, int offsetInShorts, int framesToWrite);
	private native void native_release(long pcm_handle);

	public AudioRecord(int streamType, int sampleRateInHz, int channelConfig, int audioFormat, int bufferSizeInBytes) {
		this.sampleRateInHz = sampleRateInHz;
		pcm_handle = native_constructor(streamType, sampleRateInHz, channelConfig, audioFormat, bufferSizeInBytes);
	}

	public static native int getMinBufferSize(int sampleRateInHz, int channelConfig, int audioFormat);

	public int getSampleRate() {
		return sampleRateInHz;
	}

	public int getState() {
		return /*STATE_INITIALIZED*/ 1;
	}

	public int getRecordingState() {
		return recordingState;
	}

	public void startRecording() {
		native_record(pcm_handle);
		recordingState = RECORDSTATE_RECORDING;
	}

	public int read(short[] audioData, int offsetInShorts, int sizeInShorts) {
		/* sanity check the parameters before calling native_write */
		if ((audioData == null)
		    || (offsetInShorts < 0) || (sizeInShorts < 0)
		    || (offsetInShorts + sizeInShorts < 0)
		    || (offsetInShorts + sizeInShorts > audioData.length)) {
			return ERROR_BAD_VALUE;
		}

		return native_read(pcm_handle, audioData, offsetInShorts, sizeInShorts / channels) * channels;
	}

	public void stop() {
		native_stop(pcm_handle);
		recordingState = RECORDSTATE_STOPPED;
	}

	public void release() {
		native_release(pcm_handle);
		pcm_handle = 0;
	}

	public static class Builder {

		private int audioSource;
		private AudioFormat audioFormat;

		public Builder setAudioSource(int audioSource) {
			this.audioSource = audioSource;
			return this;
		}

		public Builder setAudioFormat(AudioFormat audioFormat) {
			this.audioFormat = audioFormat;
			return this;
		}

		public AudioRecord build() {
			return new AudioRecord(audioSource, audioFormat.sampleRate, audioFormat.channelMask, audioFormat.encoding, 32768);
		}
	}
}

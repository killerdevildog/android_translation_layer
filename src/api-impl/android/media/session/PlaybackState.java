package android.media.session;

import android.os.Bundle;

public class PlaybackState {

	public int state;
	public long position;
	public float playbackSpeed;
	public long updateTime;
	public long actions;
	public long activeQueueItemId;

	public static class Builder {

		PlaybackState state;

		public Builder() {
			state = new PlaybackState();
		}

		public Builder(PlaybackState from) {
			state = from;
		}

		public Builder setState(int state, long position, float playbackSpeed, long updateTime) {
			this.state.state = state;
			this.state.position = position;
			this.state.playbackSpeed = playbackSpeed;
			this.state.updateTime = updateTime;
			return this;
		}

		public Builder setBufferedPosition(long bufferedPosition) { return this; }

		public Builder setActions(long actions) {
			state.actions = actions;
			return this;
		}

		public Builder setErrorMessage(CharSequence errorMessage) { return this; }

		public Builder setActiveQueueItemId(long activeQueueItemId) {
			state.activeQueueItemId = activeQueueItemId;
			return this;
		}

		public Builder addCustomAction(CustomAction action) { return this; }

		public Builder setExtras(Bundle bundle) { return this; }

		public PlaybackState build() {
			return state;
		}
	}

	public static class CustomAction {

		public static class Builder {
			public Builder(String action, CharSequence label, int icon) {}

			public Builder setExtras(Bundle extras) { return this; }

			public CustomAction build() { return new CustomAction(); }
		}
	}
}

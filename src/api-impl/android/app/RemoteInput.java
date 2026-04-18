package android.app;

import android.os.Bundle;

public class RemoteInput {

	public static class Builder {

		public Builder(String resultKey) {}

		public Builder setLabel(CharSequence label) { return this; }

		public Builder setChoices(CharSequence[] choices) { return this; }

		public Builder setAllowFreeFormInput(boolean allowFreeFormInput) { return this; }

		public Builder addExtras(Bundle extras) { return this; }

		public RemoteInput build() {
			return new RemoteInput();
		}
	}
}

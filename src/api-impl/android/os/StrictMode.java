package android.os;

import java.util.concurrent.Executor;

public final class StrictMode {
	public static void setThreadPolicy(final ThreadPolicy policy) {}
	public static void setVmPolicy(final VmPolicy policy) {}
	public static VmPolicy getVmPolicy() { return new VmPolicy.Builder().build(); }
	public static ThreadPolicy allowThreadDiskWrites() {
		return new ThreadPolicy();
	}
	public static ThreadPolicy allowThreadDiskReads() {
		return new ThreadPolicy();
	}
	public static ThreadPolicy getThreadPolicy() {
		return new ThreadPolicy();
	}

	public static void noteSlowCall(String tag) {}

	public interface OnThreadViolationListener {
	}

	public static final class ThreadPolicy {
		public static final ThreadPolicy LAX;
		final int mask;
		final OnThreadViolationListener listener;
		final Executor callbackExecutor;

		private ThreadPolicy(int mask, OnThreadViolationListener listener, Executor executor) {
			this.mask = mask;
			this.listener = listener;
			this.callbackExecutor = executor;
		}

		private ThreadPolicy() {
			this.mask = 0;
			this.listener = new OnThreadViolationListener() {};
			this.callbackExecutor = new Executor() {
				@Override
				public void execute(Runnable command) {}
			};
		}

		public static final class Builder {
			private int mask = 0;
			private OnThreadViolationListener listener;
			private Executor executor;

			public Builder() {
				mask = 0;
			}

			public Builder(ThreadPolicy policy) {
				if (policy != null) {
					mask = policy.mask;
					listener = policy.listener;
					executor = policy.callbackExecutor;
				}
			}

			public Builder detectAll() {
				return this;
			}
			public Builder detectNetwork() {
				return this;
			}
			public Builder permitAll() {
				return this;
			}
			public Builder permitDiskReads() {
				return this;
			}
			public Builder permitDiskWrites() {
				return this;
			}
			public Builder detectResourceMismatches() {
				return this;
			}
			public Builder penaltyLog() {
				return this;
			}
			public Builder penaltyDeath() {
				return this;
			}
			public ThreadPolicy build() {
				return new ThreadPolicy(mask, listener, executor);
			}
			public Builder detectUnbufferedIo() {
				return this;
			}
			public Builder permitUnbufferedIo() {
				return this;
			}
		}
		static {
			LAX = (new Builder()).build();
		}
	}
	public static final class VmPolicy {
		public static final VmPolicy LAX;
		public static final class Builder {
			public Builder detectActivityLeaks() {
				return this;
			}
			public Builder detectAll() {
				return this;
			}
			public Builder detectLeakedSqlLiteObjects() {
				return this;
			}
			public Builder detectLeakedClosableObjects() {
				return this;
			}
			public Builder detectLeakedRegistrationObjects() {
				return this;
			}
			public Builder detectFileUriExposure() {
				return this;
			}
			public Builder penaltyDeath() {
				return this;
			}
			public Builder penaltyLog() {
				return this;
			}
			public Builder penaltyDropBox() {
				return this;
			}
			public VmPolicy build() {
				return new VmPolicy();
			}
		}

		static {
			LAX = (new Builder()).build();
		}
	}
}

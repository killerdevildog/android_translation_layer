package android.app.job;

import android.content.ComponentName;
import android.net.Uri;
import android.os.PersistableBundle;

public class JobInfo {

	private ComponentName service;
	long initialBackoffMillis;
	int backoffPolicy;
	private PersistableBundle extras;
	long periodicMillis;
	private int id;
	boolean running;
	long minLatencyMillis;

	public JobInfo() {}

	public ComponentName getService() {
		return service;
	}

	public PersistableBundle getExtras() {
		return extras;
	}

	public int getId() {
		return id;
	}

	public String toString() {
		return "JobInfo{"
		     + "jobService=" + service
		     + ", initialBackoffMillis=" + initialBackoffMillis
		     + ", backoffPolicy=" + backoffPolicy
		     + ", extras=" + extras
		     + ", periodicMillis=" + periodicMillis
		     + ", id=" + id
		     + '}';
	}

	public static final class Builder {

		private JobInfo jobInfo;

		public Builder(int jobId, ComponentName jobService) {
			jobInfo = new JobInfo();
			jobInfo.id = jobId;
			jobInfo.service = jobService;
		}

		public Builder setBackoffCriteria(long initialBackoffMillis, int backoffPolicy) {
			jobInfo.initialBackoffMillis = initialBackoffMillis;
			jobInfo.backoffPolicy = backoffPolicy;
			return this;
		}

		public Builder setExtras(PersistableBundle extras) {
			jobInfo.extras = extras;
			return this;
		}

		public Builder setMinimumLatency(long minLatencyMillis) {
			jobInfo.minLatencyMillis = minLatencyMillis;
			return this;
		}

		public Builder setOverrideDeadline(long a) {
			return this;
		}

		public Builder setPeriodic(long dummy) {
			jobInfo.periodicMillis = dummy;
			return this;
		}

		public Builder setPersisted(boolean persisted) {
			return this;
		}

		public Builder setRequiredNetworkType(int networkType) {
			return this;
		}

		public Builder setRequiresCharging(boolean requires_charging) {
			return this;
		}

		public Builder setRequiresDeviceIdle(boolean requires_device_idle) {
			return this;
		}

		public Builder setRequiresBatteryNotLow(boolean requires_battery_not_low) {
			return this;
		}

		public Builder setRequiresStorageNotLow(boolean requires_storage_not_low) {
			return this;
		}

		public Builder addTriggerContentUri(TriggerContentUri triggerContentUri) {
			return this;
		}

		public JobInfo build() {
			return jobInfo;
		}
	}

	public static class TriggerContentUri {
		public TriggerContentUri(Uri uri, int flags) {}
	}
}

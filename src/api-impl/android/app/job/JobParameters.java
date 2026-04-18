package android.app.job;

import android.net.Uri;
import android.os.Parcelable;
import android.os.PersistableBundle;

public class JobParameters implements Parcelable {

	public static final Creator<JobParameters> CREATOR = null;

	JobInfo jobInfo;

	JobParameters(JobInfo jobInfo) {
		this.jobInfo = jobInfo;
	}

	public PersistableBundle getExtras() {
		return jobInfo.getExtras();
	}

	public Uri[] getTriggeredContentUris() {
		return new Uri[0];
	}

	public String[] getTriggeredContentAuthorities() {
		return new String[0];
	}
}

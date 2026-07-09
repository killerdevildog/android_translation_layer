package android.app.job;

import android.atl.ATLLoadedApp;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Slog;
import java.util.*;

public class JobScheduler {
	private static final String TAG = "JobScheduler";

	static Map<Integer, JobInfo> pendingJobs = new HashMap<>();
	private static IdentityHashMap<Class<? extends JobService>, JobService> runningServices = new IdentityHashMap<>();

	private final Context context;

	public JobScheduler(Context context) {
		this.context = context;
	}

	/**
	 * Retrieve all jobs that have been scheduled by the calling application.
	 *
	 * @return a list of all of the app's scheduled jobs.  This includes jobs that are
	 *     currently started as well as those that are still waiting to run.
	 */
	public List<JobInfo> getAllPendingJobs() {
		return new ArrayList<>(pendingJobs.values());
	};

	public int enqueue(JobInfo job, JobWorkItem work) {
		return 1; //RESULT_SUCCESS
	}

	public int schedule(JobInfo job) {
		Slog.i(TAG, "JobScheduler.schedule() called with job: " + job);
		if (pendingJobs.containsKey(job.getId()))
			return 1; //RESULT_SUCCESS
		pendingJobs.put(job.getId(), job);
		new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					String className = job.getService().getClassName();
					Class<? extends JobService> cls = context.get_atl_loaded_app()
					                                      .loadClass(className)
					                                      .asSubclass(JobService.class);
					if (!runningServices.containsKey(cls)) {
						JobService service = cls.getConstructor().newInstance();
						ATLLoadedApp atlLoadedApp = context.get_atl_loaded_app();
						service.attachBaseContext(atlLoadedApp.createContext(null, null,
						                                                     atlLoadedApp.pkg.applicationInfo.theme));
						service.onCreate();
						runningServices.put(cls, service);
					}
					job.running = true;
					boolean result = runningServices.get(cls).onStartJob(new JobParameters(job));
					Slog.i(TAG, "onStartJob() returned " + result);
				} catch (ReflectiveOperationException e) {
					e.printStackTrace();
				}
			}
		}, job.minLatencyMillis);
		return 1; //RESULT_SUCCESS
	}

	public void cancel(int id) {
		JobInfo job = pendingJobs.remove(id);
		if (job != null && job.running) {
			new Handler(Looper.getMainLooper()).post(new Runnable() {
				@Override
				public void run() {
					JobService service = runningServices.get(job.getService().getClass());
					if (service != null) {
						JobParameters params = new JobParameters(job);
						service.onStopJob(params);
						job.running = false;
					}
				}
			});
		}
	}
}

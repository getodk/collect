package org.odk.collect.android.backgroundwork;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;

import org.odk.collect.android.formmanagement.SyncFormsTaskSpec;
import org.odk.collect.android.tasks.ServerPollingJob;
import org.odk.collect.android.upload.AutoSendWorker;
import org.odk.collect.async.Scheduler;

import java.util.Set;

/**
 * Abstraction that sits on top of {@link com.evernote.android.job.JobManager} and
 * {@link androidx.work.WorkManager}. Implementation of {@link BackgroundWorkManager} which objects
 * can use to interact with background work without having to care what underlying framework is
 * being used.
 */
public class JobManagerAndSchedulerBackgroundWorkManager implements BackgroundWorkManager {

    private static final String MATCH_EXACTLY_SYNC_TAG = "match_exactly";

    private final Scheduler scheduler;

    public JobManagerAndSchedulerBackgroundWorkManager(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void scheduleMatchExactlySync() {
        scheduler.scheduleInBackgroundWhenNetworkAvailable(MATCH_EXACTLY_SYNC_TAG, new SyncFormsTaskSpec(), 900000L);
    }

    @Override
    public void cancelMatchExactlySync() {
        scheduler.cancelInBackground(MATCH_EXACTLY_SYNC_TAG);
    }

    @Override
    public boolean isFormUploaderRunning() {
        return isRunning(AutoSendWorker.TAG);
    }

    @Override
    public boolean isFormDownloaderRunning() {
        return isRunning(ServerPollingJob.TAG);
    }

    private boolean isRunning(String tag) {
        if (ServerPollingJob.TAG.equals(tag)) {
            return isJobManagerWorkRunning(tag);
        }

        return scheduler.isRunning(tag);
    }

    private boolean isJobManagerWorkRunning(String tag) {
        Set<Job> jobs = JobManager.instance().getAllJobsForTag(tag);
        for (Job job : jobs) {
            if (!job.isFinished()) {
                return true;
            }
        }
        return false;
    }

}

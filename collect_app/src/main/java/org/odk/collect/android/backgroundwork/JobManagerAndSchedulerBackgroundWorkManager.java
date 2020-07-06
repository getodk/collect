package org.odk.collect.android.backgroundwork;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;

import org.odk.collect.android.tasks.ServerPollingJob;
import org.odk.collect.async.Scheduler;
import org.odk.collect.utilities.BackgroundWorkManager;

import java.util.Set;

/**
 * Abstraction that sits on top of {@link com.evernote.android.job.JobManager} and
 * {@link androidx.work.WorkManager}. Implementation of {@link BackgroundWorkManager} which objects
 * can use to interact with background work without having to care what underlying framework is
 * being used.
 */
public class JobManagerAndSchedulerBackgroundWorkManager implements BackgroundWorkManager {

    private final Scheduler scheduler;

    public JobManagerAndSchedulerBackgroundWorkManager(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public boolean isRunning(String tag) {
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

package org.odk.collect.android.backgroundwork;

import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.google.common.util.concurrent.ListenableFuture;

import org.odk.collect.android.tasks.ServerPollingJob;
import org.odk.collect.android.upload.AutoSendWorker;
import org.odk.collect.utilities.BackgroundWorkManager;

import java.util.List;
import java.util.Set;

import timber.log.Timber;

/**
 * Abstraction that sits on top of {@link com.evernote.android.job.JobManager} and
 * {@link androidx.work.WorkManager}. Implementation of {@link BackgroundWorkManager} which objects
 * can use to interact with background work without having to care what underlying framework is
 * being used.
 */
public class CollectBackgroundWorkManager implements BackgroundWorkManager {

    @Override
    public boolean isRunning(String tag) {
        switch (tag) {
            case AutoSendWorker.TAG: {
                return isWorkManagerWorkRunning(tag);
            }

            case ServerPollingJob.TAG: {
                return isJobManagerWorkRunning(tag);
            }

            default:
                throw new IllegalArgumentException("CollectBackgroundWorkManager doesn't know about " + tag);
        }
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

    private boolean isWorkManagerWorkRunning(String tag) {
        ListenableFuture<List<WorkInfo>> statuses = WorkManager.getInstance().getWorkInfosByTag(tag);
        try {
            for (WorkInfo workInfo : statuses.get()) {
                if (workInfo.getState() == WorkInfo.State.RUNNING) {
                    return true;
                }
            }
        } catch (Exception | Error e) {
            Timber.w(e);
        }

        return false;
    }
}

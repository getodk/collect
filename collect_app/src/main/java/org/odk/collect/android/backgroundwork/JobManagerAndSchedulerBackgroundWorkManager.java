package org.odk.collect.android.backgroundwork;

import android.content.Context;

import androidx.work.WorkerParameters;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.formmanagement.DiskFormsSynchronizer;
import org.odk.collect.android.formmanagement.FormDownloader;
import org.odk.collect.android.formmanagement.ServerFormListSynchronizer;
import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.forms.MediaFileRepository;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.openrosa.api.FormListApi;
import org.odk.collect.android.tasks.ServerPollingJob;
import org.odk.collect.android.upload.AutoSendWorker;
import org.odk.collect.async.Scheduler;
import org.odk.collect.async.TaskSpec;
import org.odk.collect.async.WorkerAdapter;

import java.util.Set;

import javax.inject.Inject;

import timber.log.Timber;

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
        scheduler.scheduleInBackgroundWhenNetworkAvailable(MATCH_EXACTLY_SYNC_TAG, new SyncTaskSpec(), 900000L);
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

    public static class SyncTaskSpec implements TaskSpec {

        @Inject
        FormRepository formRepository;

        @Inject
        MediaFileRepository mediaFileRepository;

        @Inject
        FormListApi formAPI;

        @Inject
        FormDownloader formDownloader;

        @NotNull
        @Override
        public Runnable getTask(@NotNull Context context) {
            DaggerUtils.getComponent(context).inject(this);

            return () -> {
                try {
                    new ServerFormListSynchronizer(formRepository, mediaFileRepository, formAPI, formDownloader, new DiskFormsSynchronizer()).synchronize();
                } catch (FormApiException formAPIError) {
                    Timber.e(formAPIError);
                }
            };
        }

        @NotNull
        @Override
        public Class<? extends WorkerAdapter> getWorkManagerAdapter() {
            return Adapter.class;
        }

        public static class Adapter extends WorkerAdapter {

            public Adapter(@NotNull Context context, @NotNull WorkerParameters workerParams) {
                super(new SyncTaskSpec(), context, workerParams);
            }
        }
    }
}

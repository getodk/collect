package org.odk.collect.android.backgroundwork;

import android.content.Context;

import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.formmanagement.SyncFormsTaskSpec;
import org.odk.collect.android.tasks.ServerPollingJob;
import org.odk.collect.android.upload.AutoSendWorker;
import org.odk.collect.async.Scheduler;
import org.odk.collect.async.TaskSpec;
import org.odk.collect.async.WorkerAdapter;

/**
 * Abstraction that sits on top of {@link Scheduler}. Implementation of {@link BackgroundWorkManager} which
 * can be used to interact with background work without having to care about underlying framework and details.
 */
public class SchedulerBackgroundWorkManager implements BackgroundWorkManager {

    private static final String MATCH_EXACTLY_SYNC_TAG = "match_exactly";

    private final Scheduler scheduler;

    public SchedulerBackgroundWorkManager(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void scheduleMatchExactlySync(long repeatPeriod) {
        scheduler.networkDeferred(MATCH_EXACTLY_SYNC_TAG, new SyncFormsTaskSpec(), repeatPeriod);
    }

    @Override
    public void scheduleAutoUpdate(long repeatPeriod) {
        scheduler.networkDeferred(ServerPollingJob.TAG, new AutoUpdateTaskSpec(), repeatPeriod);
    }

    @Override
    public void cancelWork() {
        scheduler.cancelDeferred(MATCH_EXACTLY_SYNC_TAG);
        scheduler.cancelDeferred(ServerPollingJob.TAG);
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
        return scheduler.isRunning(tag);
    }

    private static class AutoUpdateTaskSpec implements TaskSpec {

        @NotNull
        @Override
        public Runnable getTask(@NotNull Context context) {
            return () -> {
                ServerPollingJob serverPollingJob = new ServerPollingJob();
                serverPollingJob.onRunJob();
            };
        }

        @NotNull
        @Override
        public Class<? extends WorkerAdapter> getWorkManagerAdapter() {
            return Adapter.class;
        }

        public static class Adapter extends WorkerAdapter {

            Adapter(@NotNull Context context, @NotNull WorkerParameters workerParams) {
                super(new AutoUpdateTaskSpec(), context, workerParams);
            }
        }
    }
}

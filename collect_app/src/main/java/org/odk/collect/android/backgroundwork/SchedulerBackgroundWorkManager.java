package org.odk.collect.android.backgroundwork;

import org.odk.collect.android.formmanagement.previouslydownloaded.AutoUpdateTaskSpec;
import org.odk.collect.android.formmanagement.matchexactly.SyncFormsTaskSpec;
import org.odk.collect.android.upload.AutoSendWorker;
import org.odk.collect.async.Scheduler;

/**
 * Abstraction that sits on top of {@link Scheduler}. Implementation of {@link BackgroundWorkManager} which
 * can be used to interact with background work without having to care about underlying framework and details.
 */
public class SchedulerBackgroundWorkManager implements BackgroundWorkManager {

    private static final String MATCH_EXACTLY_SYNC_TAG = "match_exactly";
    public static final String AUTO_UPDATE_TAG = "serverPollingJob";

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
        scheduler.networkDeferred(AUTO_UPDATE_TAG, new AutoUpdateTaskSpec(), repeatPeriod);
    }

    @Override
    public void cancelWork() {
        scheduler.cancelDeferred(MATCH_EXACTLY_SYNC_TAG);
        scheduler.cancelDeferred(AUTO_UPDATE_TAG);
    }

    @Override
    public boolean isFormUploaderRunning() {
        return isRunning(AutoSendWorker.TAG);
    }

    @Override
    public boolean isFormDownloaderRunning() {
        return isRunning(AUTO_UPDATE_TAG);
    }

    private boolean isRunning(String tag) {
        return scheduler.isRunning(tag);
    }
}

package org.odk.collect.android.backgroundwork;

import android.app.Application;
import android.content.SharedPreferences;

import org.odk.collect.android.formmanagement.FormUpdateMode;
import org.odk.collect.android.formmanagement.previouslydownloaded.AutoUpdateTaskSpec;
import org.odk.collect.android.formmanagement.matchexactly.SyncFormsTaskSpec;
import org.odk.collect.android.upload.AutoSendWorker;
import org.odk.collect.async.Scheduler;

import static org.odk.collect.android.backgroundwork.BackgroundWorkUtils.getPeriodInMilliseconds;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_FORM_UPDATE_MODE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;

/**
 * Abstraction that sits on top of {@link Scheduler}. Implementation of {@link FormUpdateManager} which
 * can be used to interact with background work without having to care about underlying framework and details.
 */
public class SchedulerFormUpdateManager implements FormUpdateManager {

    private static final String MATCH_EXACTLY_SYNC_TAG = "match_exactly";
    public static final String AUTO_UPDATE_TAG = "serverPollingJob";

    private final Scheduler scheduler;
    private final SharedPreferences sharedPreferences;
    private final Application application;

    public SchedulerFormUpdateManager(Scheduler scheduler, SharedPreferences sharedPreferences, Application application) {
        this.scheduler = scheduler;
        this.sharedPreferences = sharedPreferences;
        this.application = application;
    }

    @Override
    public void scheduleUpdates() {
        cancelWork();

        String newValue = sharedPreferences.getString(KEY_FORM_UPDATE_MODE, null);
        String period = sharedPreferences.getString(KEY_PERIODIC_FORM_UPDATES_CHECK, null);

        switch (FormUpdateMode.parse(application, newValue)) {
            case MANUAL:
                break;
            case PREVIOUSLY_DOWNLOADED_ONLY:
                scheduleAutoUpdate(getPeriodInMilliseconds(period));
                break;
            case MATCH_EXACTLY:
                scheduleMatchExactlySync(getPeriodInMilliseconds(period));
                break;
        }
    }

    private void scheduleMatchExactlySync(long repeatPeriod) {
        scheduler.networkDeferred(MATCH_EXACTLY_SYNC_TAG, new SyncFormsTaskSpec(), repeatPeriod);
    }

    private void scheduleAutoUpdate(long repeatPeriod) {
        scheduler.networkDeferred(AUTO_UPDATE_TAG, new AutoUpdateTaskSpec(), repeatPeriod);
    }

    private void cancelWork() {
        scheduler.cancelDeferred(MATCH_EXACTLY_SYNC_TAG);
        scheduler.cancelDeferred(AUTO_UPDATE_TAG);
    }

    @Override
    public boolean isFormUploaderRunning() {
        return isRunning(AutoSendWorker.TAG);
    }

    @Override
    public boolean isUpdateRunning() {
        return isRunning(MATCH_EXACTLY_SYNC_TAG) || isRunning(AUTO_UPDATE_TAG);
    }

    private boolean isRunning(String tag) {
        return scheduler.isRunning(tag);
    }
}

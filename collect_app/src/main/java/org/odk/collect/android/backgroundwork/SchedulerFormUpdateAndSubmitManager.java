package org.odk.collect.android.backgroundwork;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import org.odk.collect.android.formmanagement.FormUpdateMode;
import org.odk.collect.android.upload.AutoSendWorker;
import org.odk.collect.async.Scheduler;

import static org.odk.collect.android.backgroundwork.BackgroundWorkUtils.getPeriodInMilliseconds;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_FORM_UPDATE_MODE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;

public class SchedulerFormUpdateAndSubmitManager implements FormUpdateManager, FormSubmitManager {

    private static final String MATCH_EXACTLY_SYNC_TAG = "match_exactly";
    public static final String AUTO_UPDATE_TAG = "serverPollingJob";

    private final Scheduler scheduler;
    private final SharedPreferences sharedPreferences;
    private final Application application;

    @Deprecated // Should use Scheduler instance instead
    private final WorkManager workManager;

    public SchedulerFormUpdateAndSubmitManager(Scheduler scheduler, SharedPreferences sharedPreferences, Application application, WorkManager workManager) {
        this.scheduler = scheduler;
        this.sharedPreferences = sharedPreferences;
        this.application = application;
        this.workManager = workManager;
    }

    @Override
    public void scheduleUpdates() {
        scheduler.cancelDeferred(MATCH_EXACTLY_SYNC_TAG);
        scheduler.cancelDeferred(AUTO_UPDATE_TAG);

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

    @Override
    public boolean isSubmitRunning() {
        return isRunning(AutoSendWorker.TAG);
    }

    @Override
    public void scheduleSubmit() {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest autoSendWork =
                new OneTimeWorkRequest.Builder(AutoSendWorker.class)
                        .addTag(AutoSendWorker.TAG)
                        .setConstraints(constraints)
                        .build();
        workManager.beginUniqueWork(AutoSendWorker.TAG,
                ExistingWorkPolicy.KEEP, autoSendWork).enqueue();
    }

    @Override
    public boolean isUpdateRunning() {
        return isRunning(MATCH_EXACTLY_SYNC_TAG) || isRunning(AUTO_UPDATE_TAG);
    }

    private boolean isRunning(String tag) {
        return scheduler.isRunning(tag);
    }
}

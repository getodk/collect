package org.odk.collect.android.backgroundwork;

import android.app.Application;
import android.content.SharedPreferences;

import org.odk.collect.android.formmanagement.FormUpdateMode;
import org.odk.collect.async.Scheduler;

import static org.odk.collect.android.backgroundwork.BackgroundWorkUtils.getPeriodInMilliseconds;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_FORM_UPDATE_MODE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;

public class SchedulerFormUpdateAndSubmitManager implements FormUpdateManager, FormSubmitManager {

    private static final String MATCH_EXACTLY_SYNC_TAG = "match_exactly";
    private static final String AUTO_UPDATE_TAG = "serverPollingJob";
    public static final String AUTO_SEND_TAG = "AutoSendWorker";

    private final Scheduler scheduler;
    private final SharedPreferences sharedPreferences;
    private final Application application;

    public SchedulerFormUpdateAndSubmitManager(Scheduler scheduler, SharedPreferences sharedPreferences, Application application) {
        this.scheduler = scheduler;
        this.sharedPreferences = sharedPreferences;
        this.application = application;
    }

    @Override
    public void scheduleUpdates() {
        String newValue = sharedPreferences.getString(KEY_FORM_UPDATE_MODE, null);
        String period = sharedPreferences.getString(KEY_PERIODIC_FORM_UPDATES_CHECK, null);
        long periodInMilliseconds = getPeriodInMilliseconds(period);

        switch (FormUpdateMode.parse(application, newValue)) {
            case MANUAL:
                scheduler.cancelDeferred(MATCH_EXACTLY_SYNC_TAG);
                scheduler.cancelDeferred(AUTO_UPDATE_TAG);
                break;
            case PREVIOUSLY_DOWNLOADED_ONLY:
                scheduler.cancelDeferred(MATCH_EXACTLY_SYNC_TAG);
                scheduler.networkDeferred(AUTO_UPDATE_TAG, new AutoUpdateTaskSpec(), periodInMilliseconds);
                break;
            case MATCH_EXACTLY:
                scheduler.cancelDeferred(AUTO_UPDATE_TAG);
                scheduler.networkDeferred(MATCH_EXACTLY_SYNC_TAG, new SyncFormsTaskSpec(), periodInMilliseconds);
                break;
        }
    }

    @Override
    public void scheduleSubmit() {
        scheduler.networkDeferred(AUTO_SEND_TAG, new AutoSendTaskSpec());
    }
}

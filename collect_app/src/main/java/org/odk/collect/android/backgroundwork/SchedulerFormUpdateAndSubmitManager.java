package org.odk.collect.android.backgroundwork;

import android.app.Application;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.preferences.Protocol;
import org.odk.collect.android.preferences.keys.MetaKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.async.Scheduler;
import org.odk.collect.shared.Settings;

import java.util.HashMap;

import static java.util.Collections.emptyMap;
import static org.odk.collect.android.backgroundwork.BackgroundWorkUtils.getPeriodInMilliseconds;
import static org.odk.collect.android.configure.SettingsUtils.getFormUpdateMode;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_PROTOCOL;

public class SchedulerFormUpdateAndSubmitManager implements FormUpdateManager, FormSubmitManager {

    private static final String MATCH_EXACTLY_SYNC_TAG = "match_exactly";
    public static final String AUTO_SEND_TAG = "AutoSendWorker";

    private final Scheduler scheduler;
    private final SettingsProvider settingsProvider;
    private final Application application;

    public SchedulerFormUpdateAndSubmitManager(Scheduler scheduler, SettingsProvider settingsProvider, Application application) {
        this.scheduler = scheduler;
        this.settingsProvider = settingsProvider;
        this.application = application;
    }

    @Override
    public void scheduleUpdates() {
        Settings generalSettings = settingsProvider.getGeneralSettings();

        String protocol = generalSettings.getString(KEY_PROTOCOL);
        if (Protocol.parse(application, protocol) == Protocol.GOOGLE) {
            scheduler.cancelDeferred(MATCH_EXACTLY_SYNC_TAG);
            scheduler.cancelDeferred(getAutoUpdateTag());
            return;
        }

        String period = generalSettings.getString(KEY_PERIODIC_FORM_UPDATES_CHECK);
        long periodInMilliseconds = getPeriodInMilliseconds(period, application);

        switch (getFormUpdateMode(application, generalSettings)) {
            case MANUAL:
                scheduler.cancelDeferred(MATCH_EXACTLY_SYNC_TAG);
                scheduler.cancelDeferred(getAutoUpdateTag());
                break;
            case PREVIOUSLY_DOWNLOADED_ONLY:
                scheduler.cancelDeferred(MATCH_EXACTLY_SYNC_TAG);

                HashMap<String, String> inputData = new HashMap<>();
                inputData.put(AutoUpdateTaskSpec.DATA_PROJECT_ID, currentProjectId());
                scheduler.networkDeferred(getAutoUpdateTag(), new AutoUpdateTaskSpec(), periodInMilliseconds, inputData);
                break;
            case MATCH_EXACTLY:
                scheduler.cancelDeferred(getAutoUpdateTag());
                scheduler.networkDeferred(MATCH_EXACTLY_SYNC_TAG, new SyncFormsTaskSpec(), periodInMilliseconds, emptyMap());
                break;
        }
    }

    @Override
    public void scheduleSubmit() {
        scheduler.networkDeferred(AUTO_SEND_TAG, new AutoSendTaskSpec());
    }

    @NotNull
    private String getAutoUpdateTag() {
        return "serverPollingJob:" + currentProjectId();
    }

    private String currentProjectId() {
        return settingsProvider.getMetaSettings().getString(MetaKeys.CURRENT_PROJECT_ID);
    }
}

package org.odk.collect.android.backgroundwork;

import android.app.Application;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.preferences.Protocol;
import org.odk.collect.android.preferences.keys.MetaKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.async.Scheduler;
import org.odk.collect.shared.Settings;

import java.util.HashMap;

import static org.odk.collect.android.backgroundwork.BackgroundWorkUtils.getPeriodInMilliseconds;
import static org.odk.collect.android.configure.SettingsUtils.getFormUpdateMode;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_PROTOCOL;

public class FormUpdateAndInstanceSubmitScheduler implements FormUpdateScheduler, InstanceSubmitScheduler {

    public static final String AUTO_SEND_TAG = "AutoSendWorker";

    private final Scheduler scheduler;
    private final SettingsProvider settingsProvider;
    private final Application application;

    public FormUpdateAndInstanceSubmitScheduler(Scheduler scheduler, SettingsProvider settingsProvider, Application application) {
        this.scheduler = scheduler;
        this.settingsProvider = settingsProvider;
        this.application = application;
    }

    @Override
    public void scheduleUpdates() {
        Settings generalSettings = settingsProvider.getGeneralSettings();

        String protocol = generalSettings.getString(KEY_PROTOCOL);
        if (Protocol.parse(application, protocol) == Protocol.GOOGLE) {
            scheduler.cancelDeferred(getMatchExactlyTag());
            scheduler.cancelDeferred(getAutoUpdateTag());
            return;
        }

        String period = generalSettings.getString(KEY_PERIODIC_FORM_UPDATES_CHECK);
        long periodInMilliseconds = getPeriodInMilliseconds(period, application);

        switch (getFormUpdateMode(application, generalSettings)) {
            case MANUAL:
                scheduler.cancelDeferred(getMatchExactlyTag());
                scheduler.cancelDeferred(getAutoUpdateTag());
                break;
            case PREVIOUSLY_DOWNLOADED_ONLY:
                scheduler.cancelDeferred(getMatchExactlyTag());
                scheduleAutoUpdate(periodInMilliseconds);
                break;
            case MATCH_EXACTLY:
                scheduler.cancelDeferred(getAutoUpdateTag());
                scheduleMatchExactly(periodInMilliseconds);
                break;
        }
    }

    private void scheduleAutoUpdate(long periodInMilliseconds) {
        HashMap<String, String> inputData = new HashMap<>();
        inputData.put(AutoUpdateTaskSpec.DATA_PROJECT_ID, currentProjectId());
        scheduler.networkDeferred(getAutoUpdateTag(), new AutoUpdateTaskSpec(), periodInMilliseconds, inputData);
    }

    private void scheduleMatchExactly(long periodInMilliseconds) {
        HashMap<String, String> inputData = new HashMap<>();
        inputData.put(SyncFormsTaskSpec.DATA_PROJECT_ID, currentProjectId());
        scheduler.networkDeferred(getMatchExactlyTag(), new SyncFormsTaskSpec(), periodInMilliseconds, inputData);
    }

    @Override
    public void cancelUpdates() {
        scheduler.cancelDeferred(getAutoUpdateTag());
        scheduler.cancelDeferred(getMatchExactlyTag());
    }

    @Override
    public void scheduleSubmit() {
        scheduler.networkDeferred(AUTO_SEND_TAG, new AutoSendTaskSpec());
    }

    @NotNull
    private String getAutoUpdateTag() {
        return "serverPollingJob:" + currentProjectId();
    }

    @NotNull
    private String getMatchExactlyTag() {
        return "match_exactly:" + currentProjectId();
    }

    private String currentProjectId() {
        return settingsProvider.getMetaSettings().getString(MetaKeys.CURRENT_PROJECT_ID);
    }
}

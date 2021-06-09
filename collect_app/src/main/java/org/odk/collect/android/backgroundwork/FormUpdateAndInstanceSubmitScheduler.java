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

    private final Scheduler scheduler;
    private final SettingsProvider settingsProvider;
    private final Application application;

    public FormUpdateAndInstanceSubmitScheduler(Scheduler scheduler, SettingsProvider settingsProvider, Application application) {
        this.scheduler = scheduler;
        this.settingsProvider = settingsProvider;
        this.application = application;
    }

    @Override
    public void scheduleUpdates(String projectId) {
        Settings generalSettings = settingsProvider.getGeneralSettings(projectId);

        String protocol = generalSettings.getString(KEY_PROTOCOL);
        if (Protocol.parse(application, protocol) == Protocol.GOOGLE) {
            scheduler.cancelDeferred(getMatchExactlyTag(projectId));
            scheduler.cancelDeferred(getAutoUpdateTag(projectId));
            return;
        }

        String period = generalSettings.getString(KEY_PERIODIC_FORM_UPDATES_CHECK);
        long periodInMilliseconds = getPeriodInMilliseconds(period, application);

        switch (getFormUpdateMode(application, generalSettings)) {
            case MANUAL:
                scheduler.cancelDeferred(getMatchExactlyTag(projectId));
                scheduler.cancelDeferred(getAutoUpdateTag(projectId));
                break;
            case PREVIOUSLY_DOWNLOADED_ONLY:
                scheduler.cancelDeferred(getMatchExactlyTag(projectId));
                scheduleAutoUpdate(periodInMilliseconds, projectId);
                break;
            case MATCH_EXACTLY:
                scheduler.cancelDeferred(getAutoUpdateTag(projectId));
                scheduleMatchExactly(periodInMilliseconds, projectId);
                break;
        }
    }

    private void scheduleAutoUpdate(long periodInMilliseconds, String projectId) {
        HashMap<String, String> inputData = new HashMap<>();
        inputData.put(AutoUpdateTaskSpec.DATA_PROJECT_ID, projectId);
        scheduler.networkDeferred(getAutoUpdateTag(projectId), new AutoUpdateTaskSpec(), periodInMilliseconds, inputData);
    }

    private void scheduleMatchExactly(long periodInMilliseconds, String projectId) {
        HashMap<String, String> inputData = new HashMap<>();
        inputData.put(SyncFormsTaskSpec.DATA_PROJECT_ID, projectId);
        scheduler.networkDeferred(getMatchExactlyTag(projectId), new SyncFormsTaskSpec(), periodInMilliseconds, inputData);
    }

    @Override
    public void cancelUpdates(String projectId) {
        scheduler.cancelDeferred(getAutoUpdateTag(projectId));
        scheduler.cancelDeferred(getMatchExactlyTag(projectId));
    }

    @Override
    public void scheduleSubmit() {
        HashMap<String, String> inputData = new HashMap<>();
        inputData.put(AutoSendTaskSpec.DATA_PROJECT_ID, currentProjectId());
        scheduler.networkDeferred(getAutoSendTag(), new AutoSendTaskSpec(), inputData);
    }

    @Override
    public void cancelSubmit() {
        scheduler.cancelDeferred(getAutoSendTag());
    }

    @NotNull
    public String getAutoSendTag() {
        return "AutoSendWorker:" + currentProjectId();
    }

    @NotNull
    private String getMatchExactlyTag(String projectId) {
        return "match_exactly:" + projectId;
    }

    private String getAutoUpdateTag(String projectId) {
        return "serverPollingJob:" + projectId;
    }

    private String currentProjectId() {
        return settingsProvider.getMetaSettings().getString(MetaKeys.CURRENT_PROJECT_ID);
    }
}

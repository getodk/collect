package org.odk.collect.android.backgroundwork;

import android.app.Application;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.preferences.keys.ProjectKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.async.Scheduler;
import org.odk.collect.shared.Settings;

import java.util.HashMap;

import static org.odk.collect.android.backgroundwork.BackgroundWorkUtils.getPeriodInMilliseconds;
import static org.odk.collect.android.configure.SettingsUtils.getFormUpdateMode;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_PROTOCOL;

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
        Settings generalSettings = settingsProvider.getUnprotectedSettings(projectId);

        String protocol = generalSettings.getString(KEY_PROTOCOL);
        if (protocol.equals(ProjectKeys.PROTOCOL_GOOGLE_SHEETS)) {
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
    public void scheduleSubmit(String projectId) {
        HashMap<String, String> inputData = new HashMap<>();
        inputData.put(AutoSendTaskSpec.DATA_PROJECT_ID, projectId);
        scheduler.networkDeferred(getAutoSendTag(projectId), new AutoSendTaskSpec(), inputData);
    }

    @Override
    public void cancelSubmit(String projectId) {
        scheduler.cancelDeferred(getAutoSendTag(projectId));
    }

    @NotNull
    public String getAutoSendTag(String projectId) {
        return "AutoSendWorker:" + projectId;
    }

    @NotNull
    private String getMatchExactlyTag(String projectId) {
        return "match_exactly:" + projectId;
    }

    @NotNull
    private String getAutoUpdateTag(String projectId) {
        return "serverPollingJob:" + projectId;
    }
}

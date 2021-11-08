package org.odk.collect.android.activities.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.configure.SettingsUtils;
import org.odk.collect.android.formmanagement.InstancesAppState;
import org.odk.collect.android.preferences.FormUpdateMode;
import org.odk.collect.android.preferences.keys.ProtectedProjectKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.instancemanagement.InstanceDiskSynchronizer;
import org.odk.collect.android.version.VersionInformation;
import org.odk.collect.async.Scheduler;
import org.odk.collect.shared.Settings;

public class MainMenuViewModel extends ViewModel {

    private final VersionInformation version;
    private final Settings generalSettings;
    private final Settings adminSettings;
    private final InstancesAppState instancesAppState;
    private final Scheduler scheduler;
    private final Application application;
    private final SettingsProvider settingsProvider;

    public MainMenuViewModel(Application application, VersionInformation versionInformation,
                             SettingsProvider settingsProvider, InstancesAppState instancesAppState,
                             Scheduler scheduler) {
        this.application = application;
        this.version = versionInformation;
        this.settingsProvider = settingsProvider;
        this.generalSettings = settingsProvider.getUnprotectedSettings();
        this.adminSettings = settingsProvider.getProtectedSettings();
        this.instancesAppState = instancesAppState;
        this.scheduler = scheduler;
    }

    public String getVersion() {
        return version.getVersionToDisplay();
    }

    @Nullable
    public String getVersionCommitDescription() {
        String commitDescription = "";

        if (version.getCommitCount() != null) {
            commitDescription = appendToCommitDescription(commitDescription, version.getCommitCount().toString());
        }

        if (version.getCommitSHA() != null) {
            commitDescription = appendToCommitDescription(commitDescription, version.getCommitSHA());
        }

        if (version.isDirty()) {
            commitDescription = appendToCommitDescription(commitDescription, "dirty");
        }

        if (!commitDescription.isEmpty()) {
            return commitDescription;
        } else {
            return null;
        }
    }

    public boolean shouldEditSavedFormButtonBeVisible() {
        return adminSettings.getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED);
    }

    public boolean shouldSendFinalizedFormButtonBeVisible() {
        return adminSettings.getBoolean(ProtectedProjectKeys.KEY_SEND_FINALIZED);
    }

    public boolean shouldViewSentFormButtonBeVisible() {
        return adminSettings.getBoolean(ProtectedProjectKeys.KEY_VIEW_SENT);
    }

    public boolean shouldGetBlankFormButtonBeVisible() {
        boolean buttonEnabled = adminSettings.getBoolean(ProtectedProjectKeys.KEY_GET_BLANK);
        return !isMatchExactlyEnabled() && buttonEnabled;
    }

    public boolean shouldDeleteSavedFormButtonBeVisible() {
        return adminSettings.getBoolean(ProtectedProjectKeys.KEY_DELETE_SAVED);
    }

    public LiveData<Integer> getEditableInstancesCount() {
        return instancesAppState.getEditableCount();
    }

    public LiveData<Integer> getSendableInstancesCount() {
        return instancesAppState.getSendableCount();
    }

    public LiveData<Integer> getSentInstancesCount() {
        return instancesAppState.getSentCount();
    }

    private boolean isMatchExactlyEnabled() {
        return SettingsUtils.getFormUpdateMode(application, generalSettings) == FormUpdateMode.MATCH_EXACTLY;
    }

    @NotNull
    private String appendToCommitDescription(String commitDescription, String part) {
        if (commitDescription.isEmpty()) {
            commitDescription = part;
        } else {
            commitDescription = commitDescription + "-" + part;
        }
        return commitDescription;
    }

    public void refreshInstances() {
        scheduler.immediate(() -> {
            new InstanceDiskSynchronizer(settingsProvider).doInBackground();
            instancesAppState.update();
            return null;
        }, ignored -> {
        });

    }

    public static class Factory implements ViewModelProvider.Factory {

        private final VersionInformation versionInformation;
        private final Application application;
        private final SettingsProvider settingsProvider;
        private final InstancesAppState instancesAppState;
        private final Scheduler scheduler;

        public Factory(VersionInformation versionInformation, Application application,
                       SettingsProvider settingsProvider, InstancesAppState instancesAppState,
                       Scheduler scheduler) {
            this.versionInformation = versionInformation;
            this.application = application;
            this.settingsProvider = settingsProvider;
            this.instancesAppState = instancesAppState;
            this.scheduler = scheduler;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new MainMenuViewModel(application, versionInformation, settingsProvider, instancesAppState, scheduler);
        }
    }
}

package org.odk.collect.android.activities.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.configure.SettingsUtils;
import org.odk.collect.android.preferences.FormUpdateMode;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.PreferencesDataSource;
import org.odk.collect.android.preferences.PreferencesDataSourceProvider;
import org.odk.collect.android.version.VersionInformation;

import javax.inject.Inject;

public class MainMenuViewModel extends ViewModel {

    private final VersionInformation version;
    private final PreferencesDataSource generalPreferences;
    private final PreferencesDataSource adminPreferences;
    private final Application application;

    public MainMenuViewModel(Application application, VersionInformation versionInformation, PreferencesDataSourceProvider preferencesDataSourceProvider) {
        this.application = application;
        this.version = versionInformation;
        this.generalPreferences = preferencesDataSourceProvider.getGeneralPreferences();
        this.adminPreferences = preferencesDataSourceProvider.getAdminPreferences();
    }

    public String getVersion() {
        if (version.getBetaNumber() != null) {
            return version.getSemanticVersion() + " Beta " + version.getBetaNumber();
        } else {
            return version.getSemanticVersion();
        }
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
        return adminPreferences.getBoolean(AdminKeys.KEY_EDIT_SAVED);
    }

    public boolean shouldSendFinalizedFormButtonBeVisible() {
        return adminPreferences.getBoolean(AdminKeys.KEY_SEND_FINALIZED);
    }

    public boolean shouldViewSentFormButtonBeVisible() {
        return adminPreferences.getBoolean(AdminKeys.KEY_VIEW_SENT);
    }

    public boolean shouldGetBlankFormButtonBeVisible() {
        boolean buttonEnabled = adminPreferences.getBoolean(AdminKeys.KEY_GET_BLANK);
        return !isMatchExactlyEnabled() && buttonEnabled;
    }

    public boolean shouldDeleteSavedFormButtonBeVisible() {
        return adminPreferences.getBoolean(AdminKeys.KEY_DELETE_SAVED);
    }

    private boolean isMatchExactlyEnabled() {
        return SettingsUtils.getFormUpdateMode(application, generalPreferences) == FormUpdateMode.MATCH_EXACTLY;
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

    public static class Factory implements ViewModelProvider.Factory {

        private final VersionInformation versionInformation;
        private final Application application;
        private final PreferencesDataSourceProvider preferencesDataSourceProvider;

        @Inject
        public Factory(VersionInformation versionInformation, Application application, PreferencesDataSourceProvider preferencesDataSourceProvider) {
            this.versionInformation = versionInformation;
            this.application = application;
            this.preferencesDataSourceProvider = preferencesDataSourceProvider;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new MainMenuViewModel(application, versionInformation, preferencesDataSourceProvider);
        }
    }
}

package org.odk.collect.android.activities.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.formmanagement.FormUpdateMode;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.GeneralKeys;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.version.VersionInformation;

import javax.inject.Inject;

public class MainMenuViewModel extends ViewModel {

    private final VersionInformation version;
    private final SharedPreferences generalSharedPreferences;
    private final SharedPreferences adminSharedPreferences;
    private final Application application;

    public MainMenuViewModel(Application application, VersionInformation versionInformation, PreferencesProvider preferencesProvider) {
        this.application = application;
        this.version = versionInformation;
        this.generalSharedPreferences = preferencesProvider.getGeneralSharedPreferences();
        this.adminSharedPreferences = preferencesProvider.getAdminSharedPreferences();
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
        return adminSharedPreferences.getBoolean(AdminKeys.KEY_EDIT_SAVED, true);
    }

    public boolean shouldSendFinalizedFormButtonBeVisible() {
        return adminSharedPreferences.getBoolean(AdminKeys.KEY_SEND_FINALIZED, true);
    }

    public boolean shouldViewSentFormButtonBeVisible() {
        return adminSharedPreferences.getBoolean(AdminKeys.KEY_VIEW_SENT, true);
    }

    public boolean shouldGetBlankFormButtonBeVisible() {
        boolean buttonEnabled = adminSharedPreferences.getBoolean(AdminKeys.KEY_GET_BLANK, true);
        return !isMatchExactlyEnabled() && buttonEnabled;
    }

    public boolean shouldDeleteSavedFormButtonBeVisible() {
        return adminSharedPreferences.getBoolean(AdminKeys.KEY_DELETE_SAVED, true);
    }

    private boolean isMatchExactlyEnabled() {
        FormUpdateMode formUpdateMode = FormUpdateMode.parse(application, generalSharedPreferences.getString(GeneralKeys.KEY_FORM_UPDATE_MODE, null));
        return formUpdateMode == FormUpdateMode.MATCH_EXACTLY;
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
        private final PreferencesProvider preferencesProvider;

        @Inject
        public Factory(VersionInformation versionInformation, Application application, PreferencesProvider preferencesProvider) {
            this.versionInformation = versionInformation;
            this.application = application;
            this.preferencesProvider = preferencesProvider;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new MainMenuViewModel(application, versionInformation, preferencesProvider);
        }
    }
}

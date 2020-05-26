package org.odk.collect.android.activities.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.version.VersionInformation;

public class MainMenuViewModel extends ViewModel {

    private final VersionInformation version;
    private final AdminSharedPreferences adminSharedPreferences;

    MainMenuViewModel(VersionInformation versionInformation) {
        this(versionInformation, AdminSharedPreferences.getInstance());
    }

    private MainMenuViewModel(VersionInformation versionInformation, AdminSharedPreferences adminSharedPreferences) {
        this.version = versionInformation;
        this.adminSharedPreferences = adminSharedPreferences;
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
        return (boolean) adminSharedPreferences.get(AdminKeys.KEY_EDIT_SAVED);
    }

    public boolean shouldSendFinalizedFormButtonBeVisible() {
        return (boolean) adminSharedPreferences.get(AdminKeys.KEY_SEND_FINALIZED);
    }

    public boolean shouldViewSentFormButtonBeVisible() {
        return (boolean) adminSharedPreferences.get(AdminKeys.KEY_VIEW_SENT);
    }

    public boolean shouldGetBlankFormButtonBeVisible() {
        return (boolean) adminSharedPreferences.get(AdminKeys.KEY_GET_BLANK);
    }

    public boolean shouldDeleteSavedFormButtonBeVisible() {
        return (boolean) adminSharedPreferences.get(AdminKeys.KEY_DELETE_SAVED);
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

        public Factory(VersionInformation versionInformation) {
            this.versionInformation = versionInformation;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new MainMenuViewModel(versionInformation, AdminSharedPreferences.getInstance());
        }
    }
}

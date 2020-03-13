package org.odk.collect.android.activities.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;

public class MainMenuViewModel extends ViewModel {

    private final VersionDescriptionProvider versionDescriptionProvider;
    private final AdminSharedPreferences adminSharedPreferences;

    MainMenuViewModel(VersionDescriptionProvider versionDescriptionProvider) {
        this(versionDescriptionProvider, AdminSharedPreferences.getInstance());
    }

    private MainMenuViewModel(VersionDescriptionProvider versionDescriptionProvider, AdminSharedPreferences adminSharedPreferences) {
        this.versionDescriptionProvider = versionDescriptionProvider;
        this.adminSharedPreferences = adminSharedPreferences;
    }

    public String getVersion() {
        if (hasBetaTag()) {
            return getVersionDescriptionComponents()[0] + " Beta " + versionDescriptionProvider.getVersionDescription().split("beta")[1].substring(1, 2);
        } else {
            return getVersionDescriptionComponents()[0];
        }
    }

    @Nullable
    public String getVersionCommitDescription() {
        if (isRelease() || isBetaRelease()) {
            return null;
        } else {
            String commitDescription = "";
            String[] components = getVersionDescriptionComponents();

            if (hasBetaTag() && getVersionDescriptionComponents().length > 3) {
                commitDescription = components[2] + "-" + components[3];
            } else if (!hasBetaTag() && getVersionDescriptionComponents().length > 2) {
                commitDescription = components[1] + "-" + components[2];
            }

            if (isDirty()) {
                if (commitDescription.isEmpty()) {
                    commitDescription = "dirty";
                } else {
                    commitDescription = commitDescription + "-dirty";
                }
            }

            return commitDescription;
        }
    }

    private boolean isDirty() {
        return versionDescriptionProvider.getVersionDescription().contains("dirty");
    }

    private boolean hasBetaTag() {
        return versionDescriptionProvider.getVersionDescription().contains("beta");
    }

    private boolean isRelease() {
        return getVersionDescriptionComponents().length == 1;
    }

    private boolean isBetaRelease() {
        return hasBetaTag() && getVersionDescriptionComponents().length == 2;
    }

    @NotNull
    private String[] getVersionDescriptionComponents() {
        return versionDescriptionProvider.getVersionDescription().split("-");
    }

    public interface VersionDescriptionProvider {
        String getVersionDescription();
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

    public static class Factory implements ViewModelProvider.Factory {

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new MainMenuViewModel(() -> BuildConfig.VERSION_NAME, AdminSharedPreferences.getInstance());
        }
    }
}

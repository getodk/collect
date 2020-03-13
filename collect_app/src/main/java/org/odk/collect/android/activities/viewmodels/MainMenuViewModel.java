package org.odk.collect.android.activities.viewmodels;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.BuildConfig;

public class MainMenuViewModel extends ViewModel {

    private final VersionDescriptionProvider versionDescriptionProvider;

    public MainMenuViewModel(VersionDescriptionProvider versionDescriptionProvider) {
        this.versionDescriptionProvider = versionDescriptionProvider;
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

    public static class Factory implements ViewModelProvider.Factory {

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new MainMenuViewModel(() -> BuildConfig.VERSION_NAME);
        }
    }
}

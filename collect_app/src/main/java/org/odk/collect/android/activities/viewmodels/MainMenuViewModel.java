package org.odk.collect.android.activities.viewmodels;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import org.odk.collect.android.BuildConfig;

public class MainMenuViewModel extends ViewModel {

    public String getVersion() {
        return versionNameWithoutPrefix().split("-")[0];
    }

    @Nullable
    public String getVersionSHA() {
        String[] split = versionNameWithoutPrefix().split("-", 2);

        if (split.length > 1) {
            return split[1];
        } else {
            return null;
        }
    }

    private String versionNameWithoutPrefix() {
        return BuildConfig.VERSION_NAME.split("v")[1];
    }
}

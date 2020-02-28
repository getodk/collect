package org.odk.collect.android.activities.viewmodels;

import androidx.lifecycle.ViewModel;

import org.odk.collect.android.BuildConfig;

public class MainMenuViewModel extends ViewModel {

    public String getVersion() {
        return BuildConfig.VERSION_NAME.split("v")[1].split("-")[0];
    }
}

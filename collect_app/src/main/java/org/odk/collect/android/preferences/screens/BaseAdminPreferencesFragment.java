package org.odk.collect.android.preferences.screens;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.source.SettingsStore;
import org.odk.collect.shared.settings.Settings;

import javax.inject.Inject;
import javax.inject.Named;

public abstract class BaseAdminPreferencesFragment extends BasePreferencesFragment {

    @Inject
    @Named("ADMIN_SETTINGS_STORE")
    SettingsStore adminSettingsStore;

    String projectId;
    Settings adminSettings;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);

        adminSettings = settingsProvider.getProtectedSettings();
        projectId = projectsDataService.getCurrentProject().getUuid();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setPreferenceDataStore(adminSettingsStore);
    }

    @Override
    public void onResume() {
        super.onResume();
        adminSettings.registerOnSettingChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        adminSettings.unregisterOnSettingChangeListener(this);
    }

    @Override
    public void onSettingChanged(@NotNull String key) {
        settingsChangeHandler.onSettingChanged(projectId, adminSettings.getAll().get(key), key);
    }
}

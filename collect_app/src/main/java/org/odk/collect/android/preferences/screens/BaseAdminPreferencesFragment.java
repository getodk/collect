package org.odk.collect.android.preferences.screens;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.preferences.source.SettingsStore;
import org.odk.collect.shared.settings.Settings;

public abstract class BaseAdminPreferencesFragment extends BasePreferencesFragment {

    String projectId;
    Settings adminSettings;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        adminSettings = settingsProvider.getProtectedSettings();
        projectId = projectsDataService.getCurrentProject().getUuid();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setPreferenceDataStore(new SettingsStore(adminSettings));
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

package org.odk.collect.android.preferences.screens;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.source.SettingsStore;

import javax.inject.Inject;
import javax.inject.Named;

public abstract class BaseGeneralPreferencesFragment extends BasePreferencesFragment {

    @Inject
    @Named("GENERAL_SETTINGS_STORE")
    SettingsStore generalSettingsStore;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setPreferenceDataStore(generalSettingsStore);
    }

    @Override
    public void onResume() {
        super.onResume();
        settingsProvider.getGeneralSettings().registerOnSettingChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        settingsProvider.getGeneralSettings().unregisterOnSettingChangeListener(this);
    }

    @Override
    public void onSettingChanged(@NotNull String key) {
        settingsChangeHandler.onSettingChanged(currentProjectProvider.getCurrentProject().getUuid(), settingsProvider.getGeneralSettings().getAll().get(key), key);
    }
}

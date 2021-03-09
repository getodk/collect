package org.odk.collect.android.preferences.screens;

import android.os.Bundle;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.preferences.source.GeneralPreferencesDataStore;

import javax.inject.Inject;

public abstract class BaseGeneralPreferencesFragment extends BasePreferencesFragment {
    @Inject
    GeneralPreferencesDataStore generalPreferencesDataStore;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setPreferenceDataStore(generalPreferencesDataStore);
    }

    @Override
    public void onResume() {
        super.onResume();
        preferencesDataSourceProvider.getGeneralPreferences().registerOnPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        preferencesDataSourceProvider.getGeneralPreferences().unregisterOnPreferenceChangeListener(this);
    }

    @Override
    public void onPreferenceChanged(@NotNull String key) {
        settingsChangeHandler.onSettingChanged(key, preferencesDataSourceProvider.getGeneralPreferences().getAll().get(key));
    }
}

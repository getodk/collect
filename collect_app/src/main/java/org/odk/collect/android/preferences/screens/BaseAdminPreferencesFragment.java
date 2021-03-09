package org.odk.collect.android.preferences.screens;

import android.os.Bundle;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.preferences.stores.AdminPreferencesDataStore;

import javax.inject.Inject;

public abstract class BaseAdminPreferencesFragment extends BasePreferenceFragment {
    @Inject
    AdminPreferencesDataStore adminPreferencesDataStore;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setPreferenceDataStore(adminPreferencesDataStore);
    }

    @Override
    public void onResume() {
        super.onResume();
        preferencesDataSourceProvider.getAdminPreferences().registerOnPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        preferencesDataSourceProvider.getAdminPreferences().unregisterOnPreferenceChangeListener(this);
    }

    @Override
    public void onPreferenceChanged(@NotNull String key) {
        settingsChangeHandler.onSettingChanged(key, preferencesDataSourceProvider.getAdminPreferences().getAll().get(key));
    }
}

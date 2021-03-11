package org.odk.collect.android.preferences.screens;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.source.PreferencesDataStore;

import javax.inject.Inject;
import javax.inject.Named;

public abstract class BaseGeneralPreferencesFragment extends BasePreferencesFragment {
    @Inject
    @Named("GENERAL_PREFERENCES_DATA_STORE")
    PreferencesDataStore generalPreferencesDataStore;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
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

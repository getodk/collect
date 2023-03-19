package org.odk.collect.android.preferences.screens;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.PreferenceVisibilityHandler;
import org.odk.collect.android.preferences.ProjectPreferencesViewModel;
import org.odk.collect.android.preferences.source.SettingsStore;
import org.odk.collect.android.utilities.AdminPasswordProvider;

import javax.inject.Inject;
import javax.inject.Named;

public abstract class BaseProjectPreferencesFragment extends BasePreferencesFragment {

    @Inject
    @Named("GENERAL_SETTINGS_STORE")
    SettingsStore generalSettingsStore;

    @Inject
    AdminPasswordProvider adminPasswordProvider;

    @Inject
    ProjectPreferencesViewModel.Factory factory;

    @Inject
    PreferenceVisibilityHandler preferenceVisibilityHandler;

    protected ProjectPreferencesViewModel projectPreferencesViewModel;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
        projectPreferencesViewModel = new ViewModelProvider(requireActivity(), factory).get(ProjectPreferencesViewModel.class);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setPreferenceDataStore(generalSettingsStore);
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        preferenceVisibilityHandler.updatePreferencesVisibility(getPreferenceScreen(), projectPreferencesViewModel.getState().getValue().getValue());
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        settingsProvider.getUnprotectedSettings().registerOnSettingChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        settingsProvider.getUnprotectedSettings().unregisterOnSettingChangeListener(this);
    }

    @Override
    public void onSettingChanged(@NotNull String key) {
        settingsChangeHandler.onSettingChanged(currentProjectProvider.getCurrentProject().getUuid(), settingsProvider.getUnprotectedSettings().getAll().get(key), key);
    }
}

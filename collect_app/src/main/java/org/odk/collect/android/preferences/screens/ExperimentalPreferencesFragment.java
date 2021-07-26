package org.odk.collect.android.preferences.screens;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.keys.ProjectKeys;

import static org.odk.collect.android.activities.ActivityUtils.startActivityAndCloseAllOthers;

public class ExperimentalPreferencesFragment extends BaseProjectPreferencesFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.experimental_preferences, rootKey);

        findPreference(ProjectKeys.KEY_MAGENTA_THEME).setOnPreferenceChangeListener((preference, newValue) -> {
            startActivityAndCloseAllOthers(requireActivity(), MainMenuActivity.class);
            return true;
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }
}

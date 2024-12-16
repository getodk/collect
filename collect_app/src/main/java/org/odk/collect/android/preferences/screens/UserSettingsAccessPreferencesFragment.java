package org.odk.collect.android.preferences.screens;

import android.os.Bundle;

import androidx.preference.PreferenceCategory;

import org.odk.collect.android.R;
import org.odk.collect.android.application.FeatureFlags;
import org.odk.collect.settings.keys.ProtectedProjectKeys;

public class UserSettingsAccessPreferencesFragment extends BaseAdminPreferencesFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.user_settings_access_preferences, rootKey);

        if (FeatureFlags.NO_THEME_SETTING) {
            PreferenceCategory category = (PreferenceCategory) getPreferenceScreen().getPreference(0);
            category.removePreference(findPreference(ProtectedProjectKeys.KEY_APP_THEME));
        }
    }
}

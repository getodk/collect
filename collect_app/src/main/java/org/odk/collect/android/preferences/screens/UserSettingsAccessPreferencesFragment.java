package org.odk.collect.android.preferences.screens;

import android.os.Bundle;

import org.odk.collect.android.R;

public class UserSettingsAccessPreferencesFragment extends BaseAdminPreferencesFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.user_settings_access_preferences, rootKey);
    }
}

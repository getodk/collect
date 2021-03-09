package org.odk.collect.android.preferences.screens;

import android.os.Bundle;

import org.odk.collect.android.R;

public class CustomServerPathsFragment extends BaseGeneralPreferencesFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.custom_server_paths_preferences, rootKey);
    }
}

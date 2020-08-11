package org.odk.collect.android.preferences;

import android.os.Bundle;

import org.odk.collect.android.R;

public class CustomServerPathsFragment extends BasePreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.custom_server_paths_preferences, rootKey);
    }
}

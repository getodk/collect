package org.odk.collect.android.preferences;


import android.os.Bundle;
import android.preference.PreferenceActivity;

import org.odk.collect.android.R;

public class ServerPreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new ServerPreferences())
                .commit();
        setTitle(getString(R.string.server_preferences));
    }
}

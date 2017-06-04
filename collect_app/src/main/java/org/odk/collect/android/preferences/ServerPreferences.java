package org.odk.collect.android.preferences;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

import org.odk.collect.android.R;

import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PROTOCOL;


public class ServerPreferences extends ServerPreferencesFragment implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.server_preferences);

        initProtocolPrefs();
    }

    private void initProtocolPrefs() {
        ListPreference protocolPref = (ListPreference) findPreference(KEY_PROTOCOL);

        protocolPref.setSummary(protocolPref.getEntry());
        protocolPref.setOnPreferenceChangeListener(this);

        addPreferencesResource(protocolPref.getValue());
    }

    private void addPreferencesResource(CharSequence value) {
        if (value.equals(getString(R.string.protocol_odk_default))) {
            setDefaultAggregatePaths();
            addAggregatePreferences();
        } else if (value.equals(getString(R.string.protocol_google_sheets))) {
            addGooglePreferences();
        } else {
            // other
            addOtherPreferences();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        super.onPreferenceChange(preference, newValue);

        if (preference.getKey().equals(KEY_PROTOCOL)) {
            String stringValue = (String) newValue;
            ListPreference lpref = (ListPreference) preference;
            String oldValue = lpref.getValue();
            lpref.setValue(stringValue);

            if (!newValue.equals(oldValue)) {
                removeTypeSettings();
                initProtocolPrefs();
            }
        }
        return true;
    }

    private void removeTypeSettings() {
        getPreferenceScreen().removeAll();
        addPreferencesFromResource(R.xml.server_preferences);
    }
}

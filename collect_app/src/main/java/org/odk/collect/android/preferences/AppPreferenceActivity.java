package org.odk.collect.android.preferences;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

/** Elements common to both general and admin preferences */
abstract class AppPreferenceActivity extends PreferenceActivity {
    /** Encapsulate the findPreference deprecation warning */
    protected Preference pref(String key) {
        return findPreference(key);
    }

    /** Allow shorter code to get ListPreferences */
    protected ListPreference listPref(String key) {
        return (ListPreference) pref(key);
    }
}

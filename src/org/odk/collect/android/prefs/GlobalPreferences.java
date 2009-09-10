package org.odk.collect.android.prefs;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

import org.odk.collect.android.R;

public class GlobalPreferences extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    public static String KEY_SERVER = "server";
    public static String KEY_USERNAME = "username";
    public static String KEY_PASSWORD = "password";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.global_preferences);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.preferences));
        updateServer();
        updateUsername();
        updatePassword();
    }


    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }


    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_SERVER)) {
            updateServer();
        } else if (key.equals(KEY_USERNAME)) {
            updateUsername();
        } else if (key.equals(KEY_PASSWORD)) {
            updatePassword();
        }
    }


    private void updateServer() {
        EditTextPreference etp =
                (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_SERVER);
        String s = etp.getText();
        if (s.endsWith("/")) {
            s = s.substring(0, s.lastIndexOf("/"));
        }
        etp.setSummary(s);
    }


    private void updateUsername() {
        EditTextPreference etp =
                (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_USERNAME);
        etp.setSummary(etp.getText());
    }


    private void updatePassword() {
        EditTextPreference etp =
                (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_PASSWORD);
        etp.setSummary(etp.getText());
    }



}

package org.odk.collect.android;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

public class ServerPreferences extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.server_preferences);
        updateSummary();
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
        if (key.equals("UploadServer")) {
            updateSummary();
        }
    }


    private void updateSummary() {
        EditTextPreference etp =
                (EditTextPreference) this.getPreferenceScreen().findPreference("UploadServer");
        etp.setSummary(etp.getText());
    }

}

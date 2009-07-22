package org.odk.collect.android;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class SavedPreferences extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.saved_preferences);
        updateFileTypeView();
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
        if (key.equals("saved_list_file_type")) {
            updateFileTypeView();
        }
    }
    
    private void updateFileTypeView() {
        ListPreference lp =
                (ListPreference) this.getPreferenceScreen().findPreference("saved_list_file_type");
        lp.setSummary(lp.getEntry());
    }

}

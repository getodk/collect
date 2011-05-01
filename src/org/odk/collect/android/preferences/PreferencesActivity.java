
package org.odk.collect.android.preferences;

import org.odk.collect.android.R;
import org.odk.collect.android.utilities.UrlUtils;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class PreferencesActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

    public static String KEY_LAST_VERSION = "lastVersion";
    public static String KEY_FIRST_RUN = "firstRun";
    public static String KEY_SHOW_SPLASH = "showSplash";
    public static String KEY_SERVER = "server";
    public static String KEY_USERNAME = "username";
    public static String KEY_PASSWORD = "password";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.general_preferences));
        updateSplash();
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


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_SERVER)) {
            updateServer();
        } else if (key.equals(KEY_USERNAME)) {
            updateUsername();
        } else if (key.equals(KEY_PASSWORD)) {
            updatePassword();
        } else if (key.equals(KEY_SHOW_SPLASH)) {
            updateSplash();
        }
    }


    private void updateServer() {
        EditTextPreference etp =
            (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_SERVER);
        String s = etp.getText().trim();

        if (UrlUtils.isValidUrl(s)) {
            etp.setText(s);
            etp.setSummary(s);
        } else {
            etp.setText((String) etp.getSummary());
            Toast.makeText(getApplicationContext(), getString(R.string.url_error),
                Toast.LENGTH_SHORT).show();
        }
    }


    private void updateSplash() {
        CheckBoxPreference cbp =
            (CheckBoxPreference) this.getPreferenceScreen().findPreference(KEY_SHOW_SPLASH);
        if(cbp.isChecked()) {
            cbp.setSummary("show splash screen on startup");
        } else{ 
            cbp.setSummary("no splash screen on startup");

        }
    }


    private void updateUsername() {
        EditTextPreference etp =
            (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_USERNAME);
        etp.setSummary(etp.getText());
    }


    private void updatePassword() {
        EditTextPreference etp =
            (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_PASSWORD);
        etp.setSummary("***************");
    }
}

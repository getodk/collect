package org.odk.collect.android.preferences;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND_NETWORK;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND_WIFI;

/**
 * Migrates existing autosend_wifi and autosend_network preference values to autosend
 */
public class AutosendPreferenceMigrator {

    @SuppressLint("ApplySharedPref")
    public static void migrate(SharedPreferences sharedPreferences) {

        boolean autosendWifi = sharedPreferences.getBoolean(KEY_AUTOSEND_WIFI, false);
        boolean autosendNetwork = sharedPreferences.getBoolean(KEY_AUTOSEND_NETWORK, false);
        String autosend = sharedPreferences.getString(KEY_AUTOSEND, "off");

        if (autosend.equals("off")) {
            if (autosendNetwork && autosendWifi) {
                autosend = "wifi_and_cellular";
            } else if (autosendWifi) {
                autosend = "wifi_only";
            } else if (autosendNetwork) {
                autosend = "cellular_only";
            }
        }

        GeneralSharedPreferences.getInstance().save(PreferenceKeys.KEY_AUTOSEND, autosend);
    }
}

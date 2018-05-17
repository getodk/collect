package org.odk.collect.android.preferences;

import org.json.JSONObject;

import java.util.Map;

import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND_NETWORK;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND_WIFI;

/**
 * Migrates existing autosend_wifi and autosend_network preference values to autosend
 */
public class AutoSendPreferenceMigrator {

    private AutoSendPreferenceMigrator() {

    }

    public static void migrate(GeneralSharedPreferences generalSharedPreferences) {

        boolean autoSendWifi = generalSharedPreferences.getBoolean(KEY_AUTOSEND_WIFI, false);
        boolean autoSendNetwork = generalSharedPreferences.getBoolean(KEY_AUTOSEND_NETWORK, false);

        migrate(generalSharedPreferences, autoSendWifi, autoSendNetwork);
    }

    public static void migrate(GeneralSharedPreferences generalSharedPreferences, JSONObject generalPrefsJson) {

        boolean autoSendWifi = false;
        if (generalPrefsJson.has(KEY_AUTOSEND_WIFI)) {
            autoSendWifi = true;
        }

        boolean autoSendNetwork = false;
        if (generalPrefsJson.has(KEY_AUTOSEND_NETWORK)) {
            autoSendNetwork = true;
        }

        migrate(generalSharedPreferences, autoSendWifi, autoSendNetwork);
    }

    public static void migrate(GeneralSharedPreferences generalSharedPreferences, Map<String, ?> entries) {

        boolean autoSendWifi = false;
        if (entries.containsKey(KEY_AUTOSEND_WIFI)) {
            Object value = entries.get(KEY_AUTOSEND_WIFI);
            if (value instanceof Boolean) {
                autoSendWifi = (boolean) value;
                entries.remove(KEY_AUTOSEND_WIFI);
            }
        }

        boolean autoSendNetwork = false;
        if (entries.containsKey(KEY_AUTOSEND_NETWORK)) {
            Object value = entries.get(KEY_AUTOSEND_NETWORK);
            if (value instanceof Boolean) {
                autoSendNetwork = (boolean) value;
                entries.remove(KEY_AUTOSEND_NETWORK);
            }
        }

        migrate(generalSharedPreferences, autoSendWifi, autoSendNetwork);
    }

    private static void migrate(GeneralSharedPreferences generalSharedPreferences, boolean autoSendWifi, boolean autoSendNetwork) {
        String autoSend = (String) generalSharedPreferences.get(KEY_AUTOSEND);

        if (autoSendNetwork && autoSendWifi) {
            autoSend = "wifi_and_cellular";
        } else if (autoSendWifi) {
            autoSend = "wifi_only";
        } else if (autoSendNetwork) {
            autoSend = "cellular_only";
        }

        //save to shared preferences
        generalSharedPreferences.save(KEY_AUTOSEND, autoSend);
    }
}

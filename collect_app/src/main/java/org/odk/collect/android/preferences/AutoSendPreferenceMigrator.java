package org.odk.collect.android.preferences;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND_NETWORK;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND_WIFI;

/**
 * Migrates existing autosend_wifi and autosend_network preference values to autosend
 */
public class AutoSendPreferenceMigrator {

    private static boolean autoSendWifi = false;
    private static boolean autoSendNetwork = false;
    private static String autoSend = "off";

    @SuppressLint("ApplySharedPref")
    public static void migrate(SharedPreferences sharedPreferences) {

        autoSendWifi = sharedPreferences.getBoolean(KEY_AUTOSEND_WIFI, false);
        autoSendNetwork = sharedPreferences.getBoolean(KEY_AUTOSEND_NETWORK, false);
        autoSend = sharedPreferences.getString(KEY_AUTOSEND, "off");

        migrate(autoSendWifi, autoSendNetwork, autoSend);
    }

    public static void migrate(JSONObject generalPrefsJson) throws JSONException {

        if (generalPrefsJson.has(KEY_AUTOSEND_WIFI)) {
            autoSendWifi = true;
        }

        if (generalPrefsJson.has(KEY_AUTOSEND_NETWORK)) {
            autoSendNetwork = true;
        }

        migrate(autoSendWifi, autoSendNetwork, autoSend);
    }

    public static void migrate(Map<String, ?> entries) {

        if (entries.containsKey(PreferenceKeys.KEY_AUTOSEND_WIFI)) {
            Object value = entries.get(PreferenceKeys.KEY_AUTOSEND_WIFI);
            if (value instanceof Boolean) {
                autoSendWifi = (boolean) value;
                entries.remove(PreferenceKeys.KEY_AUTOSEND_WIFI);
            }
        }

        if (entries.containsKey(PreferenceKeys.KEY_AUTOSEND_NETWORK)) {
            Object value = entries.get(PreferenceKeys.KEY_AUTOSEND_NETWORK);
            if (value instanceof Boolean) {
                autoSendNetwork = (boolean) value;
                entries.remove(PreferenceKeys.KEY_AUTOSEND_NETWORK);
            }
        }

        migrate(autoSendWifi, autoSendNetwork, autoSend);
    }

    private static void migrate(boolean autoSendWifi, boolean autoSendNetwork, String autoSend) {

        if (autoSend.equals("off")) {
            if (autoSendNetwork && autoSendWifi) {
                autoSend = "wifi_and_cellular";
            } else if (autoSendWifi) {
                autoSend = "wifi_only";
            } else if (autoSendNetwork) {
                autoSend = "cellular_only";
            }
        }

        //save to shared preferences
        GeneralSharedPreferences.getInstance().save(PreferenceKeys.KEY_AUTOSEND, autoSend);
    }
}

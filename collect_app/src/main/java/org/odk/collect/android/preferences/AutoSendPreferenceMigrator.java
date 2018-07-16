package org.odk.collect.android.preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND_MIGRATED;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND_NETWORK;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND_WIFI;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_MULTI_AUTOSEND;

/**
 * Migrates existing autosend_wifi and autosend_network preference values to autosend
 */
public class AutoSendPreferenceMigrator {

    private AutoSendPreferenceMigrator() {

    }

    public static void migrate() {

        boolean autoSendWifi = GeneralSharedPreferences.getInstance().getBoolean(KEY_AUTOSEND_WIFI, false);
        boolean autoSendNetwork = GeneralSharedPreferences.getInstance().getBoolean(KEY_AUTOSEND_NETWORK, false);

        migrate(autoSendWifi, autoSendNetwork);
    }

    public static void migrate(JSONObject generalPrefsJson) throws JSONException {

        boolean autoSendWifi = false;
        if (generalPrefsJson.has(KEY_AUTOSEND_WIFI)) {
            autoSendWifi = true;
        }

        boolean autoSendNetwork = false;
        if (generalPrefsJson.has(KEY_AUTOSEND_NETWORK)) {
            autoSendNetwork = true;
        }

        migrate(autoSendWifi, autoSendNetwork);
    }

    public static void migrate(Map<String, ?> entries) {

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

        migrate(autoSendWifi, autoSendNetwork);
    }

    private static void migrate(boolean autoSendWifi, boolean autoSendNetwork) {
        String autoSend = (String) GeneralSharedPreferences.getInstance().get(KEY_AUTOSEND);

        if (autoSendNetwork && autoSendWifi) {
            autoSend = "wifi_and_cellular";
        } else if (autoSendWifi) {
            autoSend = "wifi_only";
        } else if (autoSendNetwork) {
            autoSend = "cellular_only";
        }

        //save to shared preferences
        GeneralSharedPreferences.getInstance().save(KEY_AUTOSEND, autoSend);

        migrateToMultiListPreferences();
    }

    /**
     * Migrates the auto-send options to the multi-auto-send schema
     *
     * This involves checking the AUTO_SEND key for the current value and comparing it
     * with the values from the previous array that represented it's option.
     * Once that's done, the MULTI_AUTOSEND is populated with the selected options based on how
     * it correlates to the current options for the multi-select list.
     */
    private static void migrateToMultiListPreferences() {

        boolean migrated = GeneralSharedPreferences.getInstance().getBoolean(KEY_AUTOSEND_MIGRATED, false);

        if (migrated) {
            return;
        }

        GeneralSharedPreferences.getInstance().save(KEY_AUTOSEND_MIGRATED, true);

        String autoSend = (String) GeneralSharedPreferences.getInstance().get(KEY_AUTOSEND);

        Set<String> multiAutoSend = new HashSet<>();

        switch (autoSend) {
            case "wifi_only":
                multiAutoSend.add("wifi");
                break;

            case "cellular_only":
                multiAutoSend.add("cellular");
                break;

            case "wifi_and_cellular":
                multiAutoSend.add("wifi");
                multiAutoSend.add("cellular");
                break;
        }

        GeneralSharedPreferences.getInstance().save(KEY_MULTI_AUTOSEND, multiAutoSend);
    }
}

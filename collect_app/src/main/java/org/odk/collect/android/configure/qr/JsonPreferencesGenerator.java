package org.odk.collect.android.configure.qr;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.preferences.keys.AdminKeys;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;

import java.util.Collection;
import java.util.Map;

import timber.log.Timber;

import static org.odk.collect.android.preferences.keys.AdminKeys.ALL_KEYS;
import static org.odk.collect.android.preferences.keys.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_PASSWORD;

public class JsonPreferencesGenerator {
    private final SettingsProvider settingsProvider;
    public JsonPreferencesGenerator(SettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    public String getJSONFromPreferences(Collection<String> includedPasswordKeys) throws JSONException {
        JSONObject sharedPrefJson = getPrefsAsJson(includedPasswordKeys);
        Timber.i(sharedPrefJson.toString());
        return sharedPrefJson.toString();
    }

    private JSONObject getPrefsAsJson(Collection<String> includedPasswordKeys) throws JSONException {
        JSONObject prefs = new JSONObject();
        prefs.put("general", getGeneralPrefsAsJson(includedPasswordKeys));
        prefs.put("admin", getAdminPrefsAsJson(includedPasswordKeys));
        return prefs;
    }

    private JSONObject getGeneralPrefsAsJson(Collection<String> includedPasswordKeys) throws JSONException {
        JSONObject generalPrefs = new JSONObject();

        Map<String, ?> generalSettings = settingsProvider.getGeneralSettings().getAll();
        Map<String, ?> defaultSettings = GeneralKeys.getDefaults();
        for (String key : defaultSettings.keySet()) {
            if (key.equals(KEY_PASSWORD) && !includedPasswordKeys.contains(KEY_PASSWORD)) {
                continue;
            }

            Object value = generalSettings.get(key);
            if (value != null && !value.equals(defaultSettings.get(key))) {
                generalPrefs.put(key, value);
            }
        }
        return generalPrefs;
    }

    private JSONObject getAdminPrefsAsJson(Collection<String> includedPasswordKeys) throws JSONException {
        JSONObject adminPrefs = new JSONObject();

        Map<String, ?> adminSettings = settingsProvider.getAdminSettings().getAll();
        Map<String, ?> defaultSettings = AdminKeys.getDefaults();
        for (String key : ALL_KEYS) {
            if (key.equals(KEY_ADMIN_PW) && !includedPasswordKeys.contains(KEY_ADMIN_PW)) {
                continue;
            }

            Object value = adminSettings.get(key);
            if (value != null && !value.equals(defaultSettings.get(key))) {
                adminPrefs.put(key, value);
            }
        }
        return adminPrefs;
    }
}

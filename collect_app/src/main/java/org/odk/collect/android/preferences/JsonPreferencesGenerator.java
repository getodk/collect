package org.odk.collect.android.preferences;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

import timber.log.Timber;

import static org.odk.collect.android.preferences.AdminKeys.ALL_KEYS;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.GeneralKeys.DEFAULTS;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PASSWORD;

public class JsonPreferencesGenerator {
    private final PreferencesDataSourceProvider preferencesDataSourceProvider;
    public JsonPreferencesGenerator(PreferencesDataSourceProvider preferencesDataSourceProvider) {
        this.preferencesDataSourceProvider = preferencesDataSourceProvider;
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

        for (String key : DEFAULTS.keySet()) {
            if (key.equals(KEY_PASSWORD) && !includedPasswordKeys.contains(KEY_PASSWORD)) {
                continue;
            }

            Object defaultValue = DEFAULTS.get(key);
            Object value = preferencesDataSourceProvider.getGeneralPreferences().getAll().get(key);

            if (value == null) {
                value = "";
            }
            if (defaultValue == null) {
                defaultValue = "";
            }

            if (!defaultValue.equals(value)) {
                generalPrefs.put(key, value);
            }
        }
        return generalPrefs;
    }

    private JSONObject getAdminPrefsAsJson(Collection<String> includedPasswordKeys) throws JSONException {
        JSONObject adminPrefs = new JSONObject();

        for (String key : ALL_KEYS) {
            if (key.equals(KEY_ADMIN_PW) && !includedPasswordKeys.contains(KEY_ADMIN_PW)) {
                continue;
            }

            Object defaultValue = AdminKeys.getDefaults().get(key);
            Object value = preferencesDataSourceProvider.getAdminPreferences().getAll().get(key);

            if (defaultValue != value) {
                adminPrefs.put(key, value);
            }
        }
        return adminPrefs;
    }
}

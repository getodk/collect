package org.odk.collect.android.preferences;

import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.listeners.ActionListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import static org.odk.collect.android.preferences.AdminKeys.ALL_KEYS;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.GeneralKeys.GENERAL_KEYS;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PASSWORD;

public class PreferenceSaver {

    private final GeneralSharedPreferences generalSharedPreferences;
    private final AdminSharedPreferences adminSharedPreferences;

    public PreferenceSaver(GeneralSharedPreferences generalSharedPreferences, AdminSharedPreferences adminSharedPreferences) {
        this.generalSharedPreferences = generalSharedPreferences;
        this.adminSharedPreferences = adminSharedPreferences;
    }

    public void fromJSON(String content, @Nullable ActionListener listener) {
        try {
            JSONObject settingsJson = new JSONObject(content);
            Map<String, Object> generalPrefs = convertJSONToMap(settingsJson.getJSONObject("general"));
            JSONObject adminPrefsJson = settingsJson.getJSONObject("admin");

            if (!(new PreferenceValidator(GENERAL_KEYS).isValid(generalPrefs))) {
                if (listener != null) {
                    listener.onFailure(new GeneralSharedPreferences.ValidationException());
                }

                return;
            }

            for (String key : getAllGeneralKeys()) {
                if (generalPrefs.containsKey(key)) {
                    Object value = generalPrefs.get(key);
                    generalSharedPreferences.save(key, value);
                } else {
                    generalSharedPreferences.reset(key);
                }
            }

            for (String key : getAllAdminKeys()) {

                if (adminPrefsJson.has(key)) {
                    Object value = adminPrefsJson.get(key);
                    adminSharedPreferences.save(key, value);
                } else {
                    adminSharedPreferences.reset(key);
                }
            }

            AutoSendPreferenceMigrator.migrate(settingsJson.getJSONObject("general"));

            if (listener != null) {
                listener.onSuccess();
            }
        } catch (JSONException exception) {
            if (listener != null) {
                listener.onFailure(exception);
            }
        }
    }

    private static Map<String, Object> convertJSONToMap(JSONObject json) throws JSONException {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = json.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, json.get(key));
        }

        return map;
    }

    private static Collection<String> getAllGeneralKeys() {
        Collection<String> keys = new HashSet<>(GENERAL_KEYS.keySet());
        keys.add(KEY_PASSWORD);
        return keys;
    }

    private static Collection<String> getAllAdminKeys() {
        Collection<String> keys = new HashSet<>(ALL_KEYS);
        keys.add(KEY_ADMIN_PW);
        return keys;
    }
}

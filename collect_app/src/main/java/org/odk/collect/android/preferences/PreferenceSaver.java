/*
 * Copyright (C) 2018 Callum Stott
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.preferences;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.listeners.ActionListener;
import org.odk.collect.android.preferences.utilities.ChangingServerUrlUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import static org.odk.collect.android.preferences.AdminKeys.ALL_KEYS;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.GeneralKeys.GENERAL_KEYS;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_SERVER_URL;

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
            Map<String, Object> adminPrefsJson = convertJSONToMap(settingsJson.getJSONObject("admin"));

            if (!(new PreferenceValidator(GENERAL_KEYS).isValid(generalPrefs))) {
                if (listener != null) {
                    listener.onFailure(new GeneralSharedPreferences.ValidationException());
                }

                return;
            }

            saveGeneralPrefs(generalSharedPreferences, generalPrefs);
            saveAdminPrefs(adminSharedPreferences, adminPrefsJson);

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

    public static void saveGeneralPrefs(GeneralSharedPreferences generalSharedPreferences, Map<String, Object> generalPrefs) {
        for (String key : getAllGeneralKeys()) {
            if (generalPrefs.containsKey(key)) {
                Object value = generalPrefs.get(key);
                generalSharedPreferences.save(key, value);
                if (key.equals(KEY_SERVER_URL)) {
                    ChangingServerUrlUtils.addUrlToList(String.valueOf(value));
                }
            } else {
                generalSharedPreferences.reset(key);
            }
        }
    }

    public static void saveAdminPrefs(AdminSharedPreferences adminSharedPreferences, Map<String, Object> adminPrefsJson) {
        for (String key : getAllAdminKeys()) {
            if (adminPrefsJson.containsKey(key)) {
                Object value = adminPrefsJson.get(key);
                adminSharedPreferences.save(key, value);
            } else {
                adminSharedPreferences.reset(key);
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
        return new HashSet<>(GENERAL_KEYS.keySet());
    }

    private static Collection<String> getAllAdminKeys() {
        Collection<String> keys = new HashSet<>(ALL_KEYS);
        keys.add(KEY_ADMIN_PW);
        return keys;
    }
}

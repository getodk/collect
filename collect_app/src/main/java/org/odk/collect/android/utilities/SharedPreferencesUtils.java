/*
 * Copyright (C) 2017 Shobhit
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

package org.odk.collect.android.utilities;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import timber.log.Timber;

import static org.odk.collect.android.preferences.AdminKeys.ALL_KEYS;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.GeneralKeys.DEFAULTS;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PASSWORD;

public final class SharedPreferencesUtils {

    private SharedPreferencesUtils() {

    }

    public static String getJSONFromPreferences(Collection<String> passwordKeys) throws JSONException {
        Collection<String> keys = new ArrayList<>(passwordKeys);
        keys.addAll(DEFAULTS.keySet());
        JSONObject sharedPrefJson = getModifiedPrefs(keys);
        Timber.i(sharedPrefJson.toString());
        return sharedPrefJson.toString();
    }

    private static JSONObject getModifiedPrefs(Collection<String> keys) throws JSONException {
        JSONObject prefs = new JSONObject();
        JSONObject adminPrefs = new JSONObject();
        JSONObject generalPrefs = new JSONObject();

        // checking for admin password
        if (keys.contains(KEY_ADMIN_PW)) {
            String password = (String) AdminSharedPreferences.getInstance().get(KEY_ADMIN_PW);
            if (!password.equals("")) {
                adminPrefs.put(KEY_ADMIN_PW, password);
            }
            keys.remove(KEY_ADMIN_PW);
        }

        // checking for server password
        if (keys.contains(KEY_PASSWORD)) {
            String password = (String) GeneralSharedPreferences.getInstance().get(KEY_PASSWORD);
            if (!password.equals("")) {
                adminPrefs.put(KEY_PASSWORD, password);
            }
            keys.remove(KEY_PASSWORD);
        }

        for (String key : keys) {
            Object defaultValue = DEFAULTS.get(key);
            Object value = GeneralSharedPreferences.getInstance().get(key);

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
        prefs.put("general", generalPrefs);

        for (String key : ALL_KEYS) {

            Object defaultValue = AdminSharedPreferences.getInstance().getDefault(key);
            Object value = AdminSharedPreferences.getInstance().get(key);
            if (defaultValue != value) {
                adminPrefs.put(key, value);
            }
        }
        prefs.put("admin", adminPrefs);

        return prefs;
    }

    public static Collection<String> getAllGeneralKeys() {
        Collection<String> keys = new HashSet<>(DEFAULTS.keySet());
        keys.add(KEY_PASSWORD);
        return keys;
    }

    public static Collection<String> getAllAdminKeys() {
        Collection<String> keys = new HashSet<>(ALL_KEYS);
        keys.add(KEY_ADMIN_PW);
        return keys;
    }

    /** Writes a key with a value of varying type to a SharedPreferences.Editor. */
    public static void put(SharedPreferences.Editor editor, String key, Object value) {
        if (value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        }
    }
}


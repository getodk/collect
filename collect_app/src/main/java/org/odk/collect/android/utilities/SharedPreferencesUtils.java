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

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.ActionListener;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.preferences.AutoSendPreferenceMigrator;
import org.odk.collect.android.preferences.GeneralSharedPreferences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import timber.log.Timber;

import static org.odk.collect.android.preferences.AdminKeys.ALL_KEYS;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.PreferenceKeys.GENERAL_KEYS;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PASSWORD;

public final class SharedPreferencesUtils {

    private SharedPreferencesUtils() {

    }

    public static String getJSONFromPreferences(Collection<String> passwordKeys) throws JSONException {
        Collection<String> keys = new ArrayList<>(passwordKeys);
        keys.addAll(GENERAL_KEYS.keySet());
        JSONObject sharedPrefJson = getModifiedPrefs(keys);
        Timber.i(sharedPrefJson.toString());
        return sharedPrefJson.toString();
    }

    private static JSONObject getModifiedPrefs(Collection<String> keys) throws JSONException {
        JSONObject prefs = new JSONObject();
        JSONObject adminPrefs = new JSONObject();
        JSONObject generalPrefs = new JSONObject();

        //checking for admin password
        if (keys.contains(KEY_ADMIN_PW)) {
            String password = (String) AdminSharedPreferences.getInstance().get(KEY_ADMIN_PW);
            if (!password.equals("")) {
                adminPrefs.put(KEY_ADMIN_PW, password);
            }
            keys.remove(KEY_ADMIN_PW);
        }

        for (String key : keys) {
            Object defaultValue = GENERAL_KEYS.get(key);
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

    public static void savePreferencesFromString(String content, ActionListener listener) {
        try {
            JSONObject settingsJson = new JSONObject(content);
            JSONObject generalPrefsJson = settingsJson.getJSONObject("general");
            JSONObject adminPrefsJson = settingsJson.getJSONObject("admin");

            for (String key : getAllGeneralKeys()) {
                if (generalPrefsJson.has(key)) {
                    Object value = generalPrefsJson.get(key);
                    GeneralSharedPreferences.getInstance().save(key, value);
                } else {
                    GeneralSharedPreferences.getInstance().reset(key);
                }
            }

            for (String key : getAllAdminKeys()) {

                if (adminPrefsJson.has(key)) {
                    Object value = adminPrefsJson.get(key);
                    AdminSharedPreferences.getInstance().save(key, value);
                } else {
                    AdminSharedPreferences.getInstance().reset(key);
                }
            }

            AuthDialogUtility.setWebCredentialsFromPreferences();
            AutoSendPreferenceMigrator.migrate(generalPrefsJson);

            if (listener != null) {
                listener.onSuccess();
            }
        } catch (JSONException exception) {
            if (listener != null) {
                listener.onFailure(exception);
            }
        }
    }

    public static boolean loadSharedPreferencesFromJSONFile(File src) {
        boolean res = false;
        BufferedReader br = null;

        try {
            String line = null;
            StringBuilder builder = new StringBuilder();
            br = new BufferedReader(new FileReader(src));

            while ((line = br.readLine()) != null) {
                builder.append(line);
            }

            savePreferencesFromString(builder.toString(), null);

            Collect.getInstance().initProperties();
            res = true;
        } catch (IOException e) {
            Timber.e(e, "Exception while loading preferences from file due to : %s ", e.getMessage());
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException ex) {
                Timber.e(ex, "Exception thrown while closing an input stream due to: %s ", ex.getMessage());
            }
        }

        return res;
    }

    public static Collection<String> getAllGeneralKeys() {
        Collection<String> keys = new HashSet<>(GENERAL_KEYS.keySet());
        keys.add(KEY_PASSWORD);
        return keys;
    }

    public static Collection<String> getAllAdminKeys() {
        Collection<String> keys = new HashSet<>(ALL_KEYS);
        keys.add(KEY_ADMIN_PW);
        return keys;
    }
}


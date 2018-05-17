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

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.preferences.AdminKeys.ALL_KEYS;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.PreferenceKeys.GENERAL_KEYS;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PASSWORD;

public final class SharedPreferencesUtils {

    private final AdminSharedPreferences adminSharedPreferences;
    private final GeneralSharedPreferences generalSharedPreferences;

    @Inject
    AuthDialogUtility authDialogUtility;

    @Inject
    public SharedPreferencesUtils(AdminSharedPreferences adminSharedPreferences, GeneralSharedPreferences generalSharedPreferences) {
        this.adminSharedPreferences = adminSharedPreferences;
        this.generalSharedPreferences = generalSharedPreferences;

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

    public String getJSONFromPreferences(Collection<String> passwordKeys) throws JSONException {
        Collection<String> keys = new ArrayList<>(passwordKeys);
        keys.addAll(GENERAL_KEYS.keySet());
        JSONObject sharedPrefJson = getModifiedPrefs(keys);
        Timber.i(sharedPrefJson.toString());
        return sharedPrefJson.toString();
    }

    private JSONObject getModifiedPrefs(Collection<String> keys) throws JSONException {
        JSONObject prefs = new JSONObject();
        JSONObject adminPrefs = new JSONObject();
        JSONObject generalPrefs = new JSONObject();

        //checking for admin password
        if (keys.contains(KEY_ADMIN_PW)) {
            String password = (String) adminSharedPreferences.get(KEY_ADMIN_PW);
            if (!password.equals("")) {
                adminPrefs.put(KEY_ADMIN_PW, password);
            }
            keys.remove(KEY_ADMIN_PW);
        }

        for (String key : keys) {
            Object defaultValue = GENERAL_KEYS.get(key);
            Object value = generalSharedPreferences.get(key);

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

            Object defaultValue = adminSharedPreferences.getDefault(key);
            Object value = adminSharedPreferences.get(key);
            if (defaultValue != value) {
                adminPrefs.put(key, value);
            }
        }
        prefs.put("admin", adminPrefs);

        return prefs;
    }

    public void savePreferencesFromString(String content, ActionListener listener) {
        try {
            JSONObject settingsJson = new JSONObject(content);
            JSONObject generalPrefsJson = settingsJson.getJSONObject("general");
            JSONObject adminPrefsJson = settingsJson.getJSONObject("admin");

            for (String key : getAllGeneralKeys()) {
                if (generalPrefsJson.has(key)) {
                    Object value = generalPrefsJson.get(key);
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

            authDialogUtility.setWebCredentialsFromPreferences();
            AutoSendPreferenceMigrator.migrate(generalSharedPreferences, generalPrefsJson);

            if (listener != null) {
                listener.onSuccess();
            }
        } catch (JSONException exception) {
            if (listener != null) {
                listener.onFailure(exception);
            }
        }
    }

    public boolean loadSharedPreferencesFromJSONFile(File src) {
        try (BufferedReader br = new BufferedReader(new FileReader(src))) {
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line);
            }

            savePreferencesFromString(builder.toString(), null);

            Collect.getInstance().initProperties();
            return true;
        } catch (IOException e) {
            Timber.e(e, "Exception while loading preferences from file due to : %s ", e.getMessage());
        }
        return false;
    }
}


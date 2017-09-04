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

package org.odk.collect.android.preferences;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Set;

import timber.log.Timber;

import static org.odk.collect.android.preferences.PreferenceKeys.GENERAL_KEYS;

public class GeneralSharedPreferences {

    private android.content.SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public GeneralSharedPreferences(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public Object get(String key) {
        Object defaultValue = null;
        Object value = null;

        try {
            defaultValue = GENERAL_KEYS.get(key);
        } catch (Exception e) {
            Timber.e("Default for %s not found", key);
        }

        if (defaultValue == null || defaultValue == "" || defaultValue instanceof String) {
            value = sharedPreferences.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            value = sharedPreferences.getBoolean(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof Long) {
            value = sharedPreferences.getLong(key, (Long) defaultValue);
        } else if (defaultValue instanceof Integer) {
            value = sharedPreferences.getInt(key, (Integer) defaultValue);
        } else if (defaultValue instanceof Float) {
            value = sharedPreferences.getFloat(key, (Float) defaultValue);
        }
        return value;
    }

    public void reset(String key) {
        Object defaultValue = GENERAL_KEYS.get(key);
        save(key, defaultValue);
    }

    public void clearAll() {
        editor.clear();
        editor.apply();
    }

    public void save(String key, Object value) {
        editor = sharedPreferences.edit();
        if (value == null || value == "" || value instanceof String) {
            editor.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Integer) {
            editor.putInt(key, (Integer) value);
        } else if (value instanceof Float) {
            editor.putFloat(key, (Float) value);
        } else if (value instanceof Set) {
            editor.putStringSet(key, (Set<String>) value);
        } else {
            throw new RuntimeException("Unhandled preference value type: " + value);
        }
        editor.apply();
    }

    public boolean getBoolean(String key, boolean value) {
        return sharedPreferences.getBoolean(key, value);
    }
}

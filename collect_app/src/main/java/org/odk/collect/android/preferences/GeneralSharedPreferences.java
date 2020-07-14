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
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerUtils;

import java.util.Map;
import java.util.Set;

import timber.log.Timber;

import static org.odk.collect.android.preferences.GeneralKeys.DEFAULTS;

public class GeneralSharedPreferences {

    private final android.content.SharedPreferences sharedPreferences;

    public GeneralSharedPreferences(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Shouldn't use a static helper to inject instance into objects. Either use constructor
     * injection or Dagger if needed.
     */
    @Deprecated
    public static synchronized GeneralSharedPreferences getInstance() {
        return DaggerUtils.getComponent(Collect.getInstance()).generalSharedPreferences();
    }

    public Object get(String key) {
        if (sharedPreferences == null) {
            return null;
        }

        Object defaultValue = null;
        Object value = null;

        try {
            defaultValue = DEFAULTS.get(key);
        } catch (Exception e) {
            Timber.e("Default for %s not found", key);
        }

        if (defaultValue == null || defaultValue instanceof String) {
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
        Object defaultValue = DEFAULTS.get(key);
        save(key, defaultValue);
    }

    public GeneralSharedPreferences save(String key, @Nullable Object value) {
        Editor editor = sharedPreferences.edit();

        if (value == null || value instanceof String) {
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
        return this;
    }

    public boolean getBoolean(String key, boolean value) {
        return sharedPreferences.getBoolean(key, value);
    }

    public void clear() {
        for (Map.Entry<String, ?> prefs : getAll().entrySet()) {
            reset(prefs.getKey());
        }
    }

    public Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }

    public void loadDefaultPreferences() {
        clear();
        reloadPreferences();
    }

    public void reloadPreferences() {
        for (Map.Entry<String, Object> keyValuePair : GeneralKeys.DEFAULTS.entrySet()) {
            save(keyValuePair.getKey(), get(keyValuePair.getKey()));
        }
    }

    public static boolean isAutoSendEnabled() {
        return !getInstance().get(GeneralKeys.KEY_AUTOSEND).equals("off");
    }

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static class ValidationException extends RuntimeException {
    }
}

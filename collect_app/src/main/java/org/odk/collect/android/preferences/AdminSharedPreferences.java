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


import org.odk.collect.android.application.Collect;

import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.AdminPreferencesFragment.ADMIN_PREFERENCES;

public class AdminSharedPreferences {

    private static AdminSharedPreferences instance = null;
    private android.content.SharedPreferences sharedPreferences;
    private android.content.SharedPreferences.Editor editor;

    private AdminSharedPreferences() {
        sharedPreferences = Collect.getInstance().getSharedPreferences(ADMIN_PREFERENCES, 0);
    }

    public static synchronized AdminSharedPreferences getInstance() {
        if (instance == null) {
            instance = new AdminSharedPreferences();
        }
        return instance;
    }


    public Object get(String key) {
        if (key.equals(KEY_ADMIN_PW)) {
            return sharedPreferences.getString(key, "");
        } else {
            return sharedPreferences.getBoolean(key, true);
        }
    }

    public Object getDefault(String key) {
        if (key.equals(KEY_ADMIN_PW)) {
            return "";
        } else {
            return true;
        }
    }

    public void reset(String key) {
        Object defaultValue = getDefault(key);
        save(key, defaultValue);
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
        }
        editor.apply();
    }

}

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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import java.util.Collection;

import timber.log.Timber;

import static org.odk.collect.android.preferences.AdminKeys.ALL_KEYS;
import static org.odk.collect.android.preferences.PreferenceKeys.ALL_GENERAL_KEYS;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_APP_LANGUAGE;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND_NETWORK;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_AUTOSEND_WIFI;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_CONSTRAINT_BEHAVIOR;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_DELETE_AFTER_SEND;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_FONT_SIZE;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_FORMLIST_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_LAST_VERSION;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_MAP_BASEMAP;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_MAP_SDK;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_NAVIGATION;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SERVER_URL;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SHOW_SPLASH;
import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SUBMISSION_URL;

/**
 * Created by shobhit on 12/4/17.
 */

public class SharedPreferencesUtils {


    private final Context mContext = Collect.getInstance();
    private final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    private final SharedPreferences.Editor mEditor = sharedPrefs.edit();

    private static Collection<String> getAllKeys() {
        Collection<String> allKeys = ALL_KEYS;
        for (String key : ALL_GENERAL_KEYS) {
            allKeys.add(key);
        }
        return allKeys;
    }

    static String getJSONFromPreferences() throws JSONException {
        SharedPreferencesUtils obj = new SharedPreferencesUtils();
        JSONObject sharedPrefJson = obj.getModifiedPrefs();
        Timber.i(sharedPrefJson.toString());
        return sharedPrefJson.toString();
    }


    private JSONObject getModifiedPrefs() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        Collection<String> allKeys = getAllKeys();
        for (String key : allKeys) {
            String stringValue;
            String defaultStringValue;
            try {
                stringValue = getStringValue(key);
                defaultStringValue = getDefaultStringValue(key);
                if (!stringValue.equals(defaultStringValue)) {
                    jsonObject.put(key, stringValue);
                }
            } catch (ClassCastException e) {
                try {
                    boolean booleanValue = getBooleanValue(key);
                    boolean defaultBooleanValue = getDefaultBooleanValue(key);
                    if (booleanValue != defaultBooleanValue) {
                        jsonObject.put(key, booleanValue);
                    }
                } catch (ClassCastException e1) {
                    long longValue = getLongValue(key);
                    long defaultLongValue = getDefaultLongValue(key);
                    if (longValue != defaultLongValue) {
                        jsonObject.put(key, longValue);
                    }
                }
            }
        }
        return jsonObject;
    }


    private String getDefaultStringValue(String key) {
        String defValue;
        switch (key) {
            case KEY_SERVER_URL:
                defValue = mContext.getString(R.string.default_server_url);
                break;
            case KEY_FORMLIST_URL:
                defValue = mContext.getString(R.string.default_odk_formlist);
                break;
            case KEY_SUBMISSION_URL:
                defValue = mContext.getString(R.string.default_odk_submission);
                break;
            case KEY_APP_LANGUAGE:
                defValue = "en";
                break;
            case KEY_NAVIGATION:
                defValue = "swipe";
                break;
            case KEY_CONSTRAINT_BEHAVIOR:
                defValue = "on_swipe";
                break;
            case KEY_FONT_SIZE:
                defValue = "21";
                break;
            case KEY_PROTOCOL:
                defValue = "odk_default";
                break;
            case KEY_MAP_SDK:
                defValue = "google_maps";
                break;
            case KEY_MAP_BASEMAP:
                defValue = "streets";
                break;
            default:
                defValue = "";
        }
        return defValue;
    }

    private boolean getDefaultBooleanValue(String key) {
        boolean defValue;
        switch (key) {
            case KEY_AUTOSEND_WIFI:
            case KEY_AUTOSEND_NETWORK:
            case KEY_DELETE_AFTER_SEND:
            case KEY_SHOW_SPLASH:
                defValue = false;
                break;
            default:
                defValue = true;
        }
        return defValue;
    }

    private String getStringValue(String key) {
        return sharedPrefs.getString(key, getDefaultStringValue(key));
    }

    private boolean getBooleanValue(String key) {
        return sharedPrefs.getBoolean(key, getDefaultBooleanValue(key));
    }

    private long getLongValue(String key) {
        return sharedPrefs.getLong(key, getDefaultLongValue(key));
    }

    private long getDefaultLongValue(String key) {
        long defValue = 0;
        switch (key) {
            case KEY_LAST_VERSION:
                try {
                    defValue = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(),
                            PackageManager.GET_META_DATA).versionCode;
                } catch (PackageManager.NameNotFoundException e) {
                    Timber.e(e, "Unable to get package info");
                }
        }
        return defValue;
    }

    public void savePreferencesFromJSON(JSONObject settingsJson) throws JSONException {

        Collection<String> allKeys = getAllKeys();
        for (String key : allKeys) {
            if (settingsJson.has(key)) {
                try {
                    mEditor.putString(key, settingsJson.getString(key));
                } catch (JSONException e) {
                    try {
                        mEditor.putBoolean(key, settingsJson.getBoolean(key));
                    } catch (JSONException e1) {
                        mEditor.putLong(key, settingsJson.getLong(key));
                    }
                }
            }
        }
        mEditor.apply();

        //settings import confirmation toast
        ToastUtils.showLongToast(mContext.getString(R.string.successfully_imported_settings));
    }
}


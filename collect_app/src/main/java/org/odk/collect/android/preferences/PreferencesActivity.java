/*
 * Copyright (C) 2011 University of Washington
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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore.Images;
import android.util.Log;

import org.javarosa.core.services.IPropertyManager;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.utilities.MediaUtils;

import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SPLASH_PATH;

/**
 * Handles general preferences.
 *
 * @author Thomas Smyth, Sassafras Tech Collective (tom@sassafrastech.com;
 *         constraint behavior option)
 */
public class PreferencesActivity extends PreferenceActivity {
    public static final String INTENT_KEY_ADMIN_MODE = "adminMode";
    protected static final int IMAGE_CHOOSER = 0;
    private static final String t = "PreferencesActivity";
    private PreferencesFragment fragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(t, "onCreate");
        super.onCreate(savedInstanceState);
        fragment = new PreferencesFragment();
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, fragment)
                .commit();

        setTitle(getString(R.string.general_preferences));

    }

    @Override
    public void onStart() {
        Log.d(t, "onStart");
        super.onStart();
    }


    @Override
    protected void onPause() {
        Log.d(t, "onPause");
        super.onPause();

        // the property manager should be re-assigned, as properties
        // may have changed.
        IPropertyManager mgr = new PropertyManager(this);
        FormController.initializeJavaRosa(mgr);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(t, "onActivityResult " + requestCode + " " + resultCode);
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_CANCELED) {
            // request was canceled, so do nothing
            return;
        }

        switch (requestCode) {
            case IMAGE_CHOOSER:

                // get gp of chosen file
                Uri selectedMedia = intent.getData();
                String sourceMediaPath = MediaUtils.getPathFromUri(this, selectedMedia,
                        Images.Media.DATA);

                // setting image path
                setSplashPath(sourceMediaPath);
                break;
        }
    }

    @Override
    protected void onStop() {
        Log.d(t, "onStop");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Log.d(t, "onRestart");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Log.d(t, "onDestroy");
        super.onDestroy();
    }

    void setSplashPath(String path) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SPLASH_PATH, path);
        editor.commit();

        PreferenceScreen splashPathPreference = (PreferenceScreen) fragment.findPreference(KEY_SPLASH_PATH);
        String summary = splashPathPreference.getSharedPreferences().getString(
                KEY_SPLASH_PATH, getString(R.string.default_splash_path));
        splashPathPreference.setSummary(summary);
    }
}

/*
 * Copyright (C) 2017 University of Washington
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

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.MediaStore.Images;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.javarosa.core.services.IPropertyManager;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.utilities.MediaUtils;

import timber.log.Timber;

import static org.odk.collect.android.preferences.PreferenceKeys.KEY_SPLASH_PATH;

/**
 * Handles general preferences.
 *
 * @author Thomas Smyth, Sassafras Tech Collective (tom@sassafrastech.com;
 *         constraint behavior option)
 */
public class PreferencesActivity extends AppCompatActivity {
    public static final String INTENT_KEY_ADMIN_MODE = "adminMode";
    protected static final int IMAGE_CHOOSER = 0;
    private PreferencesFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.d("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.general_preferences));
        setSupportActionBar(toolbar);

        fragment = new PreferencesFragment();
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    @Override
    public void onStart() {
        Timber.d("onStart");
        super.onStart();
    }

    @Override
    protected void onPause() {
        Timber.d("onPause");
        super.onPause();

        // the property manager should be re-assigned, as properties
        // may have changed.
        IPropertyManager mgr = new PropertyManager(this);
        FormController.initializeJavaRosa(mgr);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Timber.d("onActivityResult %d %d", requestCode, resultCode);
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
        Timber.d("onStop");
        super.onStop();
    }

    @Override
    protected void onRestart() {
        Timber.d("onRestart");
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        Timber.d("onDestroy");
        super.onDestroy();
    }

    void setSplashPath(String path) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sharedPreferences.edit();
        editor.putString(KEY_SPLASH_PATH, path);
        editor.apply();

        PreferenceScreen splashPathPreference = (PreferenceScreen) fragment.findPreference(KEY_SPLASH_PATH);
        String summary = splashPathPreference.getSharedPreferences().getString(
                KEY_SPLASH_PATH, getString(R.string.default_splash_path));
        splashPathPreference.setSummary(summary);
    }
}

/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.preferences.screens;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.listeners.OnBackPressedListener;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.utilities.ThemeUtils;

import javax.inject.Inject;

public class GeneralPreferencesActivity extends CollectAbstractActivity {

    public static final String TAG = "GeneralPreferencesFragment";
    public static final String INTENT_KEY_ADMIN_MODE = "adminMode";

    private OnBackPressedListener onBackPressedListener;

    @Inject
    PropertyManager propertyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences_layout);
        DaggerUtils.getComponent(this).inject(this);

        setTheme(new ThemeUtils(this).getSettingsTheme());

        setTitle(R.string.general_preferences);
        if (savedInstanceState == null) {
            Fragment fragment = new GeneralPreferencesFragment();
            fragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.preferences_fragment_container, fragment, TAG)
                    .commit();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        propertyManager.reload();
    }

    // If the onBackPressedListener is set then onBackPressed is delegated to it.
    @Override
    public void onBackPressed() {
        if (onBackPressedListener != null) {
            onBackPressedListener.doBack();
        } else {
            super.onBackPressed();
        }
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }
}

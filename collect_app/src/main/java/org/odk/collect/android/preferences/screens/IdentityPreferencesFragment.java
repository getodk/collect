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

package org.odk.collect.android.preferences.screens;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.CheckBoxPreference;

import org.odk.collect.android.R;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.MultiClickGuard;

import javax.inject.Inject;

import static org.odk.collect.android.preferences.keys.GeneralKeys.KEY_ANALYTICS;

public class IdentityPreferencesFragment extends BaseGeneralPreferencesFragment {

    @Inject
    Analytics analytics;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Collect.getInstance().getComponent().inject(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.identity_preferences, rootKey);

        findPreference("form_metadata").setOnPreferenceClickListener(preference -> {
            if (MultiClickGuard.allowClick(getClass().getName())) {
                Fragment fragment = new FormMetadataPreferencesFragment();
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.preferences_fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
                return true;
            }
            return false;
        });

        initAnalyticsPref();
    }

    private void initAnalyticsPref() {
        final CheckBoxPreference analyticsPreference = (CheckBoxPreference) findPreference(KEY_ANALYTICS);

        if (analyticsPreference != null) {
            analyticsPreference.setOnPreferenceClickListener(preference -> {
                analytics.setAnalyticsCollectionEnabled(analyticsPreference.isChecked());
                return true;
            });
        }
    }
}

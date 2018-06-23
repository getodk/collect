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

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.android.gms.analytics.GoogleAnalytics;

import org.odk.collect.android.R;

import static org.odk.collect.android.preferences.PreferenceKeys.KEY_ANALYTICS;

public class IdentityPreferences extends BasePreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.identity_preferences);

        findPreference("form_metadata").setOnPreferenceClickListener(preference -> {
            getActivity().getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new FormMetadataFragment())
                    .addToBackStack(null)
                    .commit();
            return true;
        });

        initAnalyticsPref();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        toolbar.setTitle(R.string.user_and_device_identity_title);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (toolbar != null) {
            toolbar.setTitle(R.string.general_preferences);
        }
    }

    private void initAnalyticsPref() {
        final CheckBoxPreference analyticsPreference = (CheckBoxPreference) findPreference(KEY_ANALYTICS);

        if (analyticsPreference != null) {
            analyticsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    GoogleAnalytics googleAnalytics = GoogleAnalytics.getInstance(getActivity().getApplicationContext());
                    googleAnalytics.setAppOptOut(!analyticsPreference.isChecked());
                    return true;
                }
            });
        }
    }
}

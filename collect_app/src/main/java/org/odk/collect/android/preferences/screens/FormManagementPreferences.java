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
import androidx.annotation.Nullable;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.backgroundwork.FormUpdateManager;
import org.odk.collect.android.preferences.PreferencesDataSource;
import org.odk.collect.android.preferences.Protocol;

import javax.inject.Inject;

import static org.odk.collect.android.analytics.AnalyticsEvents.AUTO_FORM_UPDATE_PREF_CHANGE;
import static org.odk.collect.android.configure.SettingsUtils.getFormUpdateMode;
import static org.odk.collect.android.preferences.AdminKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_AUTOMATIC_UPDATE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_AUTOSEND;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_CONSTRAINT_BEHAVIOR;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_FORM_UPDATE_MODE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_GUIDANCE_HINT;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_IMAGE_SIZE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.utilities.PreferencesUtils.displayDisabled;

public class FormManagementPreferences extends BaseGeneralPreferencesFragment {

    @Inject
    Analytics analytics;

    @Inject
    FormUpdateManager formUpdateManager;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Collect.getInstance().getComponent().inject(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.form_management_preferences, rootKey);

        initListPref(KEY_PERIODIC_FORM_UPDATES_CHECK);
        initPref(KEY_AUTOMATIC_UPDATE);
        initListPref(KEY_CONSTRAINT_BEHAVIOR);
        initListPref(KEY_AUTOSEND);
        initListPref(KEY_IMAGE_SIZE);
        initGuidancePrefs();

        updateDisabledPrefs();
    }

    @Override
    public void onPreferenceChanged(@NotNull String key) {
        super.onPreferenceChanged(key);

        if (key.equals(KEY_FORM_UPDATE_MODE) || key.equals(KEY_PERIODIC_FORM_UPDATES_CHECK)) {
            updateDisabledPrefs();
        }
    }

    private void updateDisabledPrefs() {
        PreferencesDataSource generalPrefs = preferencesDataSourceProvider.getGeneralPreferences();

        // Might be null if disabled in Admin settings
        @Nullable Preference updateFrequency = findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK);
        @Nullable CheckBoxPreference automaticDownload = findPreference(KEY_AUTOMATIC_UPDATE);

        if (Protocol.parse(getActivity(), generalPrefs.getString(KEY_PROTOCOL)) == Protocol.GOOGLE) {
            displayDisabled(findPreference(KEY_FORM_UPDATE_MODE), getString(R.string.manual));
            if (automaticDownload != null) {
                displayDisabled(automaticDownload, false);
            }
            if (updateFrequency != null) {
                updateFrequency.setEnabled(false);
            }
        } else {
            switch (getFormUpdateMode(requireContext(), generalPrefs)) {
                case MANUAL:
                    if (automaticDownload != null) {
                        displayDisabled(automaticDownload, false);
                    }
                    if (updateFrequency != null) {
                        updateFrequency.setEnabled(false);
                    }
                    break;
                case PREVIOUSLY_DOWNLOADED_ONLY:
                    if (automaticDownload != null) {
                        automaticDownload.setEnabled(true);
                        automaticDownload.setChecked(generalPrefs.getBoolean(KEY_AUTOMATIC_UPDATE));
                    }
                    if (updateFrequency != null) {
                        updateFrequency.setEnabled(true);
                    }
                    break;
                case MATCH_EXACTLY:
                    if (automaticDownload != null) {
                        displayDisabled(automaticDownload, true);
                    }
                    if (updateFrequency != null) {
                        updateFrequency.setEnabled(true);
                    }
                    break;
            }
        }
    }

    private void initListPref(String key) {
        final ListPreference pref = findPreference(key);

        if (pref != null) {
            pref.setSummary(pref.getEntry());
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                CharSequence entry = ((ListPreference) preference).getEntries()[index];
                preference.setSummary(entry);

                if (key.equals(KEY_PERIODIC_FORM_UPDATES_CHECK)) {
                    analytics.logEvent(AUTO_FORM_UPDATE_PREF_CHANGE, "Periodic form updates check", (String) newValue);
                }
                return true;
            });
            if (key.equals(KEY_CONSTRAINT_BEHAVIOR)) {
                pref.setEnabled(preferencesDataSourceProvider.getAdminPreferences().getBoolean(ALLOW_OTHER_WAYS_OF_EDITING_FORM));
            }
        }
    }

    private void initPref(String key) {
        final Preference pref = findPreference(key);

        if (pref != null) {
            if (key.equals(KEY_AUTOMATIC_UPDATE)) {
                String formUpdateCheckPeriod = preferencesDataSourceProvider.getGeneralPreferences().getString(KEY_PERIODIC_FORM_UPDATES_CHECK);

                // Only enable automatic form updates if periodic updates are set
                pref.setEnabled(!formUpdateCheckPeriod.equals(getString(R.string.never_value)));

                pref.setOnPreferenceChangeListener((preference, newValue) -> {
                    analytics.logEvent(AUTO_FORM_UPDATE_PREF_CHANGE, "Automatic form updates", newValue + " " + formUpdateCheckPeriod);

                    return true;
                });
            }
        }
    }

    private void initGuidancePrefs() {
        final ListPreference guidance = findPreference(KEY_GUIDANCE_HINT);

        if (guidance == null) {
            return;
        }

        guidance.setSummary(guidance.getEntry());
        guidance.setOnPreferenceChangeListener((preference, newValue) -> {
            int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
            String entry = (String) ((ListPreference) preference).getEntries()[index];
            preference.setSummary(entry);
            return true;
        });
    }
}

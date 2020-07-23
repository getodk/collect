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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

import org.odk.collect.android.R;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.backgroundwork.FormUpdateManager;
import org.odk.collect.android.formmanagement.FormUpdateMode;

import javax.inject.Inject;

import static org.odk.collect.android.analytics.AnalyticsEvents.AUTO_FORM_UPDATE_PREF_CHANGE;
import static org.odk.collect.android.preferences.AdminKeys.ALLOW_OTHER_WAYS_OF_EDITING_FORM;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_AUTOMATIC_UPDATE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_AUTOSEND;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_CONSTRAINT_BEHAVIOR;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_FORM_UPDATE_MODE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_GUIDANCE_HINT;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_IMAGE_SIZE;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PROTOCOL;
import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

public class FormManagementPreferences extends BasePreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    Analytics analytics;

    @Inject
    PreferencesProvider preferencesProvider;

    @Inject
    FormUpdateManager formUpdateManager;

    public static FormManagementPreferences newInstance(boolean adminMode) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(INTENT_KEY_ADMIN_MODE, adminMode);

        FormManagementPreferences formManagementPreferences = new FormManagementPreferences();
        formManagementPreferences.setArguments(bundle);

        return formManagementPreferences;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.form_management_preferences);
        Collect.getInstance().getComponent().inject(this);

        initListPref(KEY_PERIODIC_FORM_UPDATES_CHECK);
        initPref(KEY_AUTOMATIC_UPDATE);
        initListPref(KEY_CONSTRAINT_BEHAVIOR);
        initListPref(KEY_AUTOSEND);
        initListPref(KEY_IMAGE_SIZE);
        initGuidancePrefs();

        setupFormUpdateMode();
    }

    private void setupFormUpdateMode() {
        SharedPreferences sharedPreferences = preferencesProvider.getGeneralSharedPreferences();
        updateDisabledPrefs(sharedPreferences.getString(KEY_FORM_UPDATE_MODE, null), sharedPreferences.getString(KEY_PROTOCOL, null));

        Preference formUpdateMode = findPreference(KEY_FORM_UPDATE_MODE);
        formUpdateMode.setSummary(((ListPreference) formUpdateMode).getEntry());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void updateDisabledPrefs(String formUpdateMode, String protocol) {
        if (Protocol.parse(getActivity(), protocol) == Protocol.GOOGLE) {
            findPreference(KEY_FORM_UPDATE_MODE).setEnabled(false);
            findPreference(KEY_AUTOMATIC_UPDATE).setEnabled(false);
            findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).setEnabled(false);
        } else {
            switch (FormUpdateMode.parse(getActivity(), formUpdateMode)) {
                case MANUAL:
                    findPreference(KEY_AUTOMATIC_UPDATE).setEnabled(false);
                    findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).setEnabled(false);
                    break;
                case PREVIOUSLY_DOWNLOADED_ONLY:
                    findPreference(KEY_AUTOMATIC_UPDATE).setEnabled(true);
                    findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).setEnabled(true);
                    break;
                case MATCH_EXACTLY:
                    findPreference(KEY_AUTOMATIC_UPDATE).setEnabled(false);
                    findPreference(KEY_PERIODIC_FORM_UPDATES_CHECK).setEnabled(true);
                    break;
            }
        }
    }

    private void initListPref(String key) {
        final ListPreference pref = (ListPreference) findPreference(key);

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
                pref.setEnabled((Boolean) AdminSharedPreferences.getInstance().get(ALLOW_OTHER_WAYS_OF_EDITING_FORM));
            }
        }
    }

    private void initPref(String key) {
        final Preference pref = findPreference(key);

        if (pref != null) {
            if (key.equals(KEY_AUTOMATIC_UPDATE)) {
                String formUpdateCheckPeriod = (String) GeneralSharedPreferences.getInstance()
                        .get(KEY_PERIODIC_FORM_UPDATES_CHECK);

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
        final ListPreference guidance = (ListPreference) findPreference(KEY_GUIDANCE_HINT);

        if (guidance == null) {
            return;
        }

        guidance.setSummary(guidance.getEntry());
        guidance.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                String entry = (String) ((ListPreference) preference).getEntries()[index];
                preference.setSummary(entry);
                return true;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_FORM_UPDATE_MODE) || key.equals(KEY_PERIODIC_FORM_UPDATES_CHECK)) {
            formUpdateManager.scheduleUpdates();

            String newValue = sharedPreferences.getString(KEY_FORM_UPDATE_MODE, null);
            updateDisabledPrefs(newValue, sharedPreferences.getString(KEY_PROTOCOL, null));

            Preference preference = findPreference(KEY_FORM_UPDATE_MODE);
            preference.setSummary(((ListPreference) preference).getEntry());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        preferencesProvider.getGeneralSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}

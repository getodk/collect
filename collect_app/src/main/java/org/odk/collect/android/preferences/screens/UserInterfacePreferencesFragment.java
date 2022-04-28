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

import static org.odk.collect.android.activities.ActivityUtils.startActivityAndCloseAllOthers;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_APP_LANGUAGE;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_APP_THEME;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_FONT_SIZE;
import static org.odk.collect.settings.keys.ProjectKeys.KEY_NAVIGATION;

import android.content.Context;
import android.os.Bundle;
import androidx.preference.ListPreference;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.utilities.LocaleHelper;
import org.odk.collect.android.version.VersionInformation;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.inject.Inject;

public class UserInterfacePreferencesFragment extends BaseProjectPreferencesFragment {

    @Inject
    VersionInformation versionInformation;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.user_interface_preferences, rootKey);

        initThemePrefs();
        initNavigationPrefs();
        initFontSizePref();
        initLanguagePrefs();
    }

    private void initThemePrefs() {
        final ListPreference pref = findPreference(KEY_APP_THEME);

        if (pref != null) {
            pref.setSummary(pref.getEntry());
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                String entry = (String) ((ListPreference) preference).getEntries()[index];
                if (pref.getEntry() == null || !pref.getEntry().equals(entry)) {
                    preference.setSummary(entry);
                    startActivityAndCloseAllOthers(getActivity(), MainMenuActivity.class);
                }
                return true;
            });
        }
    }

    private void initNavigationPrefs() {
        final ListPreference pref = findPreference(KEY_NAVIGATION);

        if (pref != null) {
            pref.setSummary(pref.getEntry());
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                String entry = (String) ((ListPreference) preference).getEntries()[index];
                preference.setSummary(entry);
                return true;
            });
        }
    }

    private void initFontSizePref() {
        final ListPreference pref = findPreference(KEY_FONT_SIZE);

        if (pref != null) {
            pref.setSummary(pref.getEntry());
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                CharSequence entry = ((ListPreference) preference).getEntries()[index];
                preference.setSummary(entry);
                return true;
            });
        }
    }

    private void initLanguagePrefs() {
        final ListPreference pref = findPreference(KEY_APP_LANGUAGE);

        if (pref != null) {
            final LocaleHelper localeHelper = new LocaleHelper();
            TreeMap<String, String> languageList = localeHelper.getEntryListValues();
            ArrayList<String> entryValues = new ArrayList<>();
            entryValues.add(0, "");
            entryValues.addAll(languageList.values());
            pref.setEntryValues(entryValues.toArray(new String[0]));
            ArrayList<String> entries = new ArrayList<>();
            entries.add(0, getActivity().getResources()
                    .getString(R.string.use_device_language));
            entries.addAll(languageList.keySet());
            pref.setEntries(entries.toArray(new String[0]));
            if (pref.getValue() == null) {
                //set Default value to "Use phone locale"
                pref.setValueIndex(0);
            }
            pref.setSummary(pref.getEntry());
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                int index = ((ListPreference) preference).findIndexOfValue(newValue.toString());
                String entry = (String) ((ListPreference) preference).getEntries()[index];
                preference.setSummary(entry);

                settingsProvider.getUnprotectedSettings().save(KEY_APP_LANGUAGE, newValue.toString());

                startActivityAndCloseAllOthers(getActivity(), MainMenuActivity.class);
                return true;
            });
        }
    }
}

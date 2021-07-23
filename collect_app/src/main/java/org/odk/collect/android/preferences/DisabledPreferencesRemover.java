/*
 * Copyright 2017 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.preferences;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;

import org.odk.collect.android.preferences.keys.AdminAndGeneralKeys;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.shared.Settings;

import timber.log.Timber;

public class DisabledPreferencesRemover {

    private final PreferenceFragmentCompat pf;
    private final Settings adminSettings;

    public DisabledPreferencesRemover(PreferenceFragmentCompat pf, Settings adminSettings) {
        this.pf = pf;
        this.adminSettings = adminSettings;
    }

    public void hideDisabledPref(AdminAndGeneralKeys... keyPairs) {
        hideDisabledPreferences(keyPairs);
        hideEmptyCategories(pf.getPreferenceScreen());
    }

    /**
     * Removes any preferences from the category that are excluded by the admin settings.
     *
     * @param keyPairs one or more AdminAndGeneralKeys objects.
     */
    private void hideDisabledPreferences(AdminAndGeneralKeys... keyPairs) {
        for (AdminAndGeneralKeys agKeys : keyPairs) {
            boolean prefAllowed = adminSettings.getBoolean(agKeys.adminKey);

            if (!prefAllowed) {
                Preference preference = pf.findPreference(agKeys.generalKey);

                if (preference == null) {
                    // preference not found in the current preference fragment, so ignore
                    continue;
                }

                preference.setVisible(false);
                Timber.d("Removed %s", preference.toString());
            }
        }
    }

    /**
     * Deletes all empty PreferenceCategory items.
     */
    private void hideEmptyCategories(PreferenceGroup pc) {
        if (pc == null) {
            return;
        }

        for (int i = 0; i < pc.getPreferenceCount(); i++) {
            Preference preference = pc.getPreference(i);

            if (preference instanceof PreferenceGroup) {

                if (!hideEmptyPreference(pc, preference)) {
                    hideEmptyCategories((PreferenceGroup) preference);

                    // try to remove preference group if it is empty now
                    hideEmptyPreference(pc, preference);
                }
            }
        }
    }

    private boolean hideEmptyPreference(PreferenceGroup pc, Preference preference) {
        if (((PreferenceGroup) preference).getPreferenceCount() == 0
                && hasChildPrefs(preference.getKey())) {
            pc.removePreference(preference);
            Timber.d("Removed %s", preference.toString());
            return true;
        }
        return false;
    }

    /**
     * Checks whether the preferenceGroup actually has any child preferences defined
     */
    private boolean hasChildPrefs(String preferenceKey) {
        String[] preferenceScreensWithNoChildren = {
                GeneralKeys.KEY_SPLASH_PATH,
                GeneralKeys.KEY_FORM_METADATA
        };

        for (String pref : preferenceScreensWithNoChildren) {
            if (pref.equals(preferenceKey)) {
                return false;
            }
        }
        return true;
    }
}

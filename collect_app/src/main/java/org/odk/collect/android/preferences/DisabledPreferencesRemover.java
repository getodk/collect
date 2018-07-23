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

import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

import timber.log.Timber;

import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

class DisabledPreferencesRemover {

    private final PreferencesActivity pa;
    private final PreferenceFragment pf;

    DisabledPreferencesRemover(PreferencesActivity pa, PreferenceFragment pf) {
        this.pa = pa;
        this.pf = pf;
    }

    /**
     * Removes any preferences from the category that are excluded by the admin settings.
     *
     * @param keyPairs one or more AdminAndGeneralKeys objects.
     */
    void remove(AdminAndGeneralKeys... keyPairs) {
        for (AdminAndGeneralKeys agKeys : keyPairs) {
            boolean prefAllowed = (boolean) AdminSharedPreferences.getInstance().get(agKeys.adminKey);

            if (!prefAllowed) {
                Preference preference = pf.findPreference(agKeys.generalKey);

                if (preference == null) {
                    // preference not found in the current preference fragment, so ignore
                    continue;
                }

                PreferenceGroup parent = getParent(pf.getPreferenceScreen(), preference);
                if (parent == null) {
                    throw new RuntimeException("Couldn't find preference");
                }

                parent.removePreference(preference);
                Timber.d("Removed %s", preference.toString());
            }
        }
    }

    private PreferenceGroup getParent(PreferenceGroup groupToSearchIn, Preference preference) {
        for (int i = 0; i < groupToSearchIn.getPreferenceCount(); ++i) {
            Preference child = groupToSearchIn.getPreference(i);

            if (child == preference) {
                return groupToSearchIn;
            }

            if (child instanceof PreferenceGroup) {
                PreferenceGroup childGroup = (PreferenceGroup) child;
                PreferenceGroup result = getParent(childGroup, preference);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * Deletes all empty PreferenceCategory items.
     */
    void removeEmptyCategories() {
        removeEmptyCategories(pf.getPreferenceScreen());
        removeEmptyCategories(pf.getPreferenceScreen());
    }

    private void removeEmptyCategories(PreferenceGroup pc) {

        final boolean adminMode = pa.getIntent().getBooleanExtra(INTENT_KEY_ADMIN_MODE, false);
        if (adminMode || pc == null) {
            return;
        }

        for (int i = 0; i < pc.getPreferenceCount(); i++) {
            Preference preference = pc.getPreference(i);

            if (preference instanceof PreferenceGroup) {

                if (!removeEmptyPreference(pc, preference)) {
                    removeEmptyCategories((PreferenceGroup) preference);

                    // try to remove preference group if it is empty now
                    removeEmptyPreference(pc, preference);
                }
            }
        }
    }

    private boolean removeEmptyPreference(PreferenceGroup pc, Preference preference) {
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
                PreferenceKeys.KEY_SPLASH_PATH,
                PreferenceKeys.KEY_FORM_METADATA
        };

        for (String pref : preferenceScreensWithNoChildren) {
            if (pref.equals(preferenceKey)) {
                return false;
            }
        }
        return true;
    }
}

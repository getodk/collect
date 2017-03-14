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

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

class DisabledPreferencesRemover {
    private static final String t = "DisabledPrefRemover"; // Full name was too long

    /** A map used to find the parent category of any preference */ // ToDo: find a better way?
    private final Map<Preference, PreferenceCategory> preferencePreferenceCategoryMap;

    private PreferencesActivity pa;
    private PreferencesFragment pf;

    DisabledPreferencesRemover(PreferencesActivity pa, PreferencesFragment pf) {
        this.pa = pa;
        this.pf = pf;
        preferencePreferenceCategoryMap = createPreferenceToPreferenceCategoryMap();
    }

    private Map<Preference, PreferenceCategory> createPreferenceToPreferenceCategoryMap() {
        final Map<Preference, PreferenceCategory> map = new HashMap<>();
        PreferenceScreen screen = pf.getPreferenceScreen();
        for (int i = 0; i < screen.getPreferenceCount(); i++) {
            Preference p = screen.getPreference(i);
            if (p instanceof PreferenceCategory) {
                PreferenceCategory pc = (PreferenceCategory) p;
                for (int j = 0; j < pc.getPreferenceCount(); ++j) {
                    map.put(pc.getPreference(j), pc);
                }
            }
        }
        return map;
    }

    /**
     * Removes any preferences from the category that are excluded by the admin settings.
     *
     * @param keyPairs one or more AdminAndGeneralKeys objects.
     */
    void remove(AdminAndGeneralKeys... keyPairs) {
        final boolean adminMode = pa.getIntent().getBooleanExtra(INTENT_KEY_ADMIN_MODE, false);

        final SharedPreferences adminPreferences = pa.getSharedPreferences(
                AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

        for (AdminAndGeneralKeys agKeys : keyPairs) {
            final boolean prefAllowed = adminPreferences.getBoolean(agKeys.adminKey, true);

            if (!prefAllowed && !adminMode) {
                Preference pref = pf.findPreference(agKeys.generalKey);
                PreferenceCategory preferenceCategory = preferencePreferenceCategoryMap.get(pref);
                if (preferenceCategory != null && pref != null) { // Neither should ever be null
                    preferenceCategory.removePreference(pref);
                    Log.d(t, "Removed " + pref.toString());
                }
            }
        }
    }

    /** Deletes all empty PreferenceCategory items. */
    void removeEmptyCategories() {
        final boolean adminMode = pa.getIntent().getBooleanExtra(INTENT_KEY_ADMIN_MODE, false);
        HashSet<PreferenceCategory> uniqueCategories = new
                HashSet<>(preferencePreferenceCategoryMap.values());
        for (PreferenceCategory pc : uniqueCategories) {
            if (pc.getPreferenceCount() == 0 && !adminMode) {
                pf.getPreferenceScreen().removePreference(pc);
            }
        }
    }
}

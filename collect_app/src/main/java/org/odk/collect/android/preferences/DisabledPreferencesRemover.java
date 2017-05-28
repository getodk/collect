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
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import timber.log.Timber;

import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

class DisabledPreferencesRemover {

    /**
     * A map used to find the parent category of any preference
     */ // ToDo: find a better way?
    private final Map<Preference, Object> preferencePreferenceCategoryMap;

    private PreferencesActivity pa;
    private PreferencesFragment pf;

    DisabledPreferencesRemover(PreferencesActivity pa, PreferencesFragment pf) {
        this.pa = pa;
        this.pf = pf;
        preferencePreferenceCategoryMap = createPreferenceToPreferenceCategoryMap();
    }

    private Map<Preference, Object> createPreferenceToPreferenceCategoryMap() {
        final Map<Preference, Object> map = new HashMap<>();
        PreferenceScreen screen = pf.getPreferenceScreen();
        for (int i = 0; i < screen.getPreferenceCount(); i++) {
            Preference p = screen.getPreference(i);
            if (p instanceof PreferenceCategory) {
                PreferenceCategory pc = (PreferenceCategory) p;
                for (int j = 0; j < pc.getPreferenceCount(); ++j) {
                    map.put(pc.getPreference(j), pc);
                }
            } else if (p instanceof PreferenceScreen) {
                PreferenceScreen pc = (PreferenceScreen) p;
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

        for (AdminAndGeneralKeys agKeys : keyPairs) {
            boolean prefAllowed = (boolean) AdminSharedPreferences.getInstance().get(agKeys.adminKey);

            if (!prefAllowed && !adminMode) {
                Preference pref = pf.findPreference(agKeys.generalKey);
                Object pc = preferencePreferenceCategoryMap.get(pref);
                if (pc != null && pref != null) { // Neither should ever be null
                    if (pc instanceof PreferenceScreen) {
                        ((PreferenceScreen) pc).removePreference(pref);
                        Timber.d("Removed %s", pref.toString());
                    } else if (pc instanceof PreferenceCategory) {
                        ((PreferenceCategory) pc).removePreference(pref);
                        Timber.d("Removed %s", pref.toString());
                    }
                }
            }
        }
    }

    /**
     * Deletes all empty PreferenceCategory items.
     */
    void removeEmptyCategories() {
        final boolean adminMode = pa.getIntent().getBooleanExtra(INTENT_KEY_ADMIN_MODE, false);
        HashSet<Object> uniqueCategories = new
                HashSet<>(preferencePreferenceCategoryMap.values());
        if (adminMode) {
            return;
        }
        for (Object pc : uniqueCategories) {
            if (pc instanceof PreferenceCategory) {
                PreferenceCategory preferenceCategory = (PreferenceCategory) pc;
                if (preferenceCategory.getPreferenceCount() == 0) {
                    pf.getPreferenceScreen().removePreference(preferenceCategory);
                }
            } else if (pc instanceof PreferenceScreen) {
                PreferenceScreen preferenceScreen = (PreferenceScreen) pc;
                if (preferenceScreen.getPreferenceCount() == 0) {
                    pf.getPreferenceScreen().removePreference(preferenceScreen);
                }
            }
        }
    }
}

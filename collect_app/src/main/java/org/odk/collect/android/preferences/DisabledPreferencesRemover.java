package org.odk.collect.android.preferences;

import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.odk.collect.android.preferences.PreferencesActivity.INTENT_KEY_ADMIN_MODE;

class DisabledPreferencesRemover {
    /** A map used to find the parent category of any preference */ // ToDo: find a better way?
    private final Map<Preference, PreferenceCategory> preferencePreferenceCategoryMap;

    private PreferencesActivity pa;

    DisabledPreferencesRemover(PreferencesActivity pa) {
        this.pa = pa;
        preferencePreferenceCategoryMap = createPreferenceToPreferenceCategoryMap();
    }

    private Map<Preference, PreferenceCategory> createPreferenceToPreferenceCategoryMap() {
        final Map<Preference, PreferenceCategory> map = new HashMap<>();
        PreferenceScreen screen = pa.getPreferenceScreen();
        int numPrefs = screen.getPreferenceCount();
        for (int i = 0; i < numPrefs; i++) {
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
     * @param adminAndGeneralKeyses one or more AdminAndGeneralKeys objects.
     */
    void remove(AdminAndGeneralKeys... adminAndGeneralKeyses) {
        final boolean adminMode = pa.getIntent().getBooleanExtra(INTENT_KEY_ADMIN_MODE, false);

        final SharedPreferences adminPreferences = pa.getSharedPreferences(
                AdminPreferencesActivity.ADMIN_PREFERENCES, 0);

        for (AdminAndGeneralKeys keys : adminAndGeneralKeyses) {
            final boolean prefAllowed = adminPreferences.getBoolean(keys.adminKey, true);

            if (!prefAllowed && !adminMode) {
                Preference pref = pa.pref(keys.generalKey);
                PreferenceCategory preferenceCategory = preferencePreferenceCategoryMap.get(pref);
                if (preferenceCategory != null && pref != null) { // Neither should ever be null
                    preferenceCategory.removePreference(pref);
                }
            }
        }
    }

    /** Deletes all empty PreferenceCategory items. */
    void removeEmptyCategories() {
        final boolean adminMode = pa.getIntent().getBooleanExtra(INTENT_KEY_ADMIN_MODE, false);
        for (PreferenceCategory pc : new HashSet<>(preferencePreferenceCategoryMap.values())) {
            if (pc.getPreferenceCount() == 0 && !adminMode) {
                pa.getPreferenceScreen().removePreference(pc);
            }
        }
    }
}

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
package org.odk.collect.android.preferences

import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import org.odk.collect.android.preferences.keys.AdminAndGeneralKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.shared.Settings
import timber.log.Timber

class DisabledPreferencesRemover(
    private val pf: PreferenceFragmentCompat,
    private val adminSettings: Settings
) {
    fun hideDisabledPref(adminAndGeneralKeys: Array<AdminAndGeneralKeys>) {
        hideDisabledPreferences(adminAndGeneralKeys)
        hideEmptyCategories(pf.preferenceScreen)
    }

    /**
     * Removes any preferences from the category that are excluded by the admin settings.
     *
     * @param keyPairs one or more AdminAndGeneralKeys objects.
     */
    private fun hideDisabledPreferences(adminAndGeneralKeys: Array<AdminAndGeneralKeys>) {
        for (agKeys in adminAndGeneralKeys) {
            val prefAllowed = adminSettings.getBoolean(agKeys.adminKey)
            if (!prefAllowed) {
                val preference = pf.findPreference<Preference>(agKeys.generalKey) ?: continue
                preference.isVisible = false
                Timber.d("Removed %s", preference.toString())
            }
        }
    }

    /**
     * Deletes all empty PreferenceCategory items.
     */
    private fun hideEmptyCategories(pc: PreferenceGroup?) {
        if (pc == null) {
            return
        }
        for (i in 0 until pc.preferenceCount) {
            val preference = pc.getPreference(i)
            if (preference is PreferenceGroup) {
                if (!hideEmptyPreference(pc, preference)) {
                    hideEmptyCategories(preference)

                    // try to remove preference group if it is empty now
                    hideEmptyPreference(pc, preference)
                }
            }
        }
    }

    private fun hideEmptyPreference(pc: PreferenceGroup, preference: Preference): Boolean {
        if ((preference as PreferenceGroup).preferenceCount == 0 && hasChildPrefs(preference.getKey())) {
            pc.removePreference(preference)
            Timber.d("Removed %s", preference.toString())
            return true
        }
        return false
    }

    /**
     * Checks whether the preferenceGroup actually has any child preferences defined
     */
    private fun hasChildPrefs(preferenceKey: String): Boolean {
        val preferenceScreensWithNoChildren = arrayOf(
            GeneralKeys.KEY_SPLASH_PATH,
            GeneralKeys.KEY_FORM_METADATA
        )
        for (pref in preferenceScreensWithNoChildren) {
            if (pref == preferenceKey) {
                return false
            }
        }
        return true
    }
}

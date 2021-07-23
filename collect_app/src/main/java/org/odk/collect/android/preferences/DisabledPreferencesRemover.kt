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
import org.odk.collect.shared.Settings

class DisabledPreferencesRemover(
    private val pf: PreferenceFragmentCompat,
    private val adminSettings: Settings
) {
    fun hideDisabledPref(adminAndGeneralKeys: Array<AdminAndGeneralKeys>) {
        hideDisabledPreferences(adminAndGeneralKeys)
        hideEmptyCategories()
    }

    // Hides preferences that are excluded by the admin settings
    private fun hideDisabledPreferences(adminAndGeneralKeys: Array<AdminAndGeneralKeys>) {
        for (agKeys in adminAndGeneralKeys) {
            if (!adminSettings.getBoolean(agKeys.adminKey)) {
                val preference = pf.findPreference<Preference>(agKeys.generalKey) ?: continue
                preference.isVisible = false
            }
        }
    }

    // Hides empty categories - this won't work with nested categories but we don't use them in our
    // settings and we rather shouldn't do that in the future since it would make them vey complex
    private fun hideEmptyCategories() {
        for (i in 0 until pf.preferenceScreen.preferenceCount) {
            val preference = pf.preferenceScreen.getPreference(i)
            if (preference is PreferenceGroup) {
                if (!hasAnyVisiblePreferences(preference)) {
                    preference.isVisible = false
                }
            }
        }
    }

    private fun hasAnyVisiblePreferences(preferenceGroup: PreferenceGroup): Boolean {
        for (i in 0 until preferenceGroup.preferenceCount) {
            val preference = preferenceGroup.getPreference(i)
            if (preference.isVisible) {
                return true
            }
        }
        return false
    }
}

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
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import org.odk.collect.android.preferences.keys.AdminAndGeneralKeys
import org.odk.collect.android.preferences.source.SettingsProvider

class DisabledPreferencesRemover(
    private val settingsProvider: SettingsProvider,
    private val adminAndGeneralKeys: Array<AdminAndGeneralKeys>
) {

    fun hideDisabledPref(preferenceScreen: PreferenceScreen) {
        hideDisabledPreferences(preferenceScreen)
        hideEmptyCategories(preferenceScreen)
    }

    // Hides preferences that are excluded by the admin settings
    private fun hideDisabledPreferences(preferenceScreen: PreferenceScreen) {
        for (agKeys in adminAndGeneralKeys) {
            if (!settingsProvider.getAdminSettings().getBoolean(agKeys.adminKey)) {
                val preference = preferenceScreen.findPreference<Preference>(agKeys.generalKey) ?: continue
                preference.isVisible = false
            }
        }
    }

    // Hides empty categories - this won't work with nested categories but we don't use them in our
    // settings and we rather shouldn't do that in the future since it would make them vey complex
    private fun hideEmptyCategories(preferenceScreen: PreferenceScreen) {
        for (i in 0 until preferenceScreen.preferenceCount) {
            val preference = preferenceScreen.getPreference(i)
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

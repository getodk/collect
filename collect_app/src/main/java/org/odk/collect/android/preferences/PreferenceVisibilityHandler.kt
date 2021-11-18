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

import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.preferences.keys.ProtectedProjectKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.version.VersionInformation

class PreferenceVisibilityHandler(
    private val settingsProvider: SettingsProvider,
    private val versionInformation: VersionInformation
) {

    fun updatePreferencesVisibility(preferenceScreen: PreferenceScreen, state: ProjectPreferencesViewModel.State) {
        updatePreferences(preferenceScreen, state)
        updateCategories(preferenceScreen)
    }

    // Hides preferences that are excluded by the protected settings
    private fun updatePreferences(preferenceGroup: PreferenceGroup, state: ProjectPreferencesViewModel.State) {
        for (i in 0 until preferenceGroup.preferenceCount) {
            val preference = preferenceGroup.getPreference(i)
            if (preference is PreferenceGroup) {
                updatePreferences(preference, state)
            }
            when (preference.key) {
                "protocol" -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_CHANGE_SERVER)
                "project_display" -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_CHANGE_PROJECT_DISPLAY)
                "user_interface" -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || hasAtLeastOnePreferenceEnabled(
                    listOf(
                        ProtectedProjectKeys.KEY_APP_THEME,
                        ProtectedProjectKeys.KEY_APP_LANGUAGE,
                        ProtectedProjectKeys.KEY_CHANGE_FONT_SIZE,
                        ProtectedProjectKeys.KEY_NAVIGATION,
                        ProtectedProjectKeys.KEY_SHOW_SPLASH_SCREEN
                    )
                )
                "maps" -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_MAPS)
                "form_management" -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || hasAtLeastOnePreferenceEnabled(
                    listOf(
                        ProtectedProjectKeys.KEY_FORM_UPDATE_MODE,
                        ProtectedProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK,
                        ProtectedProjectKeys.KEY_AUTOMATIC_UPDATE,
                        ProtectedProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS,
                        ProtectedProjectKeys.KEY_AUTOSEND,
                        ProtectedProjectKeys.KEY_DELETE_AFTER_SEND,
                        ProtectedProjectKeys.KEY_DEFAULT_TO_FINALIZED,
                        ProtectedProjectKeys.KEY_CONSTRAINT_BEHAVIOR,
                        ProtectedProjectKeys.KEY_HIGH_RESOLUTION,
                        ProtectedProjectKeys.KEY_IMAGE_SIZE,
                        ProtectedProjectKeys.KEY_GUIDANCE_HINT,
                        ProtectedProjectKeys.KEY_EXTERNAL_APP_RECORDING,
                        ProtectedProjectKeys.KEY_INSTANCE_FORM_SYNC
                    )
                )
                "user_and_device_identity" -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || hasAtLeastOnePreferenceEnabled(
                    listOf(
                        ProtectedProjectKeys.KEY_CHANGE_FORM_METADATA,
                        ProtectedProjectKeys.KEY_ANALYTICS
                    )
                )
                ProjectKeys.KEY_APP_THEME -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_APP_THEME)
                ProjectKeys.KEY_APP_LANGUAGE -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_APP_LANGUAGE)
                ProjectKeys.KEY_FONT_SIZE -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_CHANGE_FONT_SIZE)
                ProjectKeys.KEY_NAVIGATION -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_NAVIGATION)
                ProjectKeys.KEY_SHOW_SPLASH -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_SHOW_SPLASH_SCREEN)
                ProjectKeys.KEY_SPLASH_PATH -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_SHOW_SPLASH_SCREEN)

                ProjectKeys.KEY_FORM_UPDATE_MODE -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_FORM_UPDATE_MODE)
                ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)
                ProjectKeys.KEY_AUTOMATIC_UPDATE -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_AUTOMATIC_UPDATE)
                ProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS)
                ProjectKeys.KEY_AUTOSEND -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_AUTOSEND)
                ProjectKeys.KEY_DELETE_AFTER_SEND -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_DELETE_AFTER_SEND)
                ProjectKeys.KEY_COMPLETED_DEFAULT -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_DEFAULT_TO_FINALIZED)
                ProjectKeys.KEY_CONSTRAINT_BEHAVIOR -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_CONSTRAINT_BEHAVIOR)
                ProjectKeys.KEY_HIGH_RESOLUTION -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_HIGH_RESOLUTION)
                ProjectKeys.KEY_IMAGE_SIZE -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_IMAGE_SIZE)
                ProjectKeys.KEY_GUIDANCE_HINT -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_GUIDANCE_HINT)
                ProjectKeys.KEY_EXTERNAL_APP_RECORDING -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_EXTERNAL_APP_RECORDING)
                ProjectKeys.KEY_INSTANCE_SYNC -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_INSTANCE_FORM_SYNC)

                ProjectKeys.KEY_FORM_METADATA -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_CHANGE_FORM_METADATA)
                ProjectKeys.KEY_ANALYTICS -> preference.isVisible = state == ProjectPreferencesViewModel.State.UNLOCKED || isOptionEnabled(ProtectedProjectKeys.KEY_ANALYTICS)

                "experimental" -> preference.isVisible = !versionInformation.isRelease

                "admin_password" -> preference.isVisible = state != ProjectPreferencesViewModel.State.LOCKED
                "project_management" -> preference.isVisible = state != ProjectPreferencesViewModel.State.LOCKED
                "access_control" -> preference.isVisible = state != ProjectPreferencesViewModel.State.LOCKED
                "unlock_protected_settings" -> preference.isVisible = state == ProjectPreferencesViewModel.State.LOCKED
            }
        }
    }

    // Hides empty categories - this won't work with nested categories but we don't use them in our
    // settings and we rather shouldn't do that in the future since it would make them very complex
    private fun updateCategories(preferenceScreen: PreferenceScreen) {
        for (i in 0 until preferenceScreen.preferenceCount) {
            val preference = preferenceScreen.getPreference(i)
            if (preference is PreferenceGroup) {
                preference.isVisible = hasCategoryAnyVisiblePreferences(preference)
            }
        }
    }

    private fun hasCategoryAnyVisiblePreferences(preferenceGroup: PreferenceGroup): Boolean {
        for (i in 0 until preferenceGroup.preferenceCount) {
            val preference = preferenceGroup.getPreference(i)
            if (preference.isVisible) {
                return true
            }
        }
        return false
    }

    private fun hasAtLeastOnePreferenceEnabled(keys: Collection<String>): Boolean {
        for (key in keys) {
            val value = settingsProvider.getProtectedSettings().getBoolean(key)
            if (value) {
                return true
            }
        }
        return false
    }

    private fun isOptionEnabled(key: String) = settingsProvider.getProtectedSettings().getBoolean(key)
}

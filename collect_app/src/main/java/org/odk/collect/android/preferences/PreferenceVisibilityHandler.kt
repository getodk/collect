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
import org.odk.collect.android.preferences.keys.ProtectedProjectKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.version.VersionInformation

class PreferenceVisibilityHandler(
    private val settingsProvider: SettingsProvider,
    private val versionInformation: VersionInformation
) {

    fun updatePreferencesVisibility(preferenceScreen: PreferenceScreen, isStateLocked: Boolean) {
        updatePreferences(preferenceScreen, isStateLocked)
        updateCategories(preferenceScreen)
    }

    // Hides preferences that are excluded by the admin settings
    private fun updatePreferences(preferenceGroup: PreferenceGroup, isStateLocked: Boolean) {
        for (i in 0 until preferenceGroup.preferenceCount) {
            val preference = preferenceGroup.getPreference(i)
            if (preference is PreferenceGroup) {
                updatePreferences(preference, isStateLocked)
            }
            when (preference.key) {
                "protocol" -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_CHANGE_SERVER)
                "project_display" -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_CHANGE_PROJECT_DISPLAY)
                "user_interface" -> preference.isVisible = hasAtLeastOnePreferenceEnabled(
                    listOf(
                        ProtectedProjectKeys.KEY_APP_THEME,
                        ProtectedProjectKeys.KEY_APP_LANGUAGE,
                        ProtectedProjectKeys.KEY_CHANGE_FONT_SIZE,
                        ProtectedProjectKeys.KEY_NAVIGATION,
                        ProtectedProjectKeys.KEY_SHOW_SPLASH_SCREEN
                    )
                )
                "maps" -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_MAPS)
                "form_management" -> preference.isVisible = hasAtLeastOnePreferenceEnabled(
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
                "user_and_device_identity" -> preference.isVisible = hasAtLeastOnePreferenceEnabled(
                    listOf(
                        ProtectedProjectKeys.KEY_CHANGE_FORM_METADATA,
                        ProtectedProjectKeys.KEY_ANALYTICS
                    )
                )
                GeneralKeys.KEY_APP_THEME -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_APP_THEME)
                GeneralKeys.KEY_APP_LANGUAGE -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_APP_LANGUAGE)
                GeneralKeys.KEY_FONT_SIZE -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_CHANGE_FONT_SIZE)
                GeneralKeys.KEY_NAVIGATION -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_NAVIGATION)
                GeneralKeys.KEY_SHOW_SPLASH -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_SHOW_SPLASH_SCREEN)
                GeneralKeys.KEY_SPLASH_PATH -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_SHOW_SPLASH_SCREEN)

                GeneralKeys.KEY_FORM_UPDATE_MODE -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_FORM_UPDATE_MODE)
                GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)
                GeneralKeys.KEY_AUTOMATIC_UPDATE -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_AUTOMATIC_UPDATE)
                GeneralKeys.KEY_HIDE_OLD_FORM_VERSIONS -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_HIDE_OLD_FORM_VERSIONS)
                GeneralKeys.KEY_AUTOSEND -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_AUTOSEND)
                GeneralKeys.KEY_DELETE_AFTER_SEND -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_DELETE_AFTER_SEND)
                GeneralKeys.KEY_COMPLETED_DEFAULT -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_DEFAULT_TO_FINALIZED)
                GeneralKeys.KEY_CONSTRAINT_BEHAVIOR -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_CONSTRAINT_BEHAVIOR)
                GeneralKeys.KEY_HIGH_RESOLUTION -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_HIGH_RESOLUTION)
                GeneralKeys.KEY_IMAGE_SIZE -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_IMAGE_SIZE)
                GeneralKeys.KEY_GUIDANCE_HINT -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_GUIDANCE_HINT)
                GeneralKeys.KEY_EXTERNAL_APP_RECORDING -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_EXTERNAL_APP_RECORDING)
                GeneralKeys.KEY_INSTANCE_SYNC -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_INSTANCE_FORM_SYNC)

                GeneralKeys.KEY_FORM_METADATA -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_CHANGE_FORM_METADATA)
                GeneralKeys.KEY_ANALYTICS -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(
                    ProtectedProjectKeys.KEY_ANALYTICS)

                "experimental" -> preference.isVisible = !versionInformation.isRelease

                "admin_password" -> preference.isVisible = !isStateLocked
                "project_management" -> preference.isVisible = !isStateLocked
                "access_control" -> preference.isVisible = !isStateLocked
                "unlock_protected_settings" -> preference.isVisible = isStateLocked
            }
        }
    }

    // Hides empty categories - this won't work with nested categories but we don't use them in our
    // settings and we rather shouldn't do that in the future since it would make them vey complex
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
            val value = settingsProvider.getAdminSettings().getBoolean(key)
            if (value) {
                return true
            }
        }
        return false
    }
}

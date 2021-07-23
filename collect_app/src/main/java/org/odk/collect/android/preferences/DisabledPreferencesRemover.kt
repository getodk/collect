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
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.version.VersionInformation

class DisabledPreferencesRemover(
    private val settingsProvider: SettingsProvider,
    private val versionInformation: VersionInformation
) {

    fun hideDisabledPref(preferenceScreen: PreferenceScreen, isStateLocked: Boolean) {
        hideDisabledPreferences(preferenceScreen, isStateLocked)
        hideEmptyCategories(preferenceScreen)
    }

    // Hides preferences that are excluded by the admin settings
    private fun hideDisabledPreferences(preferenceScreen: PreferenceScreen, isStateLocked: Boolean) {
        for (i in 0 until preferenceScreen.preferenceCount) {
            val preference = preferenceScreen.getPreference(i)
            when (preference.key) {
                "protocol" -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_CHANGE_SERVER)
                "project_display" -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_CHANGE_PROJECT_DISPLAY)
                "user_interface" -> preference.isVisible = hasAtLeastOneSettingEnabled(
                    listOf(
                        AdminKeys.KEY_APP_THEME,
                        AdminKeys.KEY_APP_LANGUAGE,
                        AdminKeys.KEY_CHANGE_FONT_SIZE,
                        AdminKeys.KEY_NAVIGATION,
                        AdminKeys.KEY_SHOW_SPLASH_SCREEN
                    )
                )
                "maps" -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_MAPS)
                "form_management" -> preference.isVisible = hasAtLeastOneSettingEnabled(
                    listOf(
                        AdminKeys.KEY_FORM_UPDATE_MODE,
                        AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK,
                        AdminKeys.KEY_AUTOMATIC_UPDATE,
                        AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS,
                        AdminKeys.KEY_AUTOSEND,
                        AdminKeys.KEY_DELETE_AFTER_SEND,
                        AdminKeys.KEY_DEFAULT_TO_FINALIZED,
                        AdminKeys.KEY_CONSTRAINT_BEHAVIOR,
                        AdminKeys.KEY_HIGH_RESOLUTION,
                        AdminKeys.KEY_IMAGE_SIZE,
                        AdminKeys.KEY_GUIDANCE_HINT,
                        AdminKeys.KEY_EXTERNAL_APP_RECORDING,
                        AdminKeys.KEY_INSTANCE_FORM_SYNC
                    )
                )
                "user_and_device_identity" -> preference.isVisible = hasAtLeastOneSettingEnabled(
                    listOf(
                        AdminKeys.KEY_CHANGE_FORM_METADATA,
                        AdminKeys.KEY_ANALYTICS
                    )
                )
                GeneralKeys.KEY_APP_THEME -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_APP_THEME)
                GeneralKeys.KEY_APP_LANGUAGE -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_APP_LANGUAGE)
                GeneralKeys.KEY_FONT_SIZE -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_CHANGE_FONT_SIZE)
                GeneralKeys.KEY_NAVIGATION -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_NAVIGATION)
                GeneralKeys.KEY_SHOW_SPLASH -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_SHOW_SPLASH_SCREEN)
                GeneralKeys.KEY_SPLASH_PATH -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_SHOW_SPLASH_SCREEN)

                GeneralKeys.KEY_FORM_UPDATE_MODE -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_FORM_UPDATE_MODE)
                GeneralKeys.KEY_PERIODIC_FORM_UPDATES_CHECK -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)
                GeneralKeys.KEY_AUTOMATIC_UPDATE -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_AUTOMATIC_UPDATE)
                GeneralKeys.KEY_HIDE_OLD_FORM_VERSIONS -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_HIDE_OLD_FORM_VERSIONS)
                GeneralKeys.KEY_AUTOSEND -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_AUTOSEND)
                GeneralKeys.KEY_DELETE_AFTER_SEND -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_DELETE_AFTER_SEND)
                GeneralKeys.KEY_COMPLETED_DEFAULT -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_DEFAULT_TO_FINALIZED)
                GeneralKeys.KEY_CONSTRAINT_BEHAVIOR -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_CONSTRAINT_BEHAVIOR)
                GeneralKeys.KEY_HIGH_RESOLUTION -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_HIGH_RESOLUTION)
                GeneralKeys.KEY_IMAGE_SIZE -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_IMAGE_SIZE)
                GeneralKeys.KEY_GUIDANCE_HINT -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_GUIDANCE_HINT)
                GeneralKeys.KEY_EXTERNAL_APP_RECORDING -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_EXTERNAL_APP_RECORDING)
                GeneralKeys.KEY_INSTANCE_SYNC -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_INSTANCE_FORM_SYNC)

                GeneralKeys.KEY_FORM_METADATA -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_CHANGE_FORM_METADATA)
                GeneralKeys.KEY_ANALYTICS -> preference.isVisible = settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_ANALYTICS)

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

    private fun hasAtLeastOneSettingEnabled(keys: Collection<String>): Boolean {
        for (key in keys) {
            val value = settingsProvider.getAdminSettings().getBoolean(key)
            if (value) {
                return true
            }
        }
        return false
    }
}

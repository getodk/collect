package org.odk.collect.android.preferences.source

import android.content.Context
import androidx.preference.PreferenceManager
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.keys.MetaKeys.CURRENT_PROJECT_ID

class SettingsProvider(private val context: Context) {
    fun getMetaSettings() = settings.getOrPut(META_SETTINGS_NAME) {
        SharedPreferencesSettings(context.getSharedPreferences(META_SETTINGS_NAME, Context.MODE_PRIVATE))
    }

    @JvmOverloads
    fun getGeneralSettings(projectId: String = ""): Settings {
        val settingsId = getSettingsId(GENERAL_SETTINGS_NAME, projectId)

        return settings.getOrPut(settingsId) {
            if (settingsId == GENERAL_SETTINGS_NAME) {
                SharedPreferencesSettings(PreferenceManager.getDefaultSharedPreferences(context), GeneralKeys.DEFAULTS)
            } else {
                SharedPreferencesSettings(context.getSharedPreferences(settingsId, Context.MODE_PRIVATE), GeneralKeys.DEFAULTS)
            }
        }
    }

    @JvmOverloads
    fun getAdminSettings(projectId: String = ""): Settings {
        val settingsId = getSettingsId(ADMIN_SETTINGS_NAME, projectId)

        return settings.getOrPut(settingsId) {
            SharedPreferencesSettings(context.getSharedPreferences(settingsId, Context.MODE_PRIVATE), AdminKeys.getDefaults())
        }
    }

    private fun getSettingsId(settingName: String, projectId: String) = if (projectId.isBlank()) {
        settingName + (getMetaSettings().getString(CURRENT_PROJECT_ID) ?: "")
    } else {
        settingName + projectId
    }

    companion object {
        private val settings = mutableMapOf<String, Settings>()

        private const val META_SETTINGS_NAME = "meta"
        private const val GENERAL_SETTINGS_NAME = "general_prefs"
        private const val ADMIN_SETTINGS_NAME = "admin_prefs"
    }
}

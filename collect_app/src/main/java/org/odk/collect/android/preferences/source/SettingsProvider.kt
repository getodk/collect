package org.odk.collect.android.preferences.source

import android.content.Context
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.keys.MetaKeys.CURRENT_PROJECT_ID
import org.odk.collect.shared.Settings
import javax.inject.Singleton

@Singleton
class SettingsProvider(private val context: Context) {

    private val settings = mutableMapOf<String, Settings>()

    fun getMetaSettings() = settings.getOrPut(META_SETTINGS_NAME) {
        SharedPreferencesSettings(getSharedPrefs(META_SETTINGS_NAME))
    }

    @JvmOverloads
    fun getGeneralSettings(projectId: String? = null): Settings {
        val settingsId = getSettingsId(GENERAL_SETTINGS_NAME, projectId)

        return settings.getOrPut(settingsId) {
            SharedPreferencesSettings(getSharedPrefs(settingsId), GeneralKeys.getDefaults())
        }
    }

    @JvmOverloads
    fun getAdminSettings(projectId: String? = null): Settings {
        val settingsId = getSettingsId(ADMIN_SETTINGS_NAME, projectId)

        return settings.getOrPut(settingsId) {
            SharedPreferencesSettings(getSharedPrefs(settingsId), AdminKeys.getDefaults())
        }
    }

    private fun getSharedPrefs(name: String) =
        context.getSharedPreferences(name, Context.MODE_PRIVATE)

    private fun getSettingsId(settingName: String, projectId: String?): String {
        return if (projectId == null) {
            settingName + (getMetaSettings().getString(CURRENT_PROJECT_ID))
        } else {
            settingName + projectId
        }
    }

    companion object {
        private const val META_SETTINGS_NAME = "meta"
        private const val GENERAL_SETTINGS_NAME = "general_prefs"
        private const val ADMIN_SETTINGS_NAME = "admin_prefs"
    }
}

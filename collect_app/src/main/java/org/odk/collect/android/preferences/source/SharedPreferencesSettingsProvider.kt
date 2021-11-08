package org.odk.collect.android.preferences.source

import android.content.Context
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.preferences.keys.ProtectedProjectKeys
import org.odk.collect.shared.Settings
import javax.inject.Singleton

@Singleton
class SharedPreferencesSettingsProvider(private val context: Context) : SettingsProvider {

    private val settings = mutableMapOf<String, Settings>()

    override fun getMetaSettings() = settings.getOrPut(META_SETTINGS_NAME) {
        SharedPreferencesSettings(getSharedPrefs(META_SETTINGS_NAME))
    }

    override fun getUnprotectedSettings(projectId: String?): Settings {
        val settingsId = getSettingsId(GENERAL_SETTINGS_NAME, projectId)

        return settings.getOrPut(settingsId) {
            SharedPreferencesSettings(getSharedPrefs(settingsId), ProjectKeys.defaults)
        }
    }

    override fun getProtectedSettings(projectId: String?): Settings {
        val settingsId = getSettingsId(ADMIN_SETTINGS_NAME, projectId)

        return settings.getOrPut(settingsId) {
            SharedPreferencesSettings(getSharedPrefs(settingsId), ProtectedProjectKeys.defaults)
        }
    }

    override fun clearAll() {
        settings.values.forEach { it.clear() }
        settings.clear()
    }

    private fun getSharedPrefs(name: String) =
        context.getSharedPreferences(name, Context.MODE_PRIVATE)

    private fun getSettingsId(settingName: String, projectId: String?): String {
        return if (projectId == null) {
            settingName + (getMetaSettings().getString(MetaKeys.CURRENT_PROJECT_ID))
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

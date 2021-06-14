package org.odk.collect.android.configure.qr

import org.json.JSONObject
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.CurrentProjectProvider

class AppConfigurationGenerator(
    private val settingsProvider: SettingsProvider,
    private val currentProjectProvider: CurrentProjectProvider
) {

    fun getAppConfigurationAsJsonWithServerDetails(url: String, username: String, password: String): String {
        val generalSettings = JSONObject().apply {
            put(GeneralKeys.KEY_SERVER_URL, url)
            put(GeneralKeys.KEY_USERNAME, username)
            put(GeneralKeys.KEY_PASSWORD, password)
        }

        return JSONObject().apply {
            put(AppConfigurationKeys.GENERAL, generalSettings)
            put(AppConfigurationKeys.ADMIN, JSONObject())
            put(AppConfigurationKeys.PROJECT, JSONObject())
        }.toString()
    }

    fun getAppConfigurationAsJson(includedPasswordKeys: Collection<String> = emptyList()): String {
        return JSONObject().apply {
            put(AppConfigurationKeys.GENERAL, getGeneralPrefsAsJson(includedPasswordKeys))
            put(AppConfigurationKeys.ADMIN, getAdminPrefsAsJson(includedPasswordKeys))
            put(AppConfigurationKeys.PROJECT, getProjectDetailsAsJson())
        }.toString()
    }

    private fun getGeneralPrefsAsJson(includedPasswordKeys: Collection<String>): JSONObject {
        val generalPrefs = JSONObject()

        val generalSettings = settingsProvider.getGeneralSettings().getAll()
        val defaultGeneralSettings: Map<String, *> = GeneralKeys.getDefaults()

        for (key in defaultGeneralSettings.keys) {
            if (key == GeneralKeys.KEY_PASSWORD && !includedPasswordKeys.contains(GeneralKeys.KEY_PASSWORD)) {
                continue
            }
            val value = generalSettings[key]
            if (value != null && value != defaultGeneralSettings[key]) {
                generalPrefs.put(key, value)
            }
        }
        return generalPrefs
    }

    private fun getAdminPrefsAsJson(includedPasswordKeys: Collection<String>): JSONObject {
        val adminPrefs = JSONObject()

        val adminSettings = settingsProvider.getAdminSettings().getAll()
        val defaultAdminSettings = AdminKeys.getDefaults()

        for (key in AdminKeys.ALL_KEYS) {
            if (key == AdminKeys.KEY_ADMIN_PW && !includedPasswordKeys.contains(AdminKeys.KEY_ADMIN_PW)) {
                continue
            }
            val value = adminSettings[key]
            if (value != null && value != defaultAdminSettings[key]) {
                adminPrefs.put(key, value)
            }
        }
        return adminPrefs
    }

    private fun getProjectDetailsAsJson(): JSONObject {
        val currentProject = currentProjectProvider.getCurrentProject()

        return JSONObject().apply {
            put(AppConfigurationKeys.PROJECT_NAME, currentProject.name)
            put(AppConfigurationKeys.PROJECT_ICON, currentProject.icon)
            put(AppConfigurationKeys.PROJECT_COLOR, currentProject.color)
        }
    }
}

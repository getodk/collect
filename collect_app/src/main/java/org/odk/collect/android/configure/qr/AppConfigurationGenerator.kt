package org.odk.collect.android.configure.qr

import org.json.JSONObject
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.preferences.keys.ProtectedProjectKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.CurrentProjectProvider

class AppConfigurationGenerator(
    private val settingsProvider: SettingsProvider,
    private val currentProjectProvider: CurrentProjectProvider
) {

    fun getAppConfigurationAsJsonWithServerDetails(url: String, username: String, password: String): String {
        val generalSettings = JSONObject().apply {
            put(ProjectKeys.KEY_SERVER_URL, url)
            put(ProjectKeys.KEY_USERNAME, username)
            put(ProjectKeys.KEY_PASSWORD, password)
        }

        return JSONObject().apply {
            put(AppConfigurationKeys.GENERAL, generalSettings)
            put(AppConfigurationKeys.ADMIN, JSONObject())
            put(AppConfigurationKeys.PROJECT, JSONObject())
        }.toString()
    }

    fun getAppConfigurationAsJsonWithGoogleDriveDetails(googleAccount: String?): String {
        val generalSettings = JSONObject().apply {
            put(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_GOOGLE_SHEETS)
            put(ProjectKeys.KEY_SELECTED_GOOGLE_ACCOUNT, googleAccount)
            put(ProjectKeys.KEY_SERVER_URL, "")
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
        val defaultGeneralSettings: Map<String, *> = ProjectKeys.defaults

        for (key in defaultGeneralSettings.keys) {
            if (key == ProjectKeys.KEY_PASSWORD && !includedPasswordKeys.contains(ProjectKeys.KEY_PASSWORD)) {
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
        val defaultAdminSettings = ProtectedProjectKeys.defaults

        for (key in ProtectedProjectKeys.allKeys()) {
            if (key == ProtectedProjectKeys.KEY_ADMIN_PW && !includedPasswordKeys.contains(ProtectedProjectKeys.KEY_ADMIN_PW)) {
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

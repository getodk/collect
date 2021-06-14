package org.odk.collect.android.configure

import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.application.initialization.SettingsPreferenceMigrator
import org.odk.collect.android.configure.qr.JsonPreferencesKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.shared.Settings

class SettingsImporter(
    private val settingsProvider: SettingsProvider,
    private val preferenceMigrator: SettingsPreferenceMigrator,
    private val settingsValidator: SettingsValidator,
    private val generalDefaults: Map<String, Any>,
    private val adminDefaults: Map<String, Any>,
    private val settingsChangedHandler: SettingsChangeHandler,
    private val projectsRepository: ProjectsRepository
) {

    fun fromJSON(json: String, projectId: String): Boolean {
        if (!settingsValidator.isValid(json)) {
            return false
        }

        val generalSettings = settingsProvider.getGeneralSettings(projectId)
        val adminSettings = settingsProvider.getAdminSettings(projectId)

        generalSettings.clear()
        adminSettings.clear()

        try {
            val jsonObject = JSONObject(json)

            val general = jsonObject.getJSONObject(JsonPreferencesKeys.GENERAL)
            importToPrefs(general, generalSettings)

            val admin = jsonObject.getJSONObject(JsonPreferencesKeys.ADMIN)
            importToPrefs(admin, adminSettings)

            if (jsonObject.has(JsonPreferencesKeys.PROJECT)) {
                val project = jsonObject.getJSONObject(JsonPreferencesKeys.PROJECT)
                importProjectDetails(project, projectId)
            }
        } catch (ignored: JSONException) {
            // Ignored
        }

        preferenceMigrator.migrate(generalSettings, adminSettings)

        clearUnknownKeys(generalSettings, generalDefaults)
        clearUnknownKeys(adminSettings, adminDefaults)
        loadDefaults(generalSettings, generalDefaults)
        loadDefaults(adminSettings, adminDefaults)

        for ((key, value) in generalSettings.getAll()) {
            settingsChangedHandler.onSettingChanged(projectId, value, key)
        }
        for ((key, value) in adminSettings.getAll()) {
            settingsChangedHandler.onSettingChanged(projectId, value, key)
        }
        return true
    }

    private fun importToPrefs(`object`: JSONObject, preferences: Settings) {
        val generalKeys = `object`.keys()
        while (generalKeys.hasNext()) {
            val key = generalKeys.next()
            preferences.save(key, `object`[key])
        }
    }

    private fun loadDefaults(preferences: Settings, defaults: Map<String, Any>) {
        for ((key, value) in defaults) {
            if (!preferences.contains(key)) {
                preferences.save(key, value)
            }
        }
    }

    private fun clearUnknownKeys(preferences: Settings, defaults: Map<String, Any>) {
        for (key in preferences.getAll().keys) {
            if (!defaults.containsKey(key)) {
                preferences.remove(key)
            }
        }
    }

    private fun importProjectDetails(projectJson: JSONObject, projectId: String) {
        val project = projectsRepository.get(projectId)!!

        val projectName = if (projectJson.has(JsonPreferencesKeys.PROJECT_NAME)) projectJson.get(JsonPreferencesKeys.PROJECT_NAME).toString() else project.name
        val projectIcon = if (projectJson.has(JsonPreferencesKeys.PROJECT_ICON)) projectJson.get(JsonPreferencesKeys.PROJECT_ICON).toString() else project.icon
        val projectColor = if (projectJson.has(JsonPreferencesKeys.PROJECT_COLOR)) projectJson.get(JsonPreferencesKeys.PROJECT_COLOR).toString() else project.color

        projectsRepository.save(
            project.copy(
                name = projectName,
                icon = projectIcon,
                color = projectColor
            )
        )
    }
}

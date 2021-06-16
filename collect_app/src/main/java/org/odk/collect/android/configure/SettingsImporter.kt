package org.odk.collect.android.configure

import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.application.initialization.SettingsPreferenceMigrator
import org.odk.collect.android.configure.qr.AppConfigurationKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.projects.Project
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

    fun fromJSON(json: String, project: Project.Saved): Boolean {
        if (!settingsValidator.isValid(json)) {
            return false
        }

        val generalSettings = settingsProvider.getGeneralSettings(project.uuid)
        val adminSettings = settingsProvider.getAdminSettings(project.uuid)

        generalSettings.clear()
        adminSettings.clear()

        try {
            val jsonObject = JSONObject(json)

            val general = jsonObject.getJSONObject(AppConfigurationKeys.GENERAL)
            importToPrefs(general, generalSettings)

            val admin = jsonObject.getJSONObject(AppConfigurationKeys.ADMIN)
            importToPrefs(admin, adminSettings)

            if (jsonObject.has(AppConfigurationKeys.PROJECT)) {
                importProjectDetails(jsonObject.getJSONObject(AppConfigurationKeys.PROJECT), project)
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
            settingsChangedHandler.onSettingChanged(project.uuid, value, key)
        }
        for ((key, value) in adminSettings.getAll()) {
            settingsChangedHandler.onSettingChanged(project.uuid, value, key)
        }
        return true
    }

    private fun importToPrefs(jsonObject: JSONObject, preferences: Settings) {
        jsonObject.keys().forEach {
            preferences.save(it, jsonObject[it])
        }
    }

    private fun loadDefaults(preferences: Settings, defaults: Map<String, Any>) {
        defaults.forEach { (key, value) ->
            if (!preferences.contains(key)) {
                preferences.save(key, value)
            }
        }
    }

    private fun clearUnknownKeys(preferences: Settings, defaults: Map<String, Any>) {
        preferences.getAll().forEach { (key, _) ->
            if (!defaults.containsKey(key)) {
                preferences.remove(key)
            }
        }
    }

    private fun importProjectDetails(projectJson: JSONObject, project: Project.Saved) {
        val projectName = if (projectJson.has(AppConfigurationKeys.PROJECT_NAME)) projectJson.get(AppConfigurationKeys.PROJECT_NAME).toString() else project.name
        val projectIcon = if (projectJson.has(AppConfigurationKeys.PROJECT_ICON)) projectJson.get(AppConfigurationKeys.PROJECT_ICON).toString() else project.icon
        val projectColor = if (projectJson.has(AppConfigurationKeys.PROJECT_COLOR)) projectJson.get(AppConfigurationKeys.PROJECT_COLOR).toString() else project.color

        projectsRepository.save(
            project.copy(
                name = projectName,
                icon = projectIcon,
                color = projectColor
            )
        )
    }
}

package org.odk.collect.android.configure

import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.application.initialization.SettingsMigrator
import org.odk.collect.android.configure.qr.AppConfigurationKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.ProjectDetailsCreator
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.shared.Settings

class SettingsImporter(
    private val settingsProvider: SettingsProvider,
    private val settingsMigrator: SettingsMigrator,
    private val settingsValidator: SettingsValidator,
    private val generalDefaults: Map<String, Any>,
    private val adminDefaults: Map<String, Any>,
    private val settingsChangedHandler: SettingsChangeHandler,
    private val projectsRepository: ProjectsRepository,
    private val projectDetailsCreator: ProjectDetailsCreator
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

            // Import general settings
            val general = jsonObject.getJSONObject(AppConfigurationKeys.GENERAL)
            importToPrefs(general, generalSettings)

            // Import admin settings
            val admin = jsonObject.getJSONObject(AppConfigurationKeys.ADMIN)
            importToPrefs(admin, adminSettings)

            // Import project details
            val projectDetails = if (jsonObject.has(AppConfigurationKeys.PROJECT)) {
                jsonObject.getJSONObject(AppConfigurationKeys.PROJECT)
            } else {
                JSONObject()
            }
            importProjectDetails(
                generalSettings.getString(GeneralKeys.KEY_SERVER_URL) ?: "",
                projectDetails,
                project
            )
        } catch (ignored: JSONException) {
            // Ignored
        }

        settingsMigrator.migrate(generalSettings, adminSettings)

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

    private fun importProjectDetails(url: String, projectJson: JSONObject, project: Project.Saved) {
        val projectName = if (projectJson.has(AppConfigurationKeys.PROJECT_NAME)) {
            projectJson.getString(AppConfigurationKeys.PROJECT_NAME)
        } else {
            ""
        }
        val projectIcon = if (projectJson.has(AppConfigurationKeys.PROJECT_ICON)) {
            projectJson.getString(AppConfigurationKeys.PROJECT_ICON)
        } else {
            ""
        }
        val projectColor = if (projectJson.has(AppConfigurationKeys.PROJECT_COLOR)) {
            projectJson.getString(AppConfigurationKeys.PROJECT_COLOR)
        } else {
            ""
        }

        val newProject = projectDetailsCreator.createProjectFromDetails(url, projectName, projectIcon, projectColor)

        projectsRepository.save(
            project.copy(
                name = newProject.name,
                icon = newProject.icon,
                color = newProject.color
            )
        )
    }
}

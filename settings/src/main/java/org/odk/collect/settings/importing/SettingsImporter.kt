package org.odk.collect.settings.importing

import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.AppConfigurationKeys
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings

internal class SettingsImporter(
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

        val generalSettings = settingsProvider.getUnprotectedSettings(project.uuid)
        val adminSettings = settingsProvider.getProtectedSettings(project.uuid)

        generalSettings.clear()
        adminSettings.clear()

        try {
            val jsonObject = JSONObject(json)

            // Import unprotected settings
            val general = jsonObject.getJSONObject(AppConfigurationKeys.GENERAL)
            importToPrefs(general, generalSettings)

            // Import protected settings
            val admin = jsonObject.getJSONObject(AppConfigurationKeys.ADMIN)
            importToPrefs(admin, adminSettings)

            // Import project details
            val projectDetails = if (jsonObject.has(AppConfigurationKeys.PROJECT)) {
                jsonObject.getJSONObject(AppConfigurationKeys.PROJECT)
            } else {
                JSONObject()
            }

            val connectionIdentifier = if (generalSettings.getString(ProjectKeys.KEY_PROTOCOL).equals(ProjectKeys.PROTOCOL_GOOGLE_SHEETS)) {
                generalSettings.getString(ProjectKeys.KEY_SELECTED_GOOGLE_ACCOUNT) ?: ""
            } else {
                generalSettings.getString(ProjectKeys.KEY_SERVER_URL) ?: ""
            }

            importProjectDetails(
                project,
                projectDetails,
                connectionIdentifier
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
        val toRemove = ArrayList<String>()

        preferences.getAll().forEach { (key, _) ->
            if (!defaults.containsKey(key)) {
                toRemove.add(key)
            }
        }

        toRemove.forEach { key ->
            preferences.remove(key)
        }
    }

    private fun importProjectDetails(project: Project.Saved, projectJson: JSONObject, connectionIdentifier: String) {
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

        val newProject = projectDetailsCreator.createProjectFromDetails(projectName, projectIcon, projectColor, connectionIdentifier)

        projectsRepository.save(
            project.copy(
                name = newProject.name,
                icon = newProject.icon,
                color = newProject.color
            )
        )
    }
}

internal fun interface SettingsValidator {
    fun isValid(json: String): Boolean
}

fun interface SettingsChangeHandler {
    fun onSettingChanged(projectId: String, newValue: Any?, changedKey: String)
}

internal fun interface SettingsMigrator {
    fun migrate(generalSettings: Settings, adminSettings: Settings)
}

interface ProjectDetailsCreator {
    fun createProjectFromDetails(name: String = "", icon: String = "", color: String = "", connectionIdentifier: String = ""): Project
}

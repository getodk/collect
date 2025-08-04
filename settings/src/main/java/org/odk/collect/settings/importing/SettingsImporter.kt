package org.odk.collect.settings.importing

import org.json.JSONObject
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectConfigurationResult
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.AppConfigurationKeys
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.collections.CollectionExtensions.has
import org.odk.collect.shared.settings.Settings

internal class SettingsImporter(
    private val settingsProvider: SettingsProvider,
    private val settingsMigrator: SettingsMigrator,
    private val settingsValidator: SettingsValidator,
    private val unprotectedDefaults: Map<String, Any>,
    private val protectedDefaults: Map<String, Any>,
    private val settingsChangedHandler: SettingsChangeHandler,
    private val projectsRepository: ProjectsRepository,
    private val projectDetailsCreator: ProjectDetailsCreator
) {

    fun fromJSON(json: String, project: Project.Saved, deviceUnsupportedSettings: JSONObject): ProjectConfigurationResult {
        if (!settingsValidator.isValid(json)) {
            return ProjectConfigurationResult.INVALID_SETTINGS
        }

        val unprotectedSettings = settingsProvider.getUnprotectedSettings(project.uuid)
        val protectedSettings = settingsProvider.getProtectedSettings(project.uuid)

        val oldUnprotectedSettings = unprotectedSettings.getAll().toMap().let {
            it.ifEmpty { unprotectedDefaults }
        }
        val oldProtectedSettings = protectedSettings.getAll().toMap().let {
            it.ifEmpty { protectedDefaults }
        }

        unprotectedSettings.clear()
        protectedSettings.clear()

        val jsonObject = JSONObject(json)

        if (isGDProject(jsonObject)) {
            return ProjectConfigurationResult.GD_PROJECT
        }

        // Import unprotected settings
        importToPrefs(jsonObject, AppConfigurationKeys.GENERAL, unprotectedSettings, deviceUnsupportedSettings)

        // Import protected settings
        importToPrefs(jsonObject, AppConfigurationKeys.ADMIN, protectedSettings, deviceUnsupportedSettings)

        // Import project details
        val projectDetails = if (jsonObject.has(AppConfigurationKeys.PROJECT)) {
            jsonObject.getJSONObject(AppConfigurationKeys.PROJECT)
        } else {
            JSONObject()
        }

        val connectionIdentifier = unprotectedSettings.getString(ProjectKeys.KEY_SERVER_URL) ?: ""

        importProjectDetails(
            project,
            projectDetails,
            connectionIdentifier
        )

        settingsMigrator.migrate(unprotectedSettings, protectedSettings)

        loadDefaults(unprotectedSettings, unprotectedDefaults)
        loadDefaults(protectedSettings, protectedDefaults)

        val newUnprotectedSettings = unprotectedSettings.getAll()
        val newProtectedSettings = protectedSettings.getAll()

        val changedUnprotectedKeys = oldUnprotectedSettings.keys.filter { key ->
            newUnprotectedSettings[key] != oldUnprotectedSettings[key]
        }
        val changedProtectedKeys = oldProtectedSettings.keys.filter { key ->
            newProtectedSettings[key] != oldProtectedSettings[key]
        }

        settingsChangedHandler.onSettingsChanged(project.uuid, changedUnprotectedKeys, changedProtectedKeys)

        return ProjectConfigurationResult.SUCCESS
    }

    private fun isGDProject(jsonObject: JSONObject): Boolean {
        val unprotectedSettings = jsonObject.getJSONObject(AppConfigurationKeys.GENERAL)
        return unprotectedSettings.has(ProjectKeys.KEY_PROTOCOL) &&
            unprotectedSettings.get(ProjectKeys.KEY_PROTOCOL) == ProjectKeys.PROTOCOL_GOOGLE_SHEETS
    }

    private fun importToPrefs(
        mainJsonObject: JSONObject,
        childJsonObjectName: String,
        preferences: Settings,
        deviceUnsupportedSettings: JSONObject
    ) {
        val childJsonObject = mainJsonObject.getJSONObject(childJsonObjectName)
        val deviceUnsupportedSettingsForGivenChildJson = if (deviceUnsupportedSettings.has(childJsonObjectName)) deviceUnsupportedSettings.getJSONObject(childJsonObjectName) else JSONObject()

        childJsonObject.keys().forEach {
            if (settingsValidator.isKeySupported(childJsonObjectName, it)) {
                val value = childJsonObject[it]
                if (settingsValidator.isValueSupported(childJsonObjectName, it, value)) {
                    if (!deviceUnsupportedSettingsForGivenChildJson.has(it) || !deviceUnsupportedSettingsForGivenChildJson.getJSONArray(it).has(value)) {
                        preferences.save(it, value)
                    }
                }
            }
        }
    }

    private fun loadDefaults(preferences: Settings, defaults: Map<String, Any>) {
        defaults.forEach { (key, value) ->
            if (!preferences.contains(key)) {
                preferences.save(key, value)
            }
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

internal interface SettingsValidator {
    fun isValid(json: String): Boolean

    fun isKeySupported(parentJsonObjectName: String, key: String): Boolean

    fun isValueSupported(parentJsonObjectName: String, key: String, value: Any): Boolean
}

interface SettingsChangeHandler {
    fun onSettingChanged(projectId: String, newValue: Any?, changedKey: String)

    fun onSettingsChanged(projectId: String, changedUnprotectedKeys: List<String>, changedProtectedKeys: List<String>)
}

internal fun interface SettingsMigrator {
    fun migrate(unprotectedSettings: Settings, protectedSettings: Settings)
}

interface ProjectDetailsCreator {
    fun createProjectFromDetails(name: String = "", icon: String = "", color: String = "", connectionIdentifier: String = ""): Project
}

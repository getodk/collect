package org.odk.collect.settings

import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.importing.ProjectDetailsCreatorImpl
import org.odk.collect.settings.importing.SettingsChangeHandler
import org.odk.collect.settings.importing.SettingsImporter
import org.odk.collect.settings.validation.JsonSchemaSettingsValidator

class ODKAppSettingsImporter(
    projectsRepository: ProjectsRepository,
    settingsProvider: SettingsProvider,
    generalDefaults: Map<String, Any>,
    adminDefaults: Map<String, Any>,
    projectColors: List<String>,
    settingsChangedHandler: SettingsChangeHandler
) {

    private val settingsImporter = SettingsImporter(
        settingsProvider,
        ODKAppSettingsMigrator(settingsProvider.getMetaSettings()),
        JsonSchemaSettingsValidator { javaClass.getResourceAsStream("/settings_schema.json")!! },
        generalDefaults,
        adminDefaults,
        settingsChangedHandler,
        projectsRepository,
        ProjectDetailsCreatorImpl(projectColors, generalDefaults)
    )

    fun fromJSON(json: String, project: Project.Saved): Boolean {
        return settingsImporter.fromJSON(json, project)
    }
}

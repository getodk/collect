package org.odk.collect.android.projects

import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.ODKAppSettingsImporter
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.importing.SettingsImportingResult

class ProjectCreator(
    private val projectsRepository: ProjectsRepository,
    private val currentProjectProvider: CurrentProjectProvider,
    private val settingsImporter: ODKAppSettingsImporter,
    private val settingsProvider: SettingsProvider
) {

    fun createNewProject(settingsJson: String): Boolean {
        val savedProject = projectsRepository.save(Project.New("", "", ""))
        val settingsImportingResult = settingsImporter.fromJSON(settingsJson, savedProject)

        return if (settingsImportingResult == SettingsImportingResult.SUCCESS) {
            currentProjectProvider.setCurrentProject(savedProject.uuid)
            true
        } else {
            settingsProvider.getUnprotectedSettings(savedProject.uuid).clear()
            settingsProvider.getProtectedSettings(savedProject.uuid).clear()
            projectsRepository.delete(savedProject.uuid)
            false
        }
    }
}

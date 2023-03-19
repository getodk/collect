package org.odk.collect.android.projects

import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.ODKAppSettingsImporter
import org.odk.collect.settings.SettingsProvider

class ProjectCreator(
    private val projectsRepository: ProjectsRepository,
    private val currentProjectProvider: CurrentProjectProvider,
    private val settingsImporter: ODKAppSettingsImporter,
    private val settingsProvider: SettingsProvider
) {

    fun createNewProject(settingsJson: String): Boolean {
        val savedProject = projectsRepository.save(Project.New("", "", ""))
        val settingsImportedSuccessfully = settingsImporter.fromJSON(settingsJson, savedProject)

        return if (settingsImportedSuccessfully) {
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

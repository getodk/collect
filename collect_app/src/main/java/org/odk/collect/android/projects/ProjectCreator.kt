package org.odk.collect.android.projects

import org.odk.collect.android.configure.SettingsImporter
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository

class ProjectCreator(
    private val projectsRepository: ProjectsRepository,
    private val currentProjectProvider: CurrentProjectProvider,
    private val settingsImporter: SettingsImporter
) {

    fun createNewProject(settingsJson: String): Boolean {
        val savedProject = projectsRepository.save(Project.New("", "", ""))
        val settingsImportedSuccessfully = settingsImporter.fromJSON(settingsJson, savedProject)

        return if (settingsImportedSuccessfully) {
            currentProjectProvider.setCurrentProject(savedProject.uuid)
            true
        } else {
            projectsRepository.delete(savedProject.uuid)
            false
        }
    }
}

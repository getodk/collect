package org.odk.collect.android.projects

import org.odk.collect.android.backgroundwork.FormUpdateManager
import org.odk.collect.projects.Project.Saved
import org.odk.collect.projects.ProjectsRepository

class ProjectDeleter(
    private val projectsRepository: ProjectsRepository,
    private val currentProjectProvider: CurrentProjectProvider,
    private val formUpdateManager: FormUpdateManager
) {
    fun deleteCurrentProject(): Saved? {
        formUpdateManager.cancelUpdates()

        val currentProject = currentProjectProvider.getCurrentProject()
        projectsRepository.delete(currentProject.uuid)

        return if (projectsRepository.getAll().isNotEmpty()) {
            val newProject = projectsRepository.getAll()[0]
            currentProjectProvider.setCurrentProject(newProject.uuid)
            newProject
        } else {
            null
        }
    }
}

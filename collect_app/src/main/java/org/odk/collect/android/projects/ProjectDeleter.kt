package org.odk.collect.android.projects

import org.odk.collect.android.backgroundwork.FormUpdateScheduler
import org.odk.collect.android.backgroundwork.InstanceSubmitScheduler
import org.odk.collect.projects.Project.Saved
import org.odk.collect.projects.ProjectsRepository

class ProjectDeleter(
    private val projectsRepository: ProjectsRepository,
    private val currentProjectProvider: CurrentProjectProvider,
    private val formUpdateScheduler: FormUpdateScheduler,
    private val instanceSubmitScheduler: InstanceSubmitScheduler
) {
    fun deleteCurrentProject(): Saved? {
        val currentProject = currentProjectProvider.getCurrentProject()

        formUpdateScheduler.cancelUpdates(currentProject.uuid)
        instanceSubmitScheduler.cancelSubmit(currentProject.uuid)

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

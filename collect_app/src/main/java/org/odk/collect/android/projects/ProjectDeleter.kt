package org.odk.collect.android.projects

import org.odk.collect.android.backgroundwork.FormUpdateScheduler
import org.odk.collect.android.backgroundwork.InstanceSubmitScheduler
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import java.io.File

class ProjectDeleter(
    private val projectsRepository: ProjectsRepository,
    private val currentProjectProvider: CurrentProjectProvider,
    private val formUpdateScheduler: FormUpdateScheduler,
    private val instanceSubmitScheduler: InstanceSubmitScheduler,
    private val instancesRepository: InstancesRepository,
    private val projectDirPath: String
) {
    fun deleteCurrentProject(): DeleteProjectResult {
        if (instancesRepository.getAllByStatus(
                Instance.STATUS_INCOMPLETE,
                Instance.STATUS_COMPLETE,
                Instance.STATUS_SUBMISSION_FAILED
            ).isNotEmpty()
        ) {
            return DeleteProjectResult.UnsentInstances
        } else {
            val currentProject = currentProjectProvider.getCurrentProject()

            formUpdateScheduler.cancelUpdates(currentProject.uuid)
            instanceSubmitScheduler.cancelSubmit(currentProject.uuid)

            projectsRepository.delete(currentProject.uuid)

            File(projectDirPath).deleteRecursively()

            return if (projectsRepository.getAll().isNotEmpty()) {
                val newProject = projectsRepository.getAll()[0]
                currentProjectProvider.setCurrentProject(newProject.uuid)
                DeleteProjectResult.DeletedSuccessfully(newProject)
            } else {
                DeleteProjectResult.DeletedSuccessfully(null)
            }
        }
    }
}

sealed class DeleteProjectResult {
    object UnsentInstances : DeleteProjectResult()

    data class DeletedSuccessfully(val newCurrentProject: Project.Saved?) : DeleteProjectResult()
}

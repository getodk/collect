package org.odk.collect.android.projects

import org.odk.collect.android.backgroundwork.FormUpdateScheduler
import org.odk.collect.android.backgroundwork.InstanceSubmitScheduler
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import java.io.File

class ProjectDeleter(
    private val projectsRepository: ProjectsRepository,
    private val currentProjectProvider: CurrentProjectProvider,
    private val formUpdateScheduler: FormUpdateScheduler,
    private val instanceSubmitScheduler: InstanceSubmitScheduler,
    private val instancesRepository: InstancesRepository,
    private val projectDirPath: String,
    private val changeLockProvider: ChangeLockProvider,
    private val settingsProvider: SettingsProvider
) {
    fun deleteCurrentProject(): DeleteProjectResult {
        return when {
            unsentInstancesDetected() -> DeleteProjectResult.UnsentInstances
            runningBackgroundJobsDetected() -> DeleteProjectResult.RunningBackgroundJobs
            else -> performProjectDeletion()
        }
    }

    private fun unsentInstancesDetected(): Boolean {
        return instancesRepository.getAllByStatus(
            Instance.STATUS_INCOMPLETE,
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED
        ).isNotEmpty()
    }

    private fun runningBackgroundJobsDetected(): Boolean {
        val acquiredFormLock = changeLockProvider.getFormLock(currentProjectProvider.getCurrentProject().uuid).withLock { acquiredLock ->
            acquiredLock
        }
        val acquiredInstanceLock = changeLockProvider.getInstanceLock(currentProjectProvider.getCurrentProject().uuid).withLock { acquiredLock ->
            acquiredLock
        }

        return !acquiredFormLock || !acquiredInstanceLock
    }

    private fun performProjectDeletion(): DeleteProjectResult {
        val currentProject = currentProjectProvider.getCurrentProject()

        formUpdateScheduler.cancelUpdates(currentProject.uuid)
        instanceSubmitScheduler.cancelSubmit(currentProject.uuid)

        settingsProvider.getUnprotectedSettings(currentProject.uuid).clear()
        settingsProvider.getProtectedSettings(currentProject.uuid).clear()

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

sealed class DeleteProjectResult {
    object UnsentInstances : DeleteProjectResult()

    object RunningBackgroundJobs : DeleteProjectResult()

    data class DeletedSuccessfully(val newCurrentProject: Project.Saved?) : DeleteProjectResult()
}

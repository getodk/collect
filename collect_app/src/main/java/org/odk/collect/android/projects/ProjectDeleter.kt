package org.odk.collect.android.projects

import org.odk.collect.android.backgroundwork.FormUpdateScheduler
import org.odk.collect.android.backgroundwork.InstanceSubmitScheduler
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.db.sqlite.DatabaseConnection
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import java.io.File

class ProjectDeleter(
    private val projectsRepository: ProjectsRepository,
    private val projectsDataService: ProjectsDataService,
    private val formUpdateScheduler: FormUpdateScheduler,
    private val instanceSubmitScheduler: InstanceSubmitScheduler,
    private val storagePathProvider: StoragePathProvider,
    private val settingsProvider: SettingsProvider
) {
    fun deleteProject(projectId: String): DeleteProjectResult {
        return performProjectDeletion(projectId)
    }

    private fun performProjectDeletion(projectId: String): DeleteProjectResult {
        formUpdateScheduler.cancelUpdates(projectId)
        instanceSubmitScheduler.cancelSubmit(projectId)

        settingsProvider.getUnprotectedSettings(projectId).clear()
        settingsProvider.getProtectedSettings(projectId).clear()

        projectsRepository.delete(projectId)

        File(storagePathProvider.getProjectRootDirPath(projectId)).deleteRecursively()

        DatabaseConnection.cleanUp()

        return try {
            projectsDataService.requireCurrentProject()
            DeleteProjectResult.DeletedSuccessfullyInactiveProject
        } catch (e: IllegalStateException) {
            if (projectsRepository.getAll().isEmpty()) {
                DeleteProjectResult.DeletedSuccessfullyLastProject
            } else {
                val newProject = projectsRepository.getAll()[0]
                projectsDataService.setCurrentProject(newProject.uuid)
                DeleteProjectResult.DeletedSuccessfullyCurrentProject(newProject)
            }
        }
    }
}

sealed class DeleteProjectResult {
    data object DeletedSuccessfullyLastProject : DeleteProjectResult()

    data object DeletedSuccessfullyInactiveProject : DeleteProjectResult()

    data class DeletedSuccessfullyCurrentProject(val newCurrentProject: Project.Saved) : DeleteProjectResult()
}

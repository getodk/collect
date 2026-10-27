package org.odk.collect.android.application.initialization

import org.odk.collect.android.projects.ProjectDeleter
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.projects.ProjectDependencyFactory
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.upgrade.Upgrade

class GoogleDriveProjectsDeleter(
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider,
    private val projectDeleter: ProjectDeleter,
    private val instancesRepositoryProvider: ProjectDependencyFactory<InstancesRepository>
) : Upgrade {
    override fun key() = null

    override fun run() {
        projectsRepository.getAll().forEach {
            val unprotectedSettings = settingsProvider.getUnprotectedSettings(it.uuid)
            val protocol = unprotectedSettings.getString(ProjectKeys.KEY_PROTOCOL)

            if (protocol == ProjectKeys.PROTOCOL_GOOGLE_SHEETS) {
                if (hasUnsentInstances(it.uuid)) {
                    unprotectedSettings.save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_SERVER)
                    unprotectedSettings.save(ProjectKeys.KEY_SERVER_URL, "https://example.com")
                    projectsRepository.save(it.copy(isOldGoogleDriveProject = true))
                } else {
                    projectDeleter.deleteProject(it.uuid)
                }
            }
        }
    }

    private fun hasUnsentInstances(projectId: String): Boolean {
        return instancesRepositoryProvider.create(projectId).getCountByStatus(
            Instance.STATUS_INCOMPLETE,
            Instance.STATUS_INVALID,
            Instance.STATUS_VALID,
            Instance.STATUS_NEW_EDIT,
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED
        ) > 0
    }
}

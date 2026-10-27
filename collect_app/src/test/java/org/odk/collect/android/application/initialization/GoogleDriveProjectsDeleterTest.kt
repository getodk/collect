package org.odk.collect.android.application.initialization

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.odk.collect.android.projects.ProjectDeleter
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.formstest.InstanceFixtures
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class GoogleDriveProjectsDeleterTest {
    private val projectsRepository = InMemProjectsRepository()
    private val settingsProvider = InMemSettingsProvider()
    private val projectDeleter = mock<ProjectDeleter>()
    private val instancesRepositories = mutableMapOf<String, InstancesRepository>()

    private val googleDriveProjectsDeleter = GoogleDriveProjectsDeleter(projectsRepository, settingsProvider, projectDeleter) {
        instancesRepositories.getOrPut(it) { InMemInstancesRepository() }
    }

    @Test
    fun `GoogleDriveProjectsDeleter should have null key`() {
        assertThat(googleDriveProjectsDeleter.key(), equalTo(null))
    }

    @Test
    fun `only GD projects should be considered for deletion`() {
        projectsRepository.save(Project.Saved("1", "project", "Q", "#000000"))
        projectsRepository.save(Project.Saved("2", "project", "Q", "#000000"))
        projectsRepository.save(Project.Saved("3", "project", "Q", "#000000"))
        projectsRepository.save(Project.Saved("4", "project", "Q", "#000000"))

        settingsProvider.getUnprotectedSettings("1").save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_SERVER)
        settingsProvider.getUnprotectedSettings("2").save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_GOOGLE_SHEETS)
        settingsProvider.getUnprotectedSettings("3").save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_GOOGLE_SHEETS)
        settingsProvider.getUnprotectedSettings("4").save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_SERVER)

        googleDriveProjectsDeleter.run()

        verify(projectDeleter).deleteProject("2")
        verify(projectDeleter).deleteProject("3")
        verifyNoMoreInteractions(projectDeleter)
    }

    @Test
    fun `GD projects with unsent forms should be converted to ODK protocol and marked as old GD projects instead of being deleted`() {
        val unsentStatuses = listOf(
            Instance.STATUS_INCOMPLETE,
            Instance.STATUS_INVALID,
            Instance.STATUS_VALID,
            Instance.STATUS_NEW_EDIT,
            Instance.STATUS_COMPLETE,
            Instance.STATUS_SUBMISSION_FAILED
        )

        unsentStatuses.forEach { status ->
            projectsRepository.save(Project.Saved(status, "project", "Q", "#000000"))
            settingsProvider.getUnprotectedSettings(status).save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_GOOGLE_SHEETS)
            instancesRepositories[status] = InMemInstancesRepository().apply {
                save(InstanceFixtures.instance(status = status))
            }
        }

        googleDriveProjectsDeleter.run()

        unsentStatuses.forEach { status ->
            verify(projectDeleter, never()).deleteProject(status)
            assertThat(status, settingsProvider.getUnprotectedSettings(status).getString(ProjectKeys.KEY_PROTOCOL), equalTo(ProjectKeys.PROTOCOL_SERVER))
            assertThat(status, settingsProvider.getUnprotectedSettings(status).getString(ProjectKeys.KEY_SERVER_URL), equalTo("https://example.com"))
            assertThat(status, projectsRepository.get(status)!!.isOldGoogleDriveProject, equalTo(true))
        }
    }

    @Test
    fun `GD projects with only sent forms should be deleted without being converted to ODK protocol and marked as old GD projects`() {
        projectsRepository.save(Project.Saved("1", "project", "Q", "#000000"))
        settingsProvider.getUnprotectedSettings("1").save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_GOOGLE_SHEETS)
        instancesRepositories["1"] = InMemInstancesRepository().apply {
            save(InstanceFixtures.instance(status = Instance.STATUS_SUBMITTED))
        }

        googleDriveProjectsDeleter.run()

        verify(projectDeleter).deleteProject("1")
        assertThat(settingsProvider.getUnprotectedSettings("1").getString(ProjectKeys.KEY_PROTOCOL), equalTo(ProjectKeys.PROTOCOL_GOOGLE_SHEETS))
        assertThat(projectsRepository.get("1")!!.isOldGoogleDriveProject, equalTo(false))
    }
}

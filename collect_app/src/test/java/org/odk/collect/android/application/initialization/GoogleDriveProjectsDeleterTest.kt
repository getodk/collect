package org.odk.collect.android.application.initialization

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.odk.collect.android.projects.DeleteProjectResult
import org.odk.collect.android.projects.ProjectDeleter
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class GoogleDriveProjectsDeleterTest {
    private val projectsRepository = InMemProjectsRepository()
    private val settingsProvider = InMemSettingsProvider()
    private val projectDeleter = mock<ProjectDeleter>()

    private val googleDriveProjectsDeleter = GoogleDriveProjectsDeleter(projectsRepository, settingsProvider, projectDeleter)

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
    fun `GD projects that cannot be deleted because of unsent forms should be converted to ODK protocol and marked as old GD projects`() {
        projectsRepository.save(Project.Saved("1", "project", "Q", "#000000"))
        settingsProvider.getUnprotectedSettings("1").save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_GOOGLE_SHEETS)
        whenever(projectDeleter.deleteProject("1")).thenReturn(DeleteProjectResult.UnsentInstances)

        googleDriveProjectsDeleter.run()

        assertThat(settingsProvider.getUnprotectedSettings("1").getString(ProjectKeys.KEY_PROTOCOL), equalTo(ProjectKeys.PROTOCOL_SERVER))
        assertThat(settingsProvider.getUnprotectedSettings("1").getString(ProjectKeys.KEY_SERVER_URL), equalTo("https://example.com"))
        assertThat(projectsRepository.get("1")!!.isOldGoogleDriveProject, equalTo(true))
    }

    @Test
    fun `GD projects that cannot be deleted because of running background jobs should be converted to ODK protocol and marked as old GD projects`() {
        projectsRepository.save(Project.Saved("1", "project", "Q", "#000000"))
        settingsProvider.getUnprotectedSettings("1").save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_GOOGLE_SHEETS)
        whenever(projectDeleter.deleteProject("1")).thenReturn(DeleteProjectResult.RunningBackgroundJobs)

        googleDriveProjectsDeleter.run()

        assertThat(settingsProvider.getUnprotectedSettings("1").getString(ProjectKeys.KEY_PROTOCOL), equalTo(ProjectKeys.PROTOCOL_SERVER))
        assertThat(settingsProvider.getUnprotectedSettings("1").getString(ProjectKeys.KEY_SERVER_URL), equalTo("https://example.com"))
        assertThat(projectsRepository.get("1")!!.isOldGoogleDriveProject, equalTo(true))
    }

    @Test
    fun `If GD project deletion results in 'DeletedSuccessfullyLastProject' there is no attempt to convert it to ODK protocol and mark it as an old GD project`() {
        projectsRepository.save(Project.Saved("1", "project", "Q", "#000000"))
        settingsProvider.getUnprotectedSettings("1").save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_GOOGLE_SHEETS)
        whenever(projectDeleter.deleteProject("1")).thenReturn(DeleteProjectResult.DeletedSuccessfullyLastProject)

        googleDriveProjectsDeleter.run()

        assertThat(settingsProvider.getUnprotectedSettings("1").getString(ProjectKeys.KEY_PROTOCOL), equalTo(ProjectKeys.PROTOCOL_GOOGLE_SHEETS))
        assertThat(projectsRepository.get("1")!!.isOldGoogleDriveProject, equalTo(false))
    }

    @Test
    fun `If GD project deletion results in 'DeletedSuccessfullyInactiveProject' there is no attempt to convert it to ODK protocol and mark it as an old GD project`() {
        projectsRepository.save(Project.Saved("1", "project", "Q", "#000000"))
        settingsProvider.getUnprotectedSettings("1").save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_GOOGLE_SHEETS)
        whenever(projectDeleter.deleteProject("1")).thenReturn(DeleteProjectResult.DeletedSuccessfullyInactiveProject)

        googleDriveProjectsDeleter.run()

        assertThat(settingsProvider.getUnprotectedSettings("1").getString(ProjectKeys.KEY_PROTOCOL), equalTo(ProjectKeys.PROTOCOL_GOOGLE_SHEETS))
        assertThat(projectsRepository.get("1")!!.isOldGoogleDriveProject, equalTo(false))
    }

    @Test
    fun `If GD project deletion results in 'DeletedSuccessfullyCurrentProject' there is no attempt to convert it to ODK protocol and mark it as an old GD project`() {
        projectsRepository.save(Project.Saved("1", "project", "Q", "#000000"))
        settingsProvider.getUnprotectedSettings("1").save(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_GOOGLE_SHEETS)
        whenever(projectDeleter.deleteProject("1")).thenReturn(DeleteProjectResult.DeletedSuccessfullyCurrentProject(mock()))

        googleDriveProjectsDeleter.run()

        assertThat(settingsProvider.getUnprotectedSettings("1").getString(ProjectKeys.KEY_PROTOCOL), equalTo(ProjectKeys.PROTOCOL_GOOGLE_SHEETS))
        assertThat(projectsRepository.get("1")!!.isOldGoogleDriveProject, equalTo(false))
    }
}

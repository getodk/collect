package org.odk.collect.android.application.initialization

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
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
}

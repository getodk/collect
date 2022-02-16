package org.odk.collect.android.projects

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.ODKAppSettingsImporter

class ProjectCreatorTest {
    private val json = "{\"general\":{\"server_url\":\"https:\\/\\/my-server.com\",\"username\":\"adam\",\"password\":\"1234\"},\"admin\":{}}"
    private val newProject = Project.New("my-server.com", "M", "#3e9fcc")
    private val savedProject = Project.Saved("1", newProject)

    private var projectsRepository = mock<ProjectsRepository> {
        on { getAll() } doReturn listOf(savedProject)
        on { save(Project.New("", "", "")) } doReturn savedProject
    }

    private var currentProjectProvider = mock<CurrentProjectProvider> {
        on { getCurrentProject() } doReturn savedProject
    }
    private var settingsImporter = mock<ODKAppSettingsImporter> {}

    private lateinit var projectCreator: ProjectCreator

    @Before
    fun setup() {
        projectCreator = ProjectCreator(
            projectsRepository,
            currentProjectProvider,
            settingsImporter
        )
    }

    @Test
    fun `Importing settings should be triggered with proper params`() {
        projectCreator.createNewProject(json)
        verify(settingsImporter).fromJSON(json, savedProject)
    }

    @Test
    fun `When importing settings failed createNewProject() should return false`() {
        whenever(settingsImporter.fromJSON(json, savedProject)).thenReturn(false)

        projectCreator.createNewProject(json)
        assertThat(projectCreator.createNewProject(json), `is`(false))
    }

    @Test
    fun `When importing settings succeeded createNewProject() should return true`() {
        whenever(settingsImporter.fromJSON(json, savedProject)).thenReturn(true)

        projectCreator.createNewProject(json)
        assertThat(projectCreator.createNewProject(json), `is`(true))
    }

    @Test
    fun `When importing settings failed should created project be deleted`() {
        whenever(settingsImporter.fromJSON(json, savedProject)).thenReturn(false)

        projectCreator.createNewProject(json)
        verify(projectsRepository).delete(savedProject.uuid)
    }

    @Test
    fun `New project id should be set`() {
        whenever(settingsImporter.fromJSON(json, savedProject)).thenReturn(true)

        projectCreator.createNewProject(json)
        verify(currentProjectProvider).setCurrentProject("1")
    }
}

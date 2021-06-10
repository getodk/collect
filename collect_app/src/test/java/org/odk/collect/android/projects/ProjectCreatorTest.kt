package org.odk.collect.android.projects

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.anyString
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.odk.collect.android.configure.SettingsImporter
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository

class ProjectCreatorTest {
    private val json = "{\"general\":{\"server_url\":\"https:\\/\\/my-server.com\",\"username\":\"adam\",\"password\":\"1234\"},\"admin\":{}}"
    private val newProject = Project.New("my-server.com", "M", "#3e9fcc")
    private val savedProject = Project.Saved("1", newProject)

    private var projectImporter = mock<ProjectImporter> {
        on { importNewProject(newProject) } doReturn savedProject
    }

    private var projectsRepository = mock<ProjectsRepository> {
        on { getAll() } doReturn listOf(savedProject)
    }

    private var currentProjectProvider = mock<CurrentProjectProvider> {}
    private var settingsImporter = mock<SettingsImporter> {}
    private var projectDetailsCreator = mock<ProjectDetailsCreator> {
        on { getProject("https://my-server.com") } doReturn newProject
    }

    private lateinit var projectCreator: ProjectCreator

    @Before
    fun setup() {
        projectCreator = ProjectCreator(projectImporter, projectsRepository, currentProjectProvider, settingsImporter, projectDetailsCreator)
    }

    @Test
    fun `Importing new project should be triggered with a proper project`() {
        projectCreator.createNewProject(json)
        verify(projectImporter).importNewProject(newProject)
    }

    @Test
    fun `Importing settings should be triggered with proper params`() {
        projectCreator.createNewProject(json)
        verify(settingsImporter).fromJSON(json, "1")
    }

    @Test
    fun `When importing settings failed createNewProject() should return false`() {
        whenever(settingsImporter.fromJSON(anyString(), anyString())).thenReturn(false)

        projectCreator.createNewProject(json)
        assertThat(projectCreator.createNewProject(json), `is`(false))
    }

    @Test
    fun `When importing settings succeeded createNewProject() should return true`() {
        whenever(settingsImporter.fromJSON(anyString(), anyString())).thenReturn(true)

        projectCreator.createNewProject(json)
        assertThat(projectCreator.createNewProject(json), `is`(true))
    }

    @Test
    fun `When importing settings failed should created project be deleted`() {
        whenever(settingsImporter.fromJSON(anyString(), anyString())).thenReturn(false)

        projectCreator.createNewProject(json)
        verify(projectsRepository).delete(savedProject.uuid)
    }

    @Test
    fun `New project id should be set if it's the only project`() {
        whenever(settingsImporter.fromJSON(anyString(), anyString())).thenReturn(true)

        projectCreator.createNewProject(json)
        verify(currentProjectProvider).setCurrentProject("1")

        whenever(projectsRepository.getAll()).thenReturn(listOf(savedProject, Project.Saved("2", "Project X", "X", "#cccccc")))
        projectCreator.createNewProject(json)
        verifyNoMoreInteractions(currentProjectProvider)
    }
}

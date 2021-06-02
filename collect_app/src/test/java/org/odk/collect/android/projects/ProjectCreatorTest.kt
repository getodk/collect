package org.odk.collect.android.projects

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.odk.collect.android.configure.SettingsImporter
import org.odk.collect.android.database.forms.FormsDatabaseProvider
import org.odk.collect.android.database.instances.InstancesDatabaseProvider
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository

class ProjectCreatorTest {
    private val json = "{\"general\":{\"server_url\":\"https:\\/\\/my-server.com\",\"username\":\"adam\",\"password\":\"1234\"},\"admin\":{}}"
    private val newProject = Project.New("my-server.com", "M", "#3e9fcc")
    private val savedProject = Project.Saved("1", newProject)

    private val projectImporter = mock<ProjectImporter> {
        on { importNewProject(newProject) } doReturn savedProject
    }

    private val projectsRepository = mock<ProjectsRepository> {
        on { getAll() } doReturn listOf(savedProject)
    }

    private val currentProjectProvider = mock<CurrentProjectProvider> {}
    private val settingsImporter = mock<SettingsImporter> {}
    private val formsDatabaseProvider = mock<FormsDatabaseProvider> {}
    private val instancesDatabaseProvider = mock<InstancesDatabaseProvider> {}

    private lateinit var projectCreator: ProjectCreator

    @Before
    fun setup() {
        projectCreator = ProjectCreator(projectImporter, projectsRepository, currentProjectProvider, settingsImporter, formsDatabaseProvider, instancesDatabaseProvider)
    }

    @Test
    fun `Importing new project should be triggered with a proper project`() {
        projectCreator.createNewProject(json)
        verify(projectImporter).importNewProject(newProject)
    }

    @Test
    fun `New project id should be set if it's the only project`() {
        projectCreator.createNewProject(json)
        verify(currentProjectProvider).setCurrentProject("1")

        `when`(projectsRepository.getAll()).thenReturn(listOf(savedProject, Project.Saved("2", "Project X", "X", "#cccccc")))
        projectCreator.createNewProject(json)
        verifyNoMoreInteractions(currentProjectProvider)
    }

    @Test
    fun `Databases should be released if new project has been set`() {
        projectCreator.createNewProject(json)
        verify(formsDatabaseProvider).releaseDatabaseHelper()
        verify(instancesDatabaseProvider).releaseDatabaseHelper()
    }

    @Test
    fun `Importing settings should be triggered`() {
        projectCreator.createNewProject(json)
        verify(settingsImporter).fromJSON(json, "1")
    }
}

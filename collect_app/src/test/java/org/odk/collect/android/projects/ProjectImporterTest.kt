package org.odk.collect.android.projects

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.odk.collect.android.projects.ProjectImporter.Companion.DEMO_PROJECT_ID
import org.odk.collect.android.storage.StorageInitializer
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository

class ProjectImporterTest {

    private lateinit var projectsRepository: ProjectsRepository
    private lateinit var storagePathProvider: StoragePathProvider
    private lateinit var projectImporter: ProjectImporter

    @Before
    fun setup() {
        projectsRepository = Mockito.mock(ProjectsRepository::class.java)
        storagePathProvider = Mockito.mock(StoragePathProvider::class.java)
        projectImporter = ProjectImporter(
            projectsRepository,
            Mockito.mock(CurrentProjectProvider::class.java),
            Mockito.mock(StorageInitializer::class.java)
        )
    }

    @Test
    fun `Default project should be imported when importDemoProject() called`() {
        projectImporter.importDemoProject()
        verify(projectsRepository).save(Project.Saved(DEMO_PROJECT_ID, "Demo project", "D", "#3e9fcc"))
    }

    @Test
    fun `Existed project should be imported when importExistingProject() called`() {
        projectImporter.importExistingProject()
        verify(projectsRepository).save(Project.Saved(DEMO_PROJECT_ID, "Demo project", "D", "#3e9fcc"))
    }
}

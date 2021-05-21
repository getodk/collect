package org.odk.collect.android.projects

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.contains
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.projects.ProjectImporter.Companion.DEMO_PROJECT_ID
import org.odk.collect.android.storage.StorageInitializer
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.shared.UUIDGenerator

class ProjectImporterTest {

    private val projectsRepository = InMemProjectsRepository(UUIDGenerator())
    private val storageInitializer = mock<StorageInitializer>()

    private val projectImporter = ProjectImporter(
        projectsRepository,
        storageInitializer
    )

    @Test
    fun `importDemoProject() creates demo project`() {
        projectImporter.importDemoProject()

        val demoProject = Project.Saved(DEMO_PROJECT_ID, "Demo project", "D", "#3e9fcc")
        assertThat(projectsRepository.getAll(), contains(demoProject))
    }

    @Test
    fun `importDemoProject() creates storage for project`() {
        projectImporter.importDemoProject()
        verify(storageInitializer).createProjectDirsOnStorage(projectsRepository.getAll()[0])
    }

    @Test
    fun `importExistingProject() creates existing project`() {
        projectImporter.importDemoProject()
        assertThat(projectsRepository.getAll().size, `is`(1))
    }

    @Test
    fun `importExistingProject() creates storage for project`() {
        projectImporter.importExistingProject()
        verify(storageInitializer).createProjectDirsOnStorage(projectsRepository.getAll()[0])
    }
}

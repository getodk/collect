package org.odk.collect.android.projects

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.contains
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.shared.strings.UUIDGenerator

@RunWith(AndroidJUnit4::class)
class ProjectImporterTest {

    private val projectsRepository = InMemProjectsRepository(UUIDGenerator())
    private val projectImporter = ProjectImporter(projectsRepository)

    @Test
    fun `importNewProject() creates new project`() {
        val newProject = Project.New("Project X", "X", "#cccccc")
        val savedProject = projectImporter.importNewProject(newProject)

        assertThat(projectsRepository.getAll(), contains(savedProject))
    }
}

package org.odk.collect.android.projects

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.contains
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.ProjectImporter.Companion.DEMO_PROJECT_ID
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.shared.TempFiles
import org.odk.collect.shared.strings.UUIDGenerator
import java.io.File

@RunWith(AndroidJUnit4::class)
class ProjectImporterTest {

    private val projectsRepository = InMemProjectsRepository(UUIDGenerator())

    private val rootDir = TempFiles.createTempDir()

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val storagePathProvider = StoragePathProvider(mock(), rootDir.absolutePath)
    private val settingsProvider = SettingsProvider(context)

    private val projectImporter = ProjectImporter(
        storagePathProvider,
        projectsRepository
    )

    @Test
    fun `importNewProject() creates new project`() {
        val newProject = Project.New("Project X", "X", "#cccccc")
        val savedProject = projectImporter.importNewProject(newProject)

        assertThat(projectsRepository.getAll(), contains(savedProject))
    }

    @Test
    fun `importNewProject() creates storage for project`() {
        val newProject = Project.New("Project X", "X", "#cccccc")
        val savedProject = projectImporter.importNewProject(newProject)

        storagePathProvider.getProjectDirPaths(savedProject.uuid).forEach {
            val dir = File(it)
            assertThat(dir.exists(), `is`(true))
            assertThat(dir.isDirectory, `is`(true))
        }
    }

    @Test
    fun `importDemoProject() creates demo project`() {
        projectImporter.importDemoProject()

        val demoProject = Project.Saved(DEMO_PROJECT_ID, "Demo project", "D", "#3e9fcc")
        assertThat(projectsRepository.getAll(), contains(demoProject))
    }

    @Test
    fun `importDemoProject() creates storage for project`() {
        projectImporter.importDemoProject()

        val demoProject = Project.Saved(DEMO_PROJECT_ID, "Demo project", "D", "#3e9fcc")
        storagePathProvider.getProjectDirPaths(demoProject.uuid).forEach {
            val dir = File(it)
            assertThat(dir.exists(), `is`(true))
            assertThat(dir.isDirectory, `is`(true))
        }
    }
}

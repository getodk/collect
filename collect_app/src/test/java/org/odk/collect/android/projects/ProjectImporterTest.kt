package org.odk.collect.android.projects

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.contains
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.ProjectImporter.Companion.DEMO_PROJECT_ID
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.shared.TempFiles
import org.odk.collect.shared.UUIDGenerator
import java.io.File

@RunWith(AndroidJUnit4::class)
class ProjectImporterTest {

    private val projectsRepository = InMemProjectsRepository(UUIDGenerator())

    private val rootDir = TempFiles.createTempDir()

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val storagePathProvider = StoragePathProvider(null, rootDir.absolutePath)
    private val settingsProvider = SettingsProvider(context)

    private val projectImporter = ProjectImporter(
        context,
        storagePathProvider,
        projectsRepository,
        settingsProvider
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

        val demoProject = Project.Saved(DEMO_PROJECT_ID, "Demo project", "D", "#3e9fcc")
        storagePathProvider.getProjectDirPaths(demoProject).forEach {
            val dir = File(it)
            assertThat(dir.exists(), `is`(true))
            assertThat(dir.isDirectory, `is`(true))
        }
    }

    @Test
    fun `importExistingProject() creates existing project`() {
        projectImporter.importDemoProject()
        assertThat(projectsRepository.getAll().size, `is`(1))
    }

    @Test
    fun `importExistingProject() moves files from root`() {
        val legacyRootDirs = listOf(
            File(rootDir, "forms"),
            File(rootDir, "instances"),
            File(rootDir, "metadata"),
            File(rootDir, "layers"),
            File(rootDir, ".cache"),
            File(rootDir, "settings")
        )

        legacyRootDirs.forEach {
            it.mkdir()
            TempFiles.createTempFile(it, "file", ".temp")
        }

        val existingProject = projectImporter.importExistingProject()

        legacyRootDirs.forEach {
            assertThat(it.exists(), `is`(false))
        }

        storagePathProvider.getProjectDirPaths(existingProject).forEach {
            val dir = File(it)
            assertThat(dir.exists(), `is`(true))
            assertThat(dir.isDirectory, `is`(true))
            assertThat(dir.listFiles()!!.isEmpty(), `is`(false))
        }
    }

    @Test
    fun `importExistingProject() still copies other files if a directory is missing`() {
        val legacyRootDirsWithoutForms = listOf(
            File(rootDir, "instances"),
            File(rootDir, "metadata"),
            File(rootDir, "layers"),
            File(rootDir, ".cache"),
            File(rootDir, "settings")
        )

        legacyRootDirsWithoutForms.forEach {
            it.mkdir()
            TempFiles.createTempFile(it, "file", ".temp")
        }

        val existingProject = projectImporter.importExistingProject()

        storagePathProvider.getProjectDirPaths(existingProject).forEach {
            val dir = File(it)
            assertThat(dir.exists(), `is`(true))
            assertThat(dir.isDirectory, `is`(true))

            if (it.endsWith("forms")) {
                assertThat(dir.listFiles()!!.isEmpty(), `is`(true))
            } else {
                assertThat(dir.listFiles()!!.isEmpty(), `is`(false))
            }
        }
    }

    @Test
    fun `importExistingProject migrates general and admin settings`() {
        val oldGeneralSettings = PreferenceManager.getDefaultSharedPreferences(context)
        oldGeneralSettings.edit().putString("generalKey", "generalValue").apply()
        val oldAdminSettings = context.getSharedPreferences("admin", Context.MODE_PRIVATE)
        oldAdminSettings.edit().putString("adminKey", "adminValue").apply()

        val existingProject = projectImporter.importExistingProject()

        val generalSettings = settingsProvider.getGeneralSettings(existingProject.uuid)
        assertThat(generalSettings.getString("generalKey"), `is`("generalValue"))
        val adminSettings = settingsProvider.getAdminSettings(existingProject.uuid)
        assertThat(adminSettings.getString("adminKey"), `is`("adminValue"))
    }
}

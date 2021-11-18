package org.odk.collect.android.projects

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.shared.TempFiles
import java.io.File

@RunWith(AndroidJUnit4::class)
class ExistingProjectMigratorTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val component = DaggerUtils.getComponent(context)
    private val existingProjectMigrator = component.existingProjectMigrator()
    private val storagePathProvider = component.storagePathProvider()
    private val projectsRepository = component.projectsRepository()
    private val settingsProvider = component.settingsProvider()
    private val currentProjectProvider = component.currentProjectProvider()
    private val rootDir = storagePathProvider.odkRootDirPath

    @Test
    fun `creates existing project with details based on its url`() {
        PreferenceManager
            .getDefaultSharedPreferences(context)
            .edit()
            .putString(ProjectKeys.KEY_SERVER_URL, "https://my-server.com")
            .apply()

        existingProjectMigrator.run()

        assertThat(projectsRepository.getAll().size, `is`(1))

        val project = projectsRepository.getAll()[0]
        assertThat(project.name, `is`("my-server.com"))
        assertThat(project.icon, `is`("M"))
        assertThat(project.color, `is`("#53bdd4"))
    }

    @Test
    fun `moves files from root`() {
        val legacyRootDirs = listOf(
            File(rootDir, "forms"),
            File(rootDir, "instances"),
            File(rootDir, "metadata"),
            File(rootDir, "layers"),
            File(rootDir, "settings")
        )

        legacyRootDirs.forEach {
            it.mkdir()
            TempFiles.createTempFile(it, "file", ".temp")
        }

        existingProjectMigrator.run()
        val existingProject = currentProjectProvider.getCurrentProject()

        legacyRootDirs.forEach {
            assertThat(it.exists(), `is`(false))
        }

        getProjectDirPaths(existingProject.uuid).forEach {
            val dir = File(it)
            assertThat(dir.exists(), `is`(true))
            assertThat(dir.isDirectory, `is`(true))
            if (!it.endsWith("cache")) {
                assertThat(dir.listFiles()!!.isEmpty(), `is`(false))
            }
        }
    }

    @Test
    fun `deletes and does not move cache directory`() {
        val cacheDir = File(rootDir, ".cache")
        cacheDir.mkdir()
        TempFiles.createTempFile(cacheDir, "file", ".temp")

        existingProjectMigrator.run()
        val existingProject = currentProjectProvider.getCurrentProject()

        assertThat(cacheDir.exists(), `is`(false))

        getProjectDirPaths(existingProject.uuid).forEach {
            val dir = File(it)
            assertThat(dir.exists(), `is`(true))
            assertThat(dir.isDirectory, `is`(true))
            assertThat(dir.listFiles()!!.isEmpty(), `is`(true))
        }
    }

    @Test
    fun `if cache dir can not be deleted the app does not crash`() {
        val cacheDir = File(rootDir, ".cache")
        cacheDir.createNewFile()

        existingProjectMigrator.run()
        val existingProject = currentProjectProvider.getCurrentProject()

        assertThat(cacheDir.exists(), `is`(true))

        getProjectDirPaths(existingProject.uuid).forEach {
            val dir = File(it)
            assertThat(dir.exists(), `is`(true))
            assertThat(dir.isDirectory, `is`(true))
            assertThat(dir.listFiles()!!.isEmpty(), `is`(true))
        }
    }

    @Test
    fun `still copies other files if a directory is missing`() {
        val legacyRootDirsWithoutForms = listOf(
            File(rootDir, "instances"),
            File(rootDir, "metadata"),
            File(rootDir, "layers"),
            File(rootDir, "settings")
        )

        legacyRootDirsWithoutForms.forEach {
            it.mkdir()
            TempFiles.createTempFile(it, "file", ".temp")
        }

        existingProjectMigrator.run()
        val existingProject = currentProjectProvider.getCurrentProject()
        getProjectDirPaths(existingProject.uuid).forEach {
            val dir = File(it)
            assertThat(dir.exists(), `is`(true))
            assertThat(dir.isDirectory, `is`(true))

            if (it.endsWith("forms") || it.endsWith("cache")) {
                assertThat(dir.listFiles()!!.isEmpty(), `is`(true))
            } else {
                assertThat(dir.listFiles()!!.isEmpty(), `is`(false))
            }
        }
    }

    @Test
    fun `migrates unprotected and protected settings`() {
        val oldGeneralSettings = PreferenceManager.getDefaultSharedPreferences(context)
        oldGeneralSettings.edit().putString("generalKey", "generalValue").apply()
        val oldAdminSettings = context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
        oldAdminSettings.edit().putString("adminKey", "adminValue").apply()

        existingProjectMigrator.run()
        val existingProject = currentProjectProvider.getCurrentProject()

        val generalSettings = settingsProvider.getUnprotectedSettings(existingProject.uuid)
        assertThat(
            generalSettings.getString("generalKey"),
            `is`("generalValue")
        )
        val adminSettings = settingsProvider.getProtectedSettings(existingProject.uuid)
        assertThat(adminSettings.getString("adminKey"), `is`("adminValue"))
    }

    @Test
    fun `has key`() {
        assertThat(existingProjectMigrator.key(), `is`(MetaKeys.EXISTING_PROJECT_IMPORTED))
    }

    private fun getProjectDirPaths(projectId: String): Array<String> {
        return arrayOf(
            storagePathProvider.getOdkDirPath(StorageSubdirectory.FORMS, projectId),
            storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, projectId),
            storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, projectId),
            storagePathProvider.getOdkDirPath(StorageSubdirectory.METADATA, projectId),
            storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS, projectId),
            storagePathProvider.getOdkDirPath(StorageSubdirectory.SETTINGS, projectId)
        )
    }
}

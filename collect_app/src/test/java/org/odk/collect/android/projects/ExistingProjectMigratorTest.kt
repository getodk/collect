package org.odk.collect.android.projects

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.injection.DaggerUtils
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
    private val rootDir = storagePathProvider.odkRootDirPath

    @Test
    fun `creates existing project`() {
        existingProjectMigrator.migrate()
        assertThat(projectsRepository.getAll().size, Matchers.`is`(1))
    }

    @Test
    fun `moves files from root`() {
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

        val existingProject = existingProjectMigrator.migrate()

        legacyRootDirs.forEach {
            assertThat(it.exists(), Matchers.`is`(false))
        }

        storagePathProvider.getProjectDirPaths(existingProject.uuid).forEach {
            val dir = File(it)
            assertThat(dir.exists(), Matchers.`is`(true))
            assertThat(dir.isDirectory, Matchers.`is`(true))
            assertThat(dir.listFiles()!!.isEmpty(), Matchers.`is`(false))
        }
    }

    @Test
    fun `still copies other files if a directory is missing`() {
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

        val existingProject = existingProjectMigrator.migrate()
        storagePathProvider.getProjectDirPaths(existingProject.uuid).forEach {
            val dir = File(it)
            assertThat(dir.exists(), Matchers.`is`(true))
            assertThat(dir.isDirectory, Matchers.`is`(true))

            if (it.endsWith("forms")) {
                assertThat(dir.listFiles()!!.isEmpty(), Matchers.`is`(true))
            } else {
                assertThat(dir.listFiles()!!.isEmpty(), Matchers.`is`(false))
            }
        }
    }

    @Test
    fun `migrates general and admin settings`() {
        val oldGeneralSettings = PreferenceManager.getDefaultSharedPreferences(context)
        oldGeneralSettings.edit().putString("generalKey", "generalValue").apply()
        val oldAdminSettings = context.getSharedPreferences("admin", Context.MODE_PRIVATE)
        oldAdminSettings.edit().putString("adminKey", "adminValue").apply()

        val existingProject = existingProjectMigrator.migrate()

        val generalSettings = settingsProvider.getGeneralSettings(existingProject.uuid)
        assertThat(
            generalSettings.getString("generalKey"),
            Matchers.`is`("generalValue")
        )
        val adminSettings = settingsProvider.getAdminSettings(existingProject.uuid)
        assertThat(adminSettings.getString("adminKey"), Matchers.`is`("adminValue"))
    }
}

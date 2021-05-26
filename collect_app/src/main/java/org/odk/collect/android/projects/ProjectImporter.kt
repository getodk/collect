package org.odk.collect.android.projects

import android.content.Context
import androidx.preference.PreferenceManager
import org.apache.commons.io.FileUtils.moveDirectoryToDirectory
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.projects.Project
import org.odk.collect.projects.Project.Saved
import org.odk.collect.projects.ProjectsRepository
import java.io.File
import java.io.FileNotFoundException

class ProjectImporter(
    private val context: Context,
    private val storagePathProvider: StoragePathProvider,
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider
) {
    fun importDemoProject() {
        val project = Saved(DEMO_PROJECT_ID, "Demo project", "D", "#3e9fcc")
        projectsRepository.save(project)
        setupProject(project)
    }

    fun importExistingProject(): Saved {
        val project = projectsRepository.save(Project.New("Existing project", "E", "#3e9fcc"))

        val rootDir = storagePathProvider.odkRootDirPath
        listOf(
            File(rootDir, "forms"),
            File(rootDir, "instances"),
            File(rootDir, "metadata"),
            File(rootDir, "layers"),
            File(rootDir, ".cache"),
            File(rootDir, "settings")
        ).forEach {
            try {
                val projectDir = File(storagePathProvider.getProjectRootDirPath(project))
                moveDirectoryToDirectory(it, projectDir, true)
            } catch (_: FileNotFoundException) {
                // Original dir doesn't exist - no  need to copy
            }
        }

        val generalSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val adminSharedPrefs = context.getSharedPreferences("admin", Context.MODE_PRIVATE)
        settingsProvider.getGeneralSettings(project.uuid).saveAll(generalSharedPrefs.all)
        settingsProvider.getAdminSettings(project.uuid).saveAll(adminSharedPrefs.all)

        setupProject(project)
        return project
    }

    fun setupProject(project: Saved) {
        createProjectDirs(project)
    }

    private fun createProjectDirs(project: Saved) {
        storagePathProvider.getProjectDirPaths(project).forEach { FileUtils.createDir(it) }
    }

    companion object {
        const val DEMO_PROJECT_ID = "DEMO"
    }
}

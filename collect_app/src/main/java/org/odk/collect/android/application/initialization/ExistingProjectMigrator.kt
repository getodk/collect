package org.odk.collect.android.application.initialization

import android.content.Context
import androidx.preference.PreferenceManager
import org.apache.commons.io.FileUtils
import org.odk.collect.android.application.initialization.upgrade.Upgrade
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import java.io.File
import java.io.FileNotFoundException

class ExistingProjectMigrator(
    private val context: Context,
    private val storagePathProvider: StoragePathProvider,
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider,
    private val currentProjectProvider: CurrentProjectProvider
) : Upgrade {

    override fun key(): String {
        return MetaKeys.EXISTING_PROJECT_IMPORTED
    }

    override fun run() {
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
                val rootPath = storagePathProvider.getProjectRootDirPath(project.uuid)
                FileUtils.moveDirectoryToDirectory(it, File(rootPath), true)
            } catch (_: FileNotFoundException) {
                // Original dir doesn't exist - no  need to copy
            }
        }

        val generalSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val adminSharedPrefs = context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
        settingsProvider.getGeneralSettings(project.uuid).saveAll(generalSharedPrefs.all)
        settingsProvider.getAdminSettings(project.uuid).saveAll(adminSharedPrefs.all)

        createProjectDirs(project)

        currentProjectProvider.setCurrentProject(project.uuid)
    }

    private fun createProjectDirs(project: Project.Saved) {
        storagePathProvider.getProjectDirPaths(project.uuid)
            .forEach { org.odk.collect.android.utilities.FileUtils.createDir(it) }
    }
}

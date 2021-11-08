package org.odk.collect.android.application.initialization

import android.content.Context
import androidx.preference.PreferenceManager
import org.apache.commons.io.FileUtils
import org.odk.collect.android.application.initialization.upgrade.Upgrade
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.projects.ProjectDetailsCreator
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.projects.ProjectsRepository
import java.io.File
import java.io.FileNotFoundException

/**
 * This migrates from a version before Projects were introduced. Any data and settings will be
 * used to create a new project which is then set as the current one This means the user never
 * goes through the "first launch" experience and has their setup available to them immediately
 * after upgrade.
 */
class ExistingProjectMigrator(
    private val context: Context,
    private val storagePathProvider: StoragePathProvider,
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider,
    private val currentProjectProvider: CurrentProjectProvider,
    private val projectDetailsCreator: ProjectDetailsCreator
) : Upgrade {

    override fun key(): String {
        return MetaKeys.EXISTING_PROJECT_IMPORTED
    }

    override fun run() {
        val generalSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        val newProject = projectDetailsCreator.createProjectFromDetails(
            connectionIdentifier = generalSharedPrefs.getString(
                ProjectKeys.KEY_SERVER_URL,
                ""
            ) ?: ""
        )
        val project = projectsRepository.save(newProject)

        val rootDir = storagePathProvider.odkRootDirPath
        listOf(
            File(rootDir, "forms"),
            File(rootDir, "instances"),
            File(rootDir, "metadata"),
            File(rootDir, "layers"),
            File(rootDir, "settings")
        ).forEach {
            try {
                val rootPath = storagePathProvider.getProjectRootDirPath(project.uuid)
                FileUtils.moveDirectoryToDirectory(it, File(rootPath), true)
            } catch (_: FileNotFoundException) {
                // Original dir doesn't exist - no  need to copy
            }
        }

        try {
            FileUtils.deleteDirectory(File(rootDir, ".cache"))
        } catch (e: Exception) {
            // ignore
        }

        val adminSharedPrefs = context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE)
        settingsProvider.getUnprotectedSettings(project.uuid).saveAll(generalSharedPrefs.all)
        settingsProvider.getProtectedSettings(project.uuid).saveAll(adminSharedPrefs.all)

        currentProjectProvider.setCurrentProject(project.uuid)
    }
}

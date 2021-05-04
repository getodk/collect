package org.odk.collect.android.projects

import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository

class CurrentProjectProvider(private val settingsProvider: SettingsProvider, private val projectsRepository: ProjectsRepository) {

    fun getCurrentProjectId(): String {
        return settingsProvider.getMetaSettings().getString(MetaKeys.CURRENT_PROJECT_ID) ?: ""
    }

    fun getCurrentProject(): Project? {
        return projectsRepository.get(getCurrentProjectId())
    }

    fun setCurrentProject(uuid: String) {
        settingsProvider.getMetaSettings().save(MetaKeys.CURRENT_PROJECT_ID, uuid)

        val storageInitializer = DaggerUtils.getComponent(Collect.getInstance()).storageInitializer()
        val formsDatabaseProvider = DaggerUtils.getComponent(Collect.getInstance()).formsDatabaseProvider()
        val instancesDatabaseProvider = DaggerUtils.getComponent(Collect.getInstance()).instancesDatabaseProvider()
        storageInitializer.createProjectDirsOnStorage()
        formsDatabaseProvider.releaseDatabaseHelper()
        instancesDatabaseProvider.releaseDatabaseHelper()
    }
}

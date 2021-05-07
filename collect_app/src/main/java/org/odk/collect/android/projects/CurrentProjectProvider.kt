
package org.odk.collect.android.projects

import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository

class CurrentProjectProvider(private val settingsProvider: SettingsProvider, private val projectsRepository: ProjectsRepository) {

    fun getCurrentProjectId(): String {
        return settingsProvider.getMetaSettings().getString(MetaKeys.CURRENT_PROJECT_ID) ?: ""
    }

    fun getCurrentProject(): Project? {
        // This should be removed. It was added just to fix tests because ResetStateRule which should be responsible for it is not always called first
        if (projectsRepository.getAll().isEmpty()) {
            ProjectImporter(projectsRepository, settingsProvider.getMetaSettings()).importDemoProject()
        }

        return projectsRepository.get(getCurrentProjectId())
    }

    fun setCurrentProject(uuid: String) {
        settingsProvider.getMetaSettings().save(MetaKeys.CURRENT_PROJECT_ID, uuid)
    }
}

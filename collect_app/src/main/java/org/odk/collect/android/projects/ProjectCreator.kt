package org.odk.collect.android.projects

import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.configure.SettingsImporter
import org.odk.collect.android.configure.qr.AppConfigurationKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.projects.ProjectsRepository

class ProjectCreator(
    private val projectImporter: ProjectImporter,
    private val projectsRepository: ProjectsRepository,
    private val currentProjectProvider: CurrentProjectProvider,
    private val settingsImporter: SettingsImporter,
    private val projectDetailsCreator: ProjectDetailsCreator
) {

    fun createNewProject(settingsJson: String): Boolean {
        val newProject = projectDetailsCreator.getProject(getServerUrl(settingsJson))
        val savedProject = projectImporter.importNewProject(newProject)

        val settingsImportedSuccessfully = settingsImporter.fromJSON(settingsJson, savedProject)

        return if (settingsImportedSuccessfully) {
            if (projectsRepository.getAll().size == 1) {
                currentProjectProvider.setCurrentProject(savedProject.uuid)
            }
            true
        } else {
            projectsRepository.delete(savedProject.uuid)
            false
        }
    }

    private fun getServerUrl(settingsJson: String): String {
        val url = try {
            JSONObject(settingsJson)
                .getJSONObject(AppConfigurationKeys.GENERAL)
                .getString(GeneralKeys.KEY_SERVER_URL)
        } catch (e: JSONException) {
            ""
        }
        return if (url.isNotBlank()) url else "https://demo.getodk.org"
    }
}

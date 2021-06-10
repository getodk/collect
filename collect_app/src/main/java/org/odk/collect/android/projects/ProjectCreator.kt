package org.odk.collect.android.projects

import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.configure.SettingsImporter
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
        val urlString = try {
            JSONObject(settingsJson)
                .getJSONObject("general")
                .getString(GeneralKeys.KEY_SERVER_URL)
        } catch (e: JSONException) {
            ""
        }

        val newProject = projectDetailsCreator.getProject(urlString)
        val savedProject = projectImporter.importNewProject(newProject)

        val settingsImportedSuccessfully = settingsImporter.fromJSON(settingsJson, savedProject.uuid)

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
}

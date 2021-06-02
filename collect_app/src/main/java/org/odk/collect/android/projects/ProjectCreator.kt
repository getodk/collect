package org.odk.collect.android.projects

import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.configure.SettingsImporter
import org.odk.collect.android.database.forms.FormsDatabaseProvider
import org.odk.collect.android.database.instances.InstancesDatabaseProvider
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import java.net.URL

class ProjectCreator(
    private val projectImporter: ProjectImporter,
    private val projectsRepository: ProjectsRepository,
    private val currentProjectProvider: CurrentProjectProvider,
    private val settingsImporter: SettingsImporter,
    private val formsDatabaseProvider: FormsDatabaseProvider,
    private val instancesDatabaseProvider: InstancesDatabaseProvider
) {

    fun createNewProject(settingsJson: String): Boolean {
        val newProject = getNewProject(settingsJson)
        val savedProject = projectImporter.importNewProject(newProject)

        val settingsImportedSuccessfully = settingsImporter.fromJSON(settingsJson, savedProject.uuid)

        return if (settingsImportedSuccessfully) {
            if (projectsRepository.getAll().size == 1) {
                currentProjectProvider.setCurrentProject(savedProject.uuid)
                formsDatabaseProvider.releaseDatabaseHelper()
                instancesDatabaseProvider.releaseDatabaseHelper()
            }
            true
        } else {
            projectsRepository.delete(savedProject.uuid)
            false
        }
    }

    private fun getNewProject(settingsJson: String): Project.New {
        val urlString = try {
            JSONObject(settingsJson)
                .getJSONObject("general")
                .getString(GeneralKeys.KEY_SERVER_URL)
        } catch (e: JSONException) {
            ""
        }

        var projectName = ""
        var projectIcon = ""
        try {
            val url = URL(urlString)
            projectName = url.host
            projectIcon = projectName.first().toUpperCase().toString()
        } catch (e: Exception) {
        }
        return Project.New(projectName, projectIcon, "#3e9fcc")
    }
}

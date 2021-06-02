package org.odk.collect.android.projects

import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.configure.SettingsImporter
import org.odk.collect.android.database.forms.FormsDatabaseProvider
import org.odk.collect.android.database.instances.InstancesDatabaseProvider
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.projects.ProjectGenerator
import org.odk.collect.projects.ProjectsRepository

class ProjectCreator(
    private val projectImporter: ProjectImporter,
    private val projectsRepository: ProjectsRepository,
    private val currentProjectProvider: CurrentProjectProvider,
    private val settingsImporter: SettingsImporter,
    private val formsDatabaseProvider: FormsDatabaseProvider,
    private val instancesDatabaseProvider: InstancesDatabaseProvider
) {

    fun createNewProject(settingsJson: String) {
        val newProject = ProjectGenerator.generateProject(getUrl(settingsJson))
        val savedProject = projectImporter.importNewProject(newProject)

        if (projectsRepository.getAll().size == 1) {
            currentProjectProvider.setCurrentProject(savedProject.uuid)
            formsDatabaseProvider.releaseDatabaseHelper()
            instancesDatabaseProvider.releaseDatabaseHelper()
        }

        settingsImporter.fromJSON(settingsJson, savedProject.uuid)
    }

    private fun getUrl(json: String): String {
        return try {
            JSONObject(json)
                .getJSONObject("general")
                .getString(GeneralKeys.KEY_SERVER_URL)
        } catch (e: JSONException) {
            ""
        }
    }
}

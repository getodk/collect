package org.odk.collect.android.projects

import android.content.Context
import androidx.core.content.ContextCompat
import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.configure.SettingsImporter
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import java.net.URL

class ProjectCreator(
    private val projectImporter: ProjectImporter,
    private val projectsRepository: ProjectsRepository,
    private val currentProjectProvider: CurrentProjectProvider,
    private val settingsImporter: SettingsImporter,
    private val context: Context
) {

    fun createNewProject(settingsJson: String): Boolean {
        val newProject = getNewProject(settingsJson)
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
        var projectColor = "#3e9fcc"
        try {
            val url = URL(urlString)
            projectName = url.host
            projectIcon = projectName.first().toUpperCase().toString()
            projectColor = getProjectColorForProjectName(projectName)
        } catch (e: Exception) {
        }
        return Project.New(projectName, projectIcon, projectColor)
    }

    private fun getProjectColorForProjectName(projectName: String): String {
        val colorId = (projectName.hashCode() % 15) + 1
        val colorName = "color$colorId"
        val colorValue = context.resources.getIdentifier(colorName, "color", context.packageName)

        return "#${Integer.toHexString(ContextCompat.getColor(context, colorValue)).substring(2)}"
    }
}

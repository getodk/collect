package org.odk.collect.android.projects

import android.content.Context
import androidx.core.content.ContextCompat
import org.odk.collect.projects.Project
import java.net.URL

class ProjectDetailsCreator(private val context: Context) {

    fun getProject(urlString: String): Project.New {
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

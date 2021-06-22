package org.odk.collect.android.projects

import android.content.Context
import androidx.core.content.ContextCompat
import org.odk.collect.android.R
import org.odk.collect.projects.Project
import java.net.URL
import kotlin.math.abs

class ProjectDetailsCreator(private val context: Context) {

    fun getProject(urlString: String): Project {
        return if (urlString.startsWith(context.getString(R.string.default_server_url))) {
            Project.DEMO_PROJECT
        } else {
            try {
                val url = URL(urlString)
                val projectName = url.host
                val projectIcon = projectName.first().toUpperCase().toString()
                val projectColor = getProjectColorForProjectName(projectName)

                Project.New(projectName, projectIcon, projectColor)
            } catch (e: Exception) {
                Project.New("Project", "P", "#3e9fcc")
            }
        }
    }

    private fun getProjectColorForProjectName(projectName: String): String {
        val colorId = (abs(projectName.hashCode()) % 15) + 1
        val colorName = "color$colorId"
        val colorValue = context.resources.getIdentifier(colorName, "color", context.packageName)

        return "#${Integer.toHexString(ContextCompat.getColor(context, colorValue)).substring(2)}"
    }
}

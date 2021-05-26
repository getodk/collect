package org.odk.collect.projects

import java.net.URL

object ProjectDetailsGenerator {
    fun generateProjectDetails(urlString: String): GeneratedProjectDetails {
        var projectName = ""
        var projectIcon = ""
        try {
            val url = URL(urlString)
            projectName = url.host
            projectIcon = projectName.first().toUpperCase().toString()
        } catch (e: Exception) {
        }
        return GeneratedProjectDetails(projectName, projectIcon, "#3e9fcc")
    }

    data class GeneratedProjectDetails(
        val projectName: String,
        val projectIcon: String,
        val projectColor: String
    )
}

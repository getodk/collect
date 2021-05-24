package org.odk.collect.projects

import java.net.URL

object ProjectDetailsGenerator {
    fun getProjectNameAndIconFromUrl(urlString: String): Pair<String, String> {
        var projectName = ""
        var projectIcon = ""
        try {
            val url = URL(urlString)
            projectName = url.host
            if (projectName.startsWith("www.")) {
                projectName = projectName.substring(4)
            }
            projectName = projectName.substring(0, projectName.indexOf("."))

            projectIcon = projectName.first().toUpperCase().toString()
        } catch (e: Exception) {
        }
        return Pair(projectName, projectIcon)
    }
}

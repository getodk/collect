package org.odk.collect.projects

import java.net.URL

object ProjectDetailsGenerator {
    fun getProjectNameAndIconFromUrl(urlString: String): Pair<String, String> {
        var projectName = ""
        var projectIcon = ""
        try {
            val url = URL(urlString)
            projectName = url.host.substring(0, url.host.indexOf("."))
            projectIcon = projectName.first().toUpperCase().toString()
        } catch (e: Exception) {
        }
        return Pair(projectName, projectIcon)
    }
}

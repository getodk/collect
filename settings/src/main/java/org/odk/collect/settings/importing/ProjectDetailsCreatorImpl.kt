package org.odk.collect.settings.importing

import org.odk.collect.projects.Project
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.collections.CollectionExtensions.itemFromHashOf
import org.odk.collect.shared.strings.StringUtils
import java.net.URL
import java.util.regex.Pattern

class ProjectDetailsCreatorImpl(private val colors: List<String>, private val defaults: Map<String, Any>) : ProjectDetailsCreator {

    override fun createProjectFromDetails(name: String, icon: String, color: String, connectionIdentifier: String): Project {
        val projectName = if (name.isNotBlank()) {
            name
        } else {
            getProjectNameFromConnectionIdentifier(connectionIdentifier)
        }

        val projectIcon = if (icon.isNotBlank()) {
            StringUtils.firstCharacterOrEmoji(icon)
        } else {
            projectName.first().toUpperCase().toString()
        }

        val projectColor = if (isProjectColorValid(color)) {
            color
        } else {
            getProjectColorFromProjectName(projectName)
        }

        return Project.New(projectName, projectIcon, projectColor)
    }

    private fun getProjectNameFromConnectionIdentifier(connectionIdentifier: String): String {
        val defaultServer = defaults[ProjectKeys.KEY_SERVER_URL] as String

        return if (connectionIdentifier.isBlank() || connectionIdentifier.startsWith(defaultServer)) {
            Project.DEMO_PROJECT_NAME
        } else {
            try {
                URL(connectionIdentifier).host
            } catch (e: Exception) {
                connectionIdentifier
            }
        }
    }

    private fun getProjectColorFromProjectName(projectName: String): String {
        if (projectName == Project.DEMO_PROJECT_NAME) {
            return Project.DEMO_PROJECT_COLOR
        }

        return colors.itemFromHashOf(projectName)
    }

    private fun isProjectColorValid(hexColor: String): Boolean {
        return Pattern
            .compile("^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})\$", Pattern.CASE_INSENSITIVE)
            .matcher(hexColor)
            .matches()
    }
}

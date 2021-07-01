package org.odk.collect.android.projects

import android.content.Context
import androidx.core.content.ContextCompat
import org.odk.collect.android.R
import org.odk.collect.projects.Project
import java.net.URL
import java.util.Locale
import java.util.regex.Pattern
import kotlin.math.abs

class ProjectDetailsCreator(private val context: Context) {

    fun createProjectFromDetails(name: String = "", icon: String = "", color: String = "", connectionIdentifier: String = ""): Project {
        val projectName = if (name.isNotBlank()) {
            name
        } else {
            getProjectNameFromConnectionIdentifier(connectionIdentifier)
        }

        val projectIcon = if (icon.isNotBlank()) {
            if (Character.codePointCount(icon, 0, icon.length) == 1) {
                icon.toUpperCase(Locale.US)
            } else {
                getFirstSign(icon)
            }
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
        return if (connectionIdentifier.isBlank() || connectionIdentifier.startsWith(context.getString(R.string.default_server_url))) {
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

        val colorId = (abs(projectName.hashCode()) % 15) + 1
        val colorName = "color$colorId"
        val colorValue = context.resources.getIdentifier(colorName, "color", context.packageName)

        return "#${Integer.toHexString(ContextCompat.getColor(context, colorValue)).substring(2)}"
    }

    private fun isProjectColorValid(hexColor: String): Boolean {
        return Pattern
            .compile("^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})\$", Pattern.CASE_INSENSITIVE)
            .matcher(hexColor)
            .matches()
    }

    private fun getFirstSign(value: String): String {
        return if (Character.codePointCount(value, 0, value.length) == 1) {
            value
        } else {
            getFirstSign(value.substring(0, value.length - 1))
        }
    }
}

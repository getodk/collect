package org.odk.collect.android.application.initialization

import android.content.Context
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.preferences.FormUpdateMode
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.projects.ProjectsRepository

class UserPropertiesInitializer(
    private val analytics: Analytics,
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider,
    private val context: Context
) {

    fun initialize() {
        val projects = projectsRepository.getAll()

        analytics.setUserProperty("ProjectsCount", projects.size.toString())
        analytics.setUserProperty(
            "UsingLegacyFormUpdate",
            projects.any {
                val settings = settingsProvider.getGeneralSettings(it.uuid)
                val formUpdateMode = settings.getString(ProjectKeys.KEY_FORM_UPDATE_MODE)
                formUpdateMode != FormUpdateMode.MATCH_EXACTLY.getValue(context)
            }.toString()
        )
    }
}

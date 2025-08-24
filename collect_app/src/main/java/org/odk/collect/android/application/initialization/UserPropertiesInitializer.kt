package org.odk.collect.android.application.initialization

import android.content.Context
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.instancemanagement.autosend.shouldFormBeSentAutomatically
import org.odk.collect.android.preferences.Defaults
import org.odk.collect.async.Scheduler
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectDependencyFactory
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.enums.AutoSend
import org.odk.collect.settings.enums.FormUpdateMode
import org.odk.collect.settings.enums.StringIdEnumUtils.getAutoSend
import org.odk.collect.settings.keys.ProjectKeys
import kotlin.time.Duration.Companion.days

class UserPropertiesInitializer(
    private val analytics: Analytics,
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider,
    private val context: Context,
    private val scheduler: Scheduler,
    private val instancesRepositoryProvider: ProjectDependencyFactory<InstancesRepository>,
    private val formsRepositoryProvider: ProjectDependencyFactory<FormsRepository>
) {

    fun initialize() {
        scheduler.immediate {
            val projects = projectsRepository.getAll()

            analytics.setUserProperty("ProjectsCount", projects.size.toString())

            analytics.setUserProperty(
                "UsingLegacyFormUpdate",
                projects.any { isNotUsingMatchExactly(it, context) }.toString()
            )

            analytics.setUserProperty(
                "UsingNonDefaultTheme",
                projects.any { isNotUsingDefaultTheme(it) }.toString()
            )

            analytics.setUserProperty(
                "HasUnsentAutosendForms",
                projects.any { hasUnsentAutoSendForms(it) }.toString()
            )
        }
    }

    private fun isNotUsingMatchExactly(project: Project.Saved, context: Context): Boolean {
        val settings = settingsProvider.getUnprotectedSettings(project.uuid)
        val serverUrl = settings.getString(ProjectKeys.KEY_SERVER_URL)
        val formUpdateMode = settings.getString(ProjectKeys.KEY_FORM_UPDATE_MODE)

        val notUsingDefaultServer = serverUrl != Defaults.unprotected[ProjectKeys.KEY_SERVER_URL]
        val notUsingMatchExactly = formUpdateMode != FormUpdateMode.MATCH_EXACTLY.getValue(context)

        return notUsingDefaultServer && notUsingMatchExactly
    }

    private fun isNotUsingDefaultTheme(project: Project.Saved): Boolean {
        val settings = settingsProvider.getUnprotectedSettings(project.uuid)
        val theme = settings.getString(ProjectKeys.KEY_APP_THEME)
        return theme != Defaults.unprotected[ProjectKeys.KEY_APP_THEME]
    }

    private fun hasUnsentAutoSendForms(project: Project.Saved): Boolean {
        val instancesRepository = instancesRepositoryProvider.create(project.uuid)
        val formsRepository = formsRepositoryProvider.create(project.uuid)
        val finalizedForms = instancesRepository.getAllByStatus(Instance.STATUS_COMPLETE)

        return finalizedForms.any {
            val form =
                formsRepository.getLatestByFormIdAndVersion(it.formId, it.formVersion)

            if (form != null) {
                val isAutoSendEnabled = when (settingsProvider.getUnprotectedSettings().getAutoSend(context)) {
                    AutoSend.OFF -> false
                    AutoSend.WIFI_ONLY, AutoSend.CELLULAR_ONLY, AutoSend.WIFI_AND_CELLULAR -> true
                }

                val formShouldAutosend = form.shouldFormBeSentAutomatically(isAutoSendEnabled)
                val timeSinceFinalize = System.currentTimeMillis() - it.lastStatusChangeDate
                formShouldAutosend && timeSinceFinalize > 3.days.inWholeMilliseconds
            } else {
                false
            }
        }
    }
}

package org.odk.collect.android.application

import org.odk.collect.analytics.Analytics
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.backgroundwork.FormUpdateScheduler
import org.odk.collect.android.formmanagement.FormsDataService
import org.odk.collect.metadata.PropertyManager
import org.odk.collect.settings.importing.SettingsChangeHandler
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.strings.Md5.getMd5Hash
import java.io.ByteArrayInputStream

class CollectSettingsChangeHandler(
    private val propertyManager: PropertyManager,
    private val formUpdateScheduler: FormUpdateScheduler,
    private val formsDataService: FormsDataService
) : SettingsChangeHandler {

    override fun onSettingChanged(projectId: String, newValue: Any?, changedKey: String) {
        propertyManager.reload()

        if (changedKey == ProjectKeys.KEY_SERVER_URL) {
            formsDataService.clear(projectId)
        }

        if (changedKey == ProjectKeys.KEY_FORM_UPDATE_MODE ||
            changedKey == ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK
        ) {
            formUpdateScheduler.scheduleUpdates(projectId)
        }

        if (changedKey == ProjectKeys.KEY_SERVER_URL) {
            logServerConfiguration(newValue.toString())
        }
    }

    override fun onSettingsChanged(
        projectId: String,
        changedUnprotectedKeys: List<String>,
        changedProtectedKeys: List<String>
    ) {
        propertyManager.reload()
        if (changedUnprotectedKeys.contains(ProjectKeys.KEY_FORM_UPDATE_MODE) || changedUnprotectedKeys.contains(ProjectKeys.KEY_PERIODIC_FORM_UPDATES_CHECK)) {
            formUpdateScheduler.scheduleUpdates(projectId)
        }
    }

    fun logServerConfiguration(url: String) {
        val upperCaseURL = url.uppercase()
        val scheme = upperCaseURL.split(":".toRegex()).toTypedArray()[0]
        val urlHash = getMd5Hash(ByteArrayInputStream(url.toByteArray()))

        Analytics.log(
            AnalyticsEvents.SET_SERVER,
            mapOf(
                "action" to scheme + " " + getHostFromUrl(url),
                "label" to (urlHash ?: "")
            )
        )
    }

    private fun getHostFromUrl(url: String?): String {
        if (url == null || url.isEmpty()) {
            return ""
        }
        val upperCaseURL = url.uppercase()
        var host = "Other"
        if (upperCaseURL.contains("APPSPOT")) {
            host = "Appspot"
        } else if (upperCaseURL.contains("KOBOTOOLBOX.ORG") || upperCaseURL.contains("HUMANITARIANRESPONSE.INFO")) {
            host = "Kobo"
        } else if (upperCaseURL.contains("ONA.IO")) {
            host = "Ona"
        } else if (upperCaseURL.contains("GETODK.CLOUD")) {
            host = "ODK Cloud"
        }
        return host
    }
}

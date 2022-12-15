package org.odk.collect.android.analytics

import org.odk.collect.analytics.Analytics
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings
import org.odk.collect.shared.strings.Md5.getMd5Hash
import java.io.ByteArrayInputStream

object AnalyticsUtils {
    @JvmStatic
    fun setForm(formController: FormController) {
        Analytics.setParam("form", getFormHash(formController))
    }

    @JvmStatic
    fun logFormEvent(event: String, formId: String?, formTitle: String?) {
        Analytics.log(event, "form", getFormHash(formId, formTitle))
    }

    @JvmStatic
    fun logServerEvent(event: String, generalSettings: Settings) {
        Analytics.log(event, "server", getServerHash(generalSettings)!!)
    }

    fun logServerConfiguration(url: String) {
        val upperCaseURL = url.uppercase()
        val scheme = upperCaseURL.split(":".toRegex()).toTypedArray()[0]
        val urlHash = getMd5Hash(ByteArrayInputStream(url.toByteArray()))
        Analytics.log(AnalyticsEvents.SET_SERVER, scheme + " " + getHostFromUrl(url), urlHash!!)
    }

    @JvmStatic
    fun logInvalidFormHash(url: String?) {
        Analytics.log(AnalyticsEvents.INVALID_FORM_HASH, "host", getHostFromUrl(url))
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

    private fun getFormHash(formController: FormController?): String {
        return if (formController != null) {
            val formID = formController.getFormDef()?.mainInstance?.root?.getAttributeValue("", "id") ?: ""
            getFormHash(formID, formController.getFormTitle())
        } else {
            ""
        }
    }

    private fun getFormHash(formId: String?, formTitle: String?): String {
        return getMd5Hash(ByteArrayInputStream("$formTitle $formId".toByteArray())) ?: ""
    }

    private fun getServerHash(generalSettings: Settings): String? {
        val currentServerUrl = generalSettings.getString(ProjectKeys.KEY_SERVER_URL)
        return getMd5Hash(ByteArrayInputStream(currentServerUrl?.toByteArray()))
    }
}

package org.odk.collect.android.formmanagement

import org.odk.collect.analytics.Analytics
import org.odk.collect.android.openrosa.OpenRosaFormSource
import org.odk.collect.android.openrosa.OpenRosaHttpInterface
import org.odk.collect.android.openrosa.OpenRosaResponseParserImpl
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.utilities.WebCredentialsUtils
import org.odk.collect.forms.FormSource

class FormSourceProvider(
    private val settingsProvider: SettingsProvider,
    private val openRosaHttpInterface: OpenRosaHttpInterface,
    private val analytics: Analytics
) {

    @JvmOverloads
    fun get(projectId: String? = null): FormSource {
        val generalSettings = settingsProvider.getGeneralSettings(projectId)

        val serverURL = generalSettings.getString(GeneralKeys.KEY_SERVER_URL)
        val formListPath = generalSettings.getString(GeneralKeys.KEY_FORMLIST_URL)

        return OpenRosaFormSource(
            serverURL,
            formListPath,
            openRosaHttpInterface,
            WebCredentialsUtils(generalSettings),
            analytics,
            OpenRosaResponseParserImpl()
        )
    }
}

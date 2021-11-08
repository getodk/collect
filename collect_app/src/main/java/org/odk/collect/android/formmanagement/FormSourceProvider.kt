package org.odk.collect.android.formmanagement

import org.odk.collect.android.openrosa.OpenRosaFormSource
import org.odk.collect.android.openrosa.OpenRosaHttpInterface
import org.odk.collect.android.openrosa.OpenRosaResponseParserImpl
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.utilities.WebCredentialsUtils
import org.odk.collect.forms.FormSource

class FormSourceProvider(
    private val settingsProvider: SettingsProvider,
    private val openRosaHttpInterface: OpenRosaHttpInterface
) {

    @JvmOverloads
    fun get(projectId: String? = null): FormSource {
        val generalSettings = settingsProvider.getUnprotectedSettings(projectId)

        val serverURL = generalSettings.getString(ProjectKeys.KEY_SERVER_URL)

        return OpenRosaFormSource(
            serverURL,
            openRosaHttpInterface,
            WebCredentialsUtils(generalSettings),
            OpenRosaResponseParserImpl()
        )
    }
}

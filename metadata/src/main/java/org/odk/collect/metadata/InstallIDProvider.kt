package org.odk.collect.metadata

import org.odk.collect.shared.settings.Settings
import org.odk.collect.shared.strings.RandomString

interface InstallIDProvider {
    val installID: String
}

class SharedPreferencesInstallIDProvider(
    private val metaPreferences: Settings,
    private val preferencesKey: String
) : InstallIDProvider {

    override val installID: String
        get() {
            return if (metaPreferences.contains(preferencesKey)) {
                metaPreferences.getString(preferencesKey) ?: generateAndStoreInstallID()
            } else {
                generateAndStoreInstallID()
            }
        }

    private fun generateAndStoreInstallID(): String {
        val installID = "collect:" + RandomString.randomString(16)
        metaPreferences.save(preferencesKey, installID)
        return installID
    }
}

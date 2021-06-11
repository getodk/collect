package org.odk.collect.android.utilities

import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.shared.Settings
import java.io.File

class AppStateProvider(
    private val currentVersion: Int,
    private val metaSettings: Settings,
    private val externalFilesDir: File
) {

    fun isUpgradedFirstLaunch(): Boolean {
        return if (metaSettings.contains(MetaKeys.LAST_LAUNCHED)) {
            metaSettings.getInt(MetaKeys.LAST_LAUNCHED) < currentVersion
        } else {
            val legacyMetadataDir = File(externalFilesDir, "metadata")
            return FileUtils.listFiles(legacyMetadataDir).isNotEmpty()
        }
    }

    fun appLaunched() {
        metaSettings.save(MetaKeys.LAST_LAUNCHED, currentVersion)
    }
}

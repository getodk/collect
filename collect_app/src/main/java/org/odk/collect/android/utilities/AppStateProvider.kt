package org.odk.collect.android.utilities

import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.shared.Settings

class AppStateProvider(
    private val currentVersion: Int,
    private val metaSettings: Settings
) {

    fun isUpgradedFirstLaunch(): Boolean {
        return if (metaSettings.contains(MetaKeys.LAST_LAUNCHED)) {
            metaSettings.getInt(MetaKeys.LAST_LAUNCHED) < currentVersion
        } else {
            false
        }
    }

    fun appLaunched() {
        metaSettings.save(MetaKeys.LAST_LAUNCHED, currentVersion)
    }
}

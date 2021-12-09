package org.odk.collect.android.utilities

import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.shared.Settings

class LaunchState(
    private val metaSettings: Settings,
    private val currentVersion: Int,
    private val installDetector: InstallDetector? = null
) {

    fun isUpgradedFirstLaunch(): Boolean {
        return if (metaSettings.contains(MetaKeys.LAST_LAUNCHED)) {
            metaSettings.getInt(MetaKeys.LAST_LAUNCHED) < currentVersion
        } else {
            return installDetector?.installDetected() ?: false
        }
    }

    fun appLaunched() {
        metaSettings.save(MetaKeys.LAST_LAUNCHED, currentVersion)
    }

    interface InstallDetector {
        fun installDetected(): Boolean
    }
}

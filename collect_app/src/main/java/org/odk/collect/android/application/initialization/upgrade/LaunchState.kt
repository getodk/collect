package org.odk.collect.android.utilities

import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.shared.Settings

interface LaunchState {
    fun isUpgradedFirstLaunch(): Boolean
    fun appLaunched()
}

class VersionCodeLaunchState(
    private val metaSettings: Settings,
    private val currentVersion: Int,
    private val installDetector: InstallDetector? = null
) : LaunchState {

    override fun isUpgradedFirstLaunch(): Boolean {
        return if (metaSettings.contains(MetaKeys.LAST_LAUNCHED)) {
            metaSettings.getInt(MetaKeys.LAST_LAUNCHED) < currentVersion
        } else {
            return installDetector?.installDetected() ?: false
        }
    }

    override fun appLaunched() {
        metaSettings.save(MetaKeys.LAST_LAUNCHED, currentVersion)
    }
}

interface InstallDetector {
    fun installDetected(): Boolean
}

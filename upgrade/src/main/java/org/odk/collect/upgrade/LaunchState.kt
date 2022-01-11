package org.odk.collect.upgrade

import org.odk.collect.shared.Settings

internal interface LaunchState {
    fun isUpgradedFirstLaunch(): Boolean
    fun appLaunched()
}

internal class VersionCodeLaunchState(
    private val key: String,
    private val settings: Settings,
    private val currentVersion: Int,
    private val installDetector: InstallDetector
) : LaunchState {

    override fun isUpgradedFirstLaunch(): Boolean {
        return if (settings.contains(key)) {
            settings.getInt(key) < currentVersion
        } else {
            return installDetector.installDetected()
        }
    }

    override fun appLaunched() {
        settings.save(key, currentVersion)
    }
}

interface InstallDetector {
    fun installDetected(): Boolean
}

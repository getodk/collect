package org.odk.collect.android.application.initialization.upgrade

import org.odk.collect.android.BuildConfig
import org.odk.collect.shared.Settings

class AppUpgrader(
    private val settings: Settings,
    private val launchState: LaunchState,
    private val upgrades: List<Upgrade>
) {

    constructor(key: String, settings: Settings, installDetector: InstallDetector, upgrades: List<Upgrade>) : this(
        settings,
        VersionCodeLaunchState(key, settings, BuildConfig.VERSION_CODE, installDetector),
        upgrades
    )

    fun upgradeIfNeeded() {
        if (launchState.isUpgradedFirstLaunch()) {
            upgrades.forEach {
                val key = it.key()

                if (key == null) {
                    it.run()
                } else if (!settings.getBoolean(key)) {
                    it.run()
                    settings.save(key, true)
                }
            }
        } else {
            upgrades.forEach {
                it.key()?.let { key ->
                    settings.save(key, true)
                }
            }
        }

        launchState.appLaunched()
    }
}

interface Upgrade {

    fun key(): String?

    fun run()
}

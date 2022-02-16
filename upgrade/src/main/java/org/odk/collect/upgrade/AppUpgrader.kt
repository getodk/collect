package org.odk.collect.upgrade

import android.app.Application
import org.odk.collect.shared.settings.Settings

class AppUpgrader internal constructor(
    private val settings: Settings,
    private val launchState: LaunchState,
    private val upgrades: List<Upgrade>
) {
    constructor(
        key: String,
        settings: Settings,
        versionCode: Int,
        installDetector: InstallDetector,
        upgrades: List<Upgrade>
    ) : this(
        settings,
        VersionCodeLaunchState(key, settings, versionCode, installDetector),
        upgrades
    )

    /**
     * Runs the list of passed [Upgrade] implementations in order if this is the first launch of a
     * new version of the app (an "app upgrade"). This should be called in (or from somewhere called
     * from) [Application.onCreate].
     *
     * [Upgrade]s that return non-null values from [Upgrade.key] will only be run if they have not
     * been run before.
     */
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

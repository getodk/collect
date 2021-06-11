package org.odk.collect.android.utilities

import android.content.pm.PackageInfo
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.shared.Settings

class AppStateProvider(private val packageInfo: PackageInfo, private val metaSettings: Settings) {

    fun isFirstEverLaunch(): Boolean {
        return !isUpdatedVersion() && !metaSettings.contains(MetaKeys.FIRST_LAUNCH)
    }

    fun isUpgradedFirstLaunch(): Boolean {
        return false
    }

    private fun isUpdatedVersion(): Boolean {
        return if (alwaysFresh) {
            false
        } else {
            packageInfo.firstInstallTime != packageInfo.lastUpdateTime
        }
    }

    companion object {
        @JvmField
        var alwaysFresh: Boolean = false
    }
}

package org.odk.collect.android.utilities

import android.content.pm.PackageInfo
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.shared.Settings

class AppStateProvider(private val packageInfo: PackageInfo, private val metaSettings: Settings) {
    fun isFreshInstall(): Boolean {
        return if (alwaysFresh) {
            true
        } else {
            !isUpdatedVersion() && !metaSettings.contains(MetaKeys.FIRST_LAUNCH)
        }
    }

    private fun isUpdatedVersion(): Boolean {
        return packageInfo.firstInstallTime != packageInfo.lastUpdateTime
    }

    companion object {
        @JvmField
        var alwaysFresh: Boolean = false
    }
}

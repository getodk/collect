package org.odk.collect.android.utilities

import android.content.Context
import androidx.preference.PreferenceManager
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.shared.Settings
import java.io.File

class LaunchState(
    private val context: Context,
    private val metaSettings: Settings,
    private val currentVersion: Int
) {

    fun isUpgradedFirstLaunch(): Boolean {
        return if (metaSettings.contains(MetaKeys.LAST_LAUNCHED)) {
            metaSettings.getInt(MetaKeys.LAST_LAUNCHED) < currentVersion
        } else {
            return legacyInstallDetected()
        }
    }

    // Check if it looks like is an install from before when `LAST_LAUNCHED` was introduced
    private fun legacyInstallDetected(): Boolean {
        val legacyMetadataDir = File(context.getExternalFilesDir(null), "metadata")
        val hasLegacyMetadata = FileUtils.listFiles(legacyMetadataDir).isNotEmpty()

        val hasLegacyGeneralPrefs =
            PreferenceManager.getDefaultSharedPreferences(context).all.isNotEmpty()
        val hasLegacyAdminPrefs =
            context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE).all.isNotEmpty()

        return hasLegacyMetadata || hasLegacyGeneralPrefs || hasLegacyAdminPrefs
    }

    fun appLaunched() {
        metaSettings.save(MetaKeys.LAST_LAUNCHED, currentVersion)
    }
}

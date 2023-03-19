package org.odk.collect.android.application.initialization.upgrade

import android.content.Context
import androidx.preference.PreferenceManager
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.upgrade.AppUpgrader
import org.odk.collect.upgrade.InstallDetector
import java.io.File

/**
 * Implementation of [InstallDetector] that looks for signs that a version of Collect
 * is installed from before Projects were introduced (< v2021.2). [AppUpgrader] was
 * introduced in that release as well so it and versions after it can use [AppUpgrader]'s
 * built in version tracking.
 */
class BeforeProjectsInstallDetector(private val context: Context) : InstallDetector {

    override fun installDetected(): Boolean {
        val legacyMetadataDir = File(context.getExternalFilesDir(null), "metadata")
        val hasLegacyMetadata = FileUtils.listFiles(legacyMetadataDir).isNotEmpty()

        val hasLegacyGeneralPrefs =
            PreferenceManager.getDefaultSharedPreferences(context).all.isNotEmpty()
        val hasLegacyAdminPrefs =
            context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE).all.isNotEmpty()

        return hasLegacyMetadata || hasLegacyGeneralPrefs || hasLegacyAdminPrefs
    }
}

package org.odk.collect.android.application.initialization.upgrade

import android.content.Context
import org.odk.collect.android.BuildConfig
import org.odk.collect.android.application.initialization.ExistingProjectMigrator
import org.odk.collect.android.application.initialization.ExistingSettingsMigrator
import org.odk.collect.android.application.initialization.FormUpdatesUpgrade
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.upgrade.AppUpgrader

class UpgradeInitializer(
    private val context: Context,
    private val settingsProvider: SettingsProvider,
    private val existingProjectMigrator: ExistingProjectMigrator,
    private val existingSettingsMigrator: ExistingSettingsMigrator,
    private val formUpdatesUpgrade: FormUpdatesUpgrade
) {

    fun initialize() {
        AppUpgrader(
            MetaKeys.LAST_LAUNCHED,
            settingsProvider.getMetaSettings(),
            BuildConfig.VERSION_CODE,
            BeforeProjectsInstallDetector(context),
            listOf(
                existingProjectMigrator,
                existingSettingsMigrator,
                formUpdatesUpgrade
            )
        ).upgradeIfNeeded()
    }
}

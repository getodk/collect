package org.odk.collect.android.application.initialization

import org.odk.collect.android.application.initialization.upgrade.Upgrade
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.projects.ProjectsRepository

class ExistingSettingsMigrator(
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider,
    private val settingsMigrator: SettingsMigrator
) : Upgrade {

    override fun key(): String? {
        return null
    }

    override fun run() {
        projectsRepository.getAll().forEach {
            settingsMigrator.migrate(
                settingsProvider.getGeneralSettings(it.uuid),
                settingsProvider.getAdminSettings(it.uuid)
            )
        }
    }
}

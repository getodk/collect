package org.odk.collect.mobiledevicemanagement

import android.os.Bundle
import org.odk.collect.analytics.Analytics
import org.odk.collect.projects.ProjectCreator
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.projects.SettingsConnectionMatcher
import org.odk.collect.settings.ODKAppSettingsImporter
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.MetaKeys.KEY_INSTALL_ID

const val DEVICE_ID_KEY = "device_id"
const val SETTINGS_JSON_KEY = "settings_json"

interface MDMConfigHandler {
    fun applyConfig(managedConfig: Bundle)
}

class MDMConfigHandlerImpl(
    private val settingsProvider: SettingsProvider,
    private val projectsRepository: ProjectsRepository,
    private val projectCreator: ProjectCreator,
    private val settingsImporter: ODKAppSettingsImporter,
    private val settingsConnectionMatcher: SettingsConnectionMatcher
) : MDMConfigHandler {
    override fun applyConfig(managedConfig: Bundle) {
        Analytics.setUserProperty("SawMDMConfig", "true")
        applyDeviceId(managedConfig)
        applySettingsJson(managedConfig)
    }

    private fun applyDeviceId(managedConfig: Bundle) {
        if (managedConfig.containsKey(DEVICE_ID_KEY) && !managedConfig.getString(DEVICE_ID_KEY).isNullOrBlank()) {
            settingsProvider.getMetaSettings().save(
                KEY_INSTALL_ID, managedConfig.getString(
                    DEVICE_ID_KEY
                ))
        }
    }

    private fun applySettingsJson(managedConfig: Bundle) {
        if (managedConfig.containsKey(SETTINGS_JSON_KEY) && !managedConfig.getString(SETTINGS_JSON_KEY).isNullOrBlank()) {
            val settingsJson = managedConfig.getString(SETTINGS_JSON_KEY)

            if (!settingsJson.isNullOrBlank()) {
                when (val matchingProjectUUID = settingsJson.let { settingsConnectionMatcher.getProjectWithMatchingConnection(it) }) {
                    null -> projectCreator.createNewProject(settingsJson, projectsRepository.getAll().isEmpty())
                    else -> settingsImporter.fromJSON(settingsJson, projectsRepository.get(matchingProjectUUID)!!)
                }
            }
        }
    }
}

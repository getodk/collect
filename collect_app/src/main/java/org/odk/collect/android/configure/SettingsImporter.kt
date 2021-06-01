package org.odk.collect.android.configure

import org.json.JSONException
import org.json.JSONObject
import org.odk.collect.android.application.initialization.SettingsPreferenceMigrator
import org.odk.collect.shared.Settings

class SettingsImporter(
    private val generalSettings: Settings,
    private val adminSettings: Settings,
    private val preferenceMigrator: SettingsPreferenceMigrator,
    private val settingsValidator: SettingsValidator,
    private val generalDefaults: Map<String, Any>,
    private val adminDefaults: Map<String, Any>,
    private val settingsChangedHandler: SettingsChangeHandler
) {
    fun fromJSON(json: String): Boolean {
        if (!settingsValidator.isValid(json)) {
            return false
        }
        generalSettings.clear()
        adminSettings.clear()

        try {
            val jsonObject = JSONObject(json)
            val general = jsonObject.getJSONObject("general")
            importToPrefs(general, generalSettings)
            val admin = jsonObject.getJSONObject("admin")
            importToPrefs(admin, adminSettings)
        } catch (ignored: JSONException) {
            // Ignored
        }

        preferenceMigrator.migrate(generalSettings, adminSettings)

        clearUnknownKeys(generalSettings, generalDefaults)
        clearUnknownKeys(adminSettings, adminDefaults)
        loadDefaults(generalSettings, generalDefaults)
        loadDefaults(adminSettings, adminDefaults)

        for ((key, value) in generalSettings.getAll()) {
            settingsChangedHandler.onSettingChanged(key, value)
        }
        for ((key, value) in adminSettings.getAll()) {
            settingsChangedHandler.onSettingChanged(key, value)
        }
        return true
    }

    private fun importToPrefs(`object`: JSONObject, preferences: Settings) {
        val generalKeys = `object`.keys()
        while (generalKeys.hasNext()) {
            val key = generalKeys.next()
            preferences.save(key, `object`[key])
        }
    }

    private fun loadDefaults(preferences: Settings, defaults: Map<String, Any>) {
        for ((key, value) in defaults) {
            if (!preferences.contains(key)) {
                preferences.save(key, value)
            }
        }
    }

    private fun clearUnknownKeys(preferences: Settings, defaults: Map<String, Any>) {
        for (key in preferences.getAll().keys) {
            if (!defaults.containsKey(key)) {
                preferences.remove(key)
            }
        }
    }
}

package org.odk.collect.android.configure.qr

import org.json.JSONObject
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.source.SettingsProvider

class JsonPreferencesGenerator(private val settingsProvider: SettingsProvider) {

    fun getJSONFromPreferences(includedPasswordKeys: Collection<String> = emptyList()): String {
        return JSONObject().apply {
            put("general", getGeneralPrefsAsJson(includedPasswordKeys))
            put("admin", getAdminPrefsAsJson(includedPasswordKeys))
        }.toString()
    }

    private fun getGeneralPrefsAsJson(includedPasswordKeys: Collection<String>): JSONObject {
        val generalPrefs = JSONObject()

        val generalSettings = settingsProvider.getGeneralSettings().getAll()
        val defaultGeneralSettings: Map<String, *> = GeneralKeys.getDefaults()

        for (key in defaultGeneralSettings.keys) {
            if (key == GeneralKeys.KEY_PASSWORD && !includedPasswordKeys.contains(GeneralKeys.KEY_PASSWORD)) {
                continue
            }
            val value = generalSettings[key]
            if (value != null && value != defaultGeneralSettings[key]) {
                generalPrefs.put(key, value)
            }
        }
        return generalPrefs
    }

    private fun getAdminPrefsAsJson(includedPasswordKeys: Collection<String>): JSONObject {
        val adminPrefs = JSONObject()

        val adminSettings = settingsProvider.getAdminSettings().getAll()
        val defaultAdminSettings = AdminKeys.getDefaults()

        for (key in AdminKeys.ALL_KEYS) {
            if (key == AdminKeys.KEY_ADMIN_PW && !includedPasswordKeys.contains(AdminKeys.KEY_ADMIN_PW)) {
                continue
            }
            val value = adminSettings[key]
            if (value != null && value != defaultAdminSettings[key]) {
                adminPrefs.put(key, value)
            }
        }
        return adminPrefs
    }
}

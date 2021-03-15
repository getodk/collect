package org.odk.collect.android.preferences.source

import android.content.Context
import androidx.preference.PreferenceManager
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.keys.GeneralKeys

class SettingsProvider(private val context: Context) {
    fun getMetaSettings(): Settings {
        return settings.getOrPut(META_SETTINGS_NAME) {
            SharedPreferencesSettings(context.getSharedPreferences(META_SETTINGS_NAME, Context.MODE_PRIVATE))
        }
    }

    @JvmOverloads
    fun getGeneralSettings(projectId: String = ""): Settings {
        val preferenceId = GENERAL_SETTINGS_NAME + projectId

        return settings.getOrPut(preferenceId) {
            if (projectId.isBlank()) {
                SharedPreferencesSettings(PreferenceManager.getDefaultSharedPreferences(context), GeneralKeys.DEFAULTS)
            } else {
                SharedPreferencesSettings(context.getSharedPreferences(preferenceId, Context.MODE_PRIVATE), GeneralKeys.DEFAULTS)
            }
        }
    }

    @JvmOverloads
    fun getAdminSettings(projectId: String = ""): Settings {
        val preferenceId = ADMIN_SETTINGS_NAME + projectId

        return settings.getOrPut(preferenceId) {
            SharedPreferencesSettings(context.getSharedPreferences(preferenceId, Context.MODE_PRIVATE), AdminKeys.getDefaults())
        }
    }

    companion object {
        private val settings = mutableMapOf<String, Settings>()

        private const val META_SETTINGS_NAME = "meta"
        private const val GENERAL_SETTINGS_NAME = "general_prefs"
        private const val ADMIN_SETTINGS_NAME = "admin_prefs"
    }
}

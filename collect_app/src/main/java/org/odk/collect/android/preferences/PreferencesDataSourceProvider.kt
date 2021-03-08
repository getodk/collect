package org.odk.collect.android.preferences

import android.content.Context
import androidx.preference.PreferenceManager

class PreferencesDataSourceProvider(private val context: Context) {
    fun getMetaPreferences(): PreferencesDataSource {
        return preferences.getOrPut(META_PREFS_NAME) {
            SharedPreferencesDataSource(context.getSharedPreferences(META_PREFS_NAME, Context.MODE_PRIVATE))
        }
    }

    @JvmOverloads
    fun getGeneralPreferences(projectId: String = ""): PreferencesDataSource {
        val preferenceId = GENERAL_PREFS_NAME + projectId

        return preferences.getOrPut(preferenceId) {
            if (projectId.isBlank()) {
                SharedPreferencesDataSource(PreferenceManager.getDefaultSharedPreferences(context), GeneralKeys.DEFAULTS)
            } else {
                SharedPreferencesDataSource(context.getSharedPreferences(preferenceId, Context.MODE_PRIVATE), GeneralKeys.DEFAULTS)
            }
        }
    }

    @JvmOverloads
    fun getAdminPreferences(projectId: String = ""): PreferencesDataSource {
        val preferenceId = ADMIN_PREFS_NAME + projectId

        return preferences.getOrPut(preferenceId) {
            SharedPreferencesDataSource(context.getSharedPreferences(preferenceId, Context.MODE_PRIVATE), AdminKeys.getDefaults())
        }
    }

    companion object {
        private val preferences = mutableMapOf<String, PreferencesDataSource>()

        private const val META_PREFS_NAME = "meta"
        private const val GENERAL_PREFS_NAME = "general_prefs"
        private const val ADMIN_PREFS_NAME = "admin_prefs"
    }
}

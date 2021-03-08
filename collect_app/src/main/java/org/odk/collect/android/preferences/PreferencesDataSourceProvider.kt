package org.odk.collect.android.preferences

import android.content.Context
import androidx.preference.PreferenceManager

class PreferencesDataSourceProvider(private val context: Context) {
    private val preferences = mutableMapOf<String, PreferencesDataSource>()

    fun getMetaPreferences(): PreferencesDataSource {
        return preferences.getOrPut(META_PREFS_NAME) {
            PreferencesDataSource(context.getSharedPreferences(META_PREFS_NAME, Context.MODE_PRIVATE))
        }
    }

    @JvmOverloads
    fun getGeneralPreferences(projectId: String = ""): PreferencesDataSource {
        val preferenceId = GENERAL_PREFS_NAME + projectId

        return preferences.getOrPut(preferenceId) {
            if (projectId.isBlank()) {
                PreferencesDataSource(PreferenceManager.getDefaultSharedPreferences(context), GeneralKeys.DEFAULTS)
            } else {
                PreferencesDataSource(context.getSharedPreferences(preferenceId, Context.MODE_PRIVATE), GeneralKeys.DEFAULTS)
            }
        }
    }

    @JvmOverloads
    fun getAdminPreferences(projectId: String = ""): PreferencesDataSource {
        val preferenceId = ADMIN_PREFS_NAME + projectId

        return preferences.getOrPut(preferenceId) {
            PreferencesDataSource(context.getSharedPreferences(preferenceId, Context.MODE_PRIVATE), AdminKeys.getDefaults())
        }
    }

    companion object {
        private const val META_PREFS_NAME = "meta"
        private const val GENERAL_PREFS_NAME = "general_prefs"
        private const val ADMIN_PREFS_NAME = "admin_prefs"
    }
}

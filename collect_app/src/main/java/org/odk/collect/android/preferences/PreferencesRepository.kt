package org.odk.collect.android.preferences

import android.content.Context
import androidx.preference.PreferenceManager

class PreferencesRepository(private val context: Context) {
    private val preferences = mutableMapOf<String, PreferencesDataSource>()

    @JvmOverloads
    fun getMetaPreferences(projectId: String = ""): PreferencesDataSource {
        val preferenceId = META_PREFS_NAME + projectId

        if (!preferences.containsKey(preferenceId)) {
            preferences[META_PREFS_NAME + projectId] = PreferencesDataSource(context.getSharedPreferences(preferenceId, Context.MODE_PRIVATE))
        }
        return preferences[preferenceId]!!
    }

    @JvmOverloads
    fun getGeneralPreferences(projectId: String = ""): PreferencesDataSource {
        val preferenceId = GENERAL_PREFS_NAME + projectId

        if (!preferences.containsKey(preferenceId)) {
            if (projectId.isBlank()) {
                preferences[preferenceId] = PreferencesDataSource(PreferenceManager.getDefaultSharedPreferences(context), GeneralKeys.DEFAULTS)
            } else {
                preferences[preferenceId] = PreferencesDataSource(context.getSharedPreferences(preferenceId, Context.MODE_PRIVATE), GeneralKeys.DEFAULTS)
            }
        }
        return preferences[preferenceId]!!
    }

    @JvmOverloads
    fun getAdminPreferences(projectId: String = ""): PreferencesDataSource {
        val preferenceId = ADMIN_PREFS_NAME + projectId

        if (!preferences.containsKey(preferenceId)) {
            preferences[preferenceId] = PreferencesDataSource(context.getSharedPreferences(preferenceId, Context.MODE_PRIVATE), AdminKeys.getDefaults())
        }
        return preferences[preferenceId]!!
    }

    // Just for tests
    fun getTestPreferences(name: String): PreferencesDataSource {
        return PreferencesDataSource(context.getSharedPreferences(name, Context.MODE_PRIVATE))
    }

    companion object {
        private const val META_PREFS_NAME = "meta"
        private const val GENERAL_PREFS_NAME = "general_prefs"
        private const val ADMIN_PREFS_NAME = "admin_prefs"
    }
}

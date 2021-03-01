package org.odk.collect.android.preferences

import android.content.Context
import androidx.preference.PreferenceManager

open class PreferencesRepository(private val context: Context) {
    fun getMetaPreferences(): PreferencesDataSource {
        return PreferencesDataSource(context.getSharedPreferences(META_PREFS_NAME, Context.MODE_PRIVATE))
    }

    fun getGeneralPreferences(): PreferencesDataSource {
        return PreferencesDataSource(PreferenceManager.getDefaultSharedPreferences(context), GeneralKeys.DEFAULTS)
    }

    fun getAdminPreferences(): PreferencesDataSource {
        return PreferencesDataSource(context.getSharedPreferences(ADMIN_PREFS_NAME, Context.MODE_PRIVATE), AdminKeys.getDefaults())
    }

    // Just for tests
    fun getTestPreferences(name: String): PreferencesDataSource {
        return PreferencesDataSource(context.getSharedPreferences(name, Context.MODE_PRIVATE))
    }

    companion object {
        private const val META_PREFS_NAME = "meta"
        private const val ADMIN_PREFS_NAME = "admin_prefs"
    }
}

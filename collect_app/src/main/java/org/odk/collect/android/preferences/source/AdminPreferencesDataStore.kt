package org.odk.collect.android.preferences.source

import androidx.preference.PreferenceDataStore

class AdminPreferencesDataStore(val preferencesDataSourceProvider: PreferencesDataSourceProvider) : PreferenceDataStore() {
    override fun putString(key: String, value: String?) {
        preferencesDataSourceProvider.getAdminPreferences().save(key, value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        preferencesDataSourceProvider.getAdminPreferences().save(key, value)
    }

    override fun putLong(key: String, value: Long) {
        preferencesDataSourceProvider.getAdminPreferences().save(key, value)
    }

    override fun putInt(key: String, value: Int) {
        preferencesDataSourceProvider.getAdminPreferences().save(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        preferencesDataSourceProvider.getAdminPreferences().save(key, value)
    }

    override fun putStringSet(key: String, value: Set<String>?) {
        preferencesDataSourceProvider.getAdminPreferences().save(key, value)
    }

    override fun getString(key: String, defValue: String?): String? {
        return preferencesDataSourceProvider.getAdminPreferences().getString(key)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferencesDataSourceProvider.getAdminPreferences().getBoolean(key)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return preferencesDataSourceProvider.getAdminPreferences().getLong(key)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return preferencesDataSourceProvider.getAdminPreferences().getInt(key)
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return preferencesDataSourceProvider.getAdminPreferences().getFloat(key)
    }

    override fun getStringSet(key: String, defValue: Set<String>?): Set<String>? {
        return preferencesDataSourceProvider.getAdminPreferences().getStringSet(key)
    }
}

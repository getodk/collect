package org.odk.collect.android.preferences.source

import androidx.preference.PreferenceDataStore

class GeneralPreferencesDataStore(val preferencesDataSourceProvider: PreferencesDataSourceProvider) : PreferenceDataStore() {
    override fun putString(key: String, value: String?) {
        preferencesDataSourceProvider.getGeneralPreferences().save(key, value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        preferencesDataSourceProvider.getGeneralPreferences().save(key, value)
    }

    override fun putLong(key: String, value: Long) {
        preferencesDataSourceProvider.getGeneralPreferences().save(key, value)
    }

    override fun putInt(key: String, value: Int) {
        preferencesDataSourceProvider.getGeneralPreferences().save(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        preferencesDataSourceProvider.getGeneralPreferences().save(key, value)
    }

    override fun putStringSet(key: String, value: Set<String>?) {
        preferencesDataSourceProvider.getGeneralPreferences().save(key, value)
    }

    override fun getString(key: String, defValue: String?): String? {
        return preferencesDataSourceProvider.getGeneralPreferences().getString(key)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferencesDataSourceProvider.getGeneralPreferences().getBoolean(key)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return preferencesDataSourceProvider.getGeneralPreferences().getLong(key)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return preferencesDataSourceProvider.getGeneralPreferences().getInt(key)
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return preferencesDataSourceProvider.getGeneralPreferences().getFloat(key)
    }

    override fun getStringSet(key: String, defValue: Set<String>?): Set<String>? {
        return preferencesDataSourceProvider.getGeneralPreferences().getStringSet(key)
    }
}

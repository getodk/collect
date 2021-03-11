package org.odk.collect.android.preferences.source

import androidx.preference.PreferenceDataStore

class PreferencesDataStore(private val preferencesDataSource: PreferencesDataSource) : PreferenceDataStore() {
    override fun putString(key: String, value: String?) {
        preferencesDataSource.save(key, value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        preferencesDataSource.save(key, value)
    }

    override fun putLong(key: String, value: Long) {
        preferencesDataSource.save(key, value)
    }

    override fun putInt(key: String, value: Int) {
        preferencesDataSource.save(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        preferencesDataSource.save(key, value)
    }

    override fun putStringSet(key: String, value: Set<String>?) {
        preferencesDataSource.save(key, value)
    }

    override fun getString(key: String, defValue: String?): String? {
        return preferencesDataSource.getString(key)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return preferencesDataSource.getBoolean(key)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return preferencesDataSource.getLong(key)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return preferencesDataSource.getInt(key)
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return preferencesDataSource.getFloat(key)
    }

    override fun getStringSet(key: String, defValue: Set<String>?): Set<String>? {
        return preferencesDataSource.getStringSet(key)
    }
}

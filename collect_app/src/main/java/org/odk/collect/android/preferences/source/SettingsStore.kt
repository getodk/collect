package org.odk.collect.android.preferences.source

import androidx.preference.PreferenceDataStore
import org.odk.collect.shared.settings.Settings

class SettingsStore(private val settings: Settings) : PreferenceDataStore() {
    override fun putString(key: String, value: String?) {
        settings.save(key, value)
    }

    override fun putBoolean(key: String, value: Boolean) {
        settings.save(key, value)
    }

    override fun putLong(key: String, value: Long) {
        settings.save(key, value)
    }

    override fun putInt(key: String, value: Int) {
        settings.save(key, value)
    }

    override fun putFloat(key: String, value: Float) {
        settings.save(key, value)
    }

    override fun putStringSet(key: String, value: Set<String>?) {
        settings.save(key, value)
    }

    override fun getString(key: String, defValue: String?): String? {
        return settings.getString(key)
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return settings.getBoolean(key)
    }

    override fun getLong(key: String, defValue: Long): Long {
        return settings.getLong(key)
    }

    override fun getInt(key: String, defValue: Int): Int {
        return settings.getInt(key)
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return settings.getFloat(key)
    }

    override fun getStringSet(key: String, defValue: Set<String>?): Set<String>? {
        return settings.getStringSet(key)
    }
}

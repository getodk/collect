package org.odk.collect.android.preferences

import android.content.SharedPreferences
import java.util.Collections

class SharedPreferencesDataSource(private val sharedPreferences: SharedPreferences, private val defaultPreferences: Map<String, Any> = emptyMap()) : PreferencesDataSource {
    private lateinit var sharedPreferencesListener: SharedPreferences.OnSharedPreferenceChangeListener

    override fun loadDefaultPreferencesIfNotExist() {
        for ((key, value) in defaultPreferences) {
            if (!sharedPreferences.contains(key)) {
                save(key, value)
            }
        }
    }

    override fun save(key: String, value: Any?) {
        saveAll(Collections.singletonMap(key, value))
    }

    override fun saveAll(prefs: Map<String, Any?>) {
        val editor = sharedPreferences.edit()
        for ((key, value) in prefs) {
            when (value) {
                null, is String -> editor.putString(key, value as String?)
                is Boolean -> editor.putBoolean(key, value)
                is Long -> editor.putLong(key, value)
                is Int -> editor.putInt(key, value)
                is Float -> editor.putFloat(key, value)
                is Set<*> -> editor.putStringSet(key, value as Set<String?>)
                else -> throw RuntimeException("Unhandled preference value type: $value")
            }
        }
        editor.apply()
    }

    override fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    override fun reset(key: String) {
        save(key, defaultPreferences[key])
    }

    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    override fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    override fun getAll(): Map<String, *> {
        return sharedPreferences.all
    }

    override fun getString(key: String): String? {
        val defaultValue = (defaultPreferences[key] ?: "") as String
        return sharedPreferences.getString(key, defaultValue)
    }

    override fun getBoolean(key: String): Boolean {
        val defaultValue = (defaultPreferences[key] ?: false) as Boolean
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    override fun getLong(key: String): Long {
        val defaultValue = (defaultPreferences[key] ?: 0L) as Long
        return sharedPreferences.getLong(key, defaultValue)
    }

    override fun getInt(key: String): Int {
        val defaultValue = (defaultPreferences[key] ?: 0) as Int
        return sharedPreferences.getInt(key, defaultValue)
    }

    override fun getFloat(key: String): Float {
        val defaultValue = (defaultPreferences[key] ?: 0f) as Float
        return sharedPreferences.getFloat(key, defaultValue)
    }

    override fun getStringSet(key: String): Set<String>? {
        val defaultValue = (defaultPreferences[key] ?: emptySet<Any>()) as Set<String>
        return sharedPreferences.getStringSet(key, defaultValue)
    }

    override fun registerOnPreferenceChangeListener(listener: PreferencesDataSource.OnPreferenceChangeListener) {
        sharedPreferencesListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key -> listener.onPreferenceChanged(key) }
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    override fun unregisterOnPreferenceChangeListener(listener: PreferencesDataSource.OnPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }
}

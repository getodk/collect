package org.odk.collect.android.preferences

import android.content.SharedPreferences
import java.util.Collections

class PreferencesDataSource(private val sharedPreferences: SharedPreferences, private val defaultPreferences: Map<String, Any> = emptyMap()) {
    fun loadDefaultPreferences() {
        saveAll(defaultPreferences)
    }

    fun save(key: String, value: Any?) {
        saveAll(Collections.singletonMap(key, value))
    }

    fun saveAll(prefs: Map<String, Any?>) {
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

    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    fun reset(key: String) {
        save(key, defaultPreferences[key])
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    fun getAll(): Map<String, *> {
        return sharedPreferences.all
    }

    // Use only for General and Admin settings which have default values, otherwise it won't be possible to determine the type
    fun get(key: String): Any? {
        return when (defaultPreferences[key]) {
            null, is String -> getString(key)
            is Boolean -> getBoolean(key)
            is Long -> getLong(key)
            is Int -> getInt(key)
            is Float -> getFloat(key)
            is Set<*> -> getStringSet(key)
            else -> throw RuntimeException("Unhandled preference value type: ${defaultPreferences[key]}")
        }
    }

    fun getString(key: String): String? {
        val defaultValue = (defaultPreferences[key] ?: "") as String
        return sharedPreferences.getString(key, defaultValue)
    }

    fun getBoolean(key: String): Boolean {
        val defaultValue = (defaultPreferences[key] ?: false) as Boolean
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun getLong(key: String): Long {
        val defaultValue = (defaultPreferences[key] ?: 0L) as Long
        return sharedPreferences.getLong(key, defaultValue)
    }

    fun getInt(key: String): Int {
        val defaultValue = (defaultPreferences[key] ?: 0) as Int
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun getFloat(key: String): Float {
        val defaultValue = (defaultPreferences[key] ?: 0f) as Float
        return sharedPreferences.getFloat(key, defaultValue)
    }

    fun getStringSet(key: String): Set<String>? {
        val defaultValue = (defaultPreferences[key] ?: emptySet<Any>()) as Set<String>
        return sharedPreferences.getStringSet(key, defaultValue)
    }

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}

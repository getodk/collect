package org.odk.collect.android.preferences.source

import android.content.SharedPreferences
import org.odk.collect.shared.settings.Settings
import java.lang.ref.WeakReference
import java.util.Collections

class SharedPreferencesSettings(
    private val sharedPreferences: SharedPreferences,
    private val settingKeysToDefaults: Map<String, Any> = emptyMap()
) : Settings {

    private val listeners =
        mutableListOf<Pair<WeakReference<Settings.OnSettingChangeListener>, SharedPreferences.OnSharedPreferenceChangeListener>>()

    override fun setDefaultForAllSettingsWithoutValues() {
        for ((key, value) in settingKeysToDefaults) {
            if (!sharedPreferences.contains(key)) {
                save(key, value)
            }
        }
    }

    override fun save(key: String, value: Any?) {
        saveAll(Collections.singletonMap(key, value))
    }

    override fun saveAll(settings: Map<String, Any?>) {
        val editor = sharedPreferences.edit()
        for ((key, value) in settings) {
            when (value) {
                null, is String -> editor.putString(key, value as String?)
                is Boolean -> editor.putBoolean(key, value)
                is Long -> editor.putLong(key, value)
                is Int -> editor.putInt(key, value)
                is Float -> editor.putFloat(key, value)
                is Set<*> -> editor.putStringSet(key, value as Set<String?>)
                else -> throw RuntimeException("Unhandled setting value type: $value")
            }
        }
        editor.apply()
    }

    override fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    override fun reset(key: String) {
        save(key, settingKeysToDefaults[key])
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
        val defaultValue = (settingKeysToDefaults[key]) as String?
        return sharedPreferences.getString(key, defaultValue)
    }

    override fun getBoolean(key: String): Boolean {
        val defaultValue = (settingKeysToDefaults[key] ?: false) as Boolean
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    override fun getLong(key: String): Long {
        val defaultValue = (settingKeysToDefaults[key] ?: 0L) as Long
        return sharedPreferences.getLong(key, defaultValue)
    }

    override fun getInt(key: String): Int {
        val defaultValue = (settingKeysToDefaults[key] ?: 0) as Int
        return sharedPreferences.getInt(key, defaultValue)
    }

    override fun getFloat(key: String): Float {
        val defaultValue = (settingKeysToDefaults[key] ?: 0f) as Float
        return sharedPreferences.getFloat(key, defaultValue)
    }

    override fun getStringSet(key: String): Set<String>? {
        val defaultValue = (settingKeysToDefaults[key] ?: emptySet<Any>()) as Set<String>
        return sharedPreferences.getStringSet(key, defaultValue)
    }

    override fun registerOnSettingChangeListener(listener: Settings.OnSettingChangeListener) {
        val sharedPreferencesListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key: String? ->
                key?.let {
                    listener.onSettingChanged(it)
                }
            }

        listeners.add(Pair(WeakReference(listener), sharedPreferencesListener))
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener)
    }

    override fun unregisterOnSettingChangeListener(listener: Settings.OnSettingChangeListener) {
        val pair = listeners.find { pair -> listener == pair.first.get() }
        listeners.remove(pair)

        if (pair != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(pair.second)
        }
    }
}

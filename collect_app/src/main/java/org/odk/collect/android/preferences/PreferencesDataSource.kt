package org.odk.collect.android.preferences

interface PreferencesDataSource {
    fun loadDefaultPreferencesIfNotExist()

    fun save(key: String, value: Any?)

    fun saveAll(prefs: Map<String, Any?>)

    fun remove(key: String)

    fun reset(key: String)

    fun clear()

    fun contains(key: String): Boolean

    fun getAll(): Map<String, *>

    fun getString(key: String): String?

    fun getBoolean(key: String): Boolean

    fun getLong(key: String): Long

    fun getInt(key: String): Int

    fun getFloat(key: String): Float

    fun getStringSet(key: String): Set<String>?

    fun registerOnPreferenceChangeListener(listener: OnPreferenceChangeListener)

    fun unregisterOnPreferenceChangeListener(listener: OnPreferenceChangeListener)

    interface OnPreferenceChangeListener {
        fun onPreferenceChanged(key: String)
    }
}

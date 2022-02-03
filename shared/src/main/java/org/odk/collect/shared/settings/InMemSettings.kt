package org.odk.collect.shared.settings

class InMemSettings : Settings {

    val map = mutableMapOf<String, Any?>()

    override fun save(key: String, value: Any?) {
        map[key] = value
    }

    override fun remove(key: String) {
        map.remove(key)
    }

    override fun getString(key: String): String? {
        return map[key] as String?
    }

    override fun setDefaultForAllSettingsWithoutValues() {
        TODO("Not yet implemented")
    }

    override fun saveAll(prefs: Map<String, Any?>) {
        map.putAll(prefs)
    }

    override fun reset(key: String) {
        TODO("Not yet implemented")
    }

    override fun clear() {
        map.clear()
    }

    override fun contains(key: String): Boolean {
        return map.containsKey(key)
    }

    override fun getAll(): Map<String, *> {
        return map
    }

    override fun getBoolean(key: String): Boolean {
        return map[key] as Boolean? ?: false
    }

    override fun getLong(key: String): Long {
        TODO("Not yet implemented")
    }

    override fun getInt(key: String): Int {
        return map[key] as Int? ?: 0
    }

    override fun getFloat(key: String): Float {
        TODO("Not yet implemented")
    }

    override fun getStringSet(key: String): Set<String>? {
        TODO("Not yet implemented")
    }

    override fun registerOnSettingChangeListener(listener: Settings.OnSettingChangeListener) {
        TODO("Not yet implemented")
    }

    override fun unregisterOnSettingChangeListener(listener: Settings.OnSettingChangeListener) {
        TODO("Not yet implemented")
    }
}

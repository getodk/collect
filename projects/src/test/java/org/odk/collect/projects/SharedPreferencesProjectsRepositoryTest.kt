package org.odk.collect.projects

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import org.junit.runner.RunWith
import org.odk.collect.shared.Settings
import org.odk.collect.shared.UUIDGenerator

@RunWith(AndroidJUnit4::class)
class SharedPreferencesProjectsRepositoryTest : ProjectsRepositoryTest() {
    override fun buildSubject(): ProjectsRepository {
        return SharedPreferencesProjectsRepository(UUIDGenerator(), Gson(), InMemSettings(), "test")
    }
}

private class InMemSettings : Settings {

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
        TODO("Not yet implemented")
    }

    override fun reset(key: String) {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }

    override fun contains(key: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAll(): Map<String, *> {
        TODO("Not yet implemented")
    }

    override fun getBoolean(key: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun getLong(key: String): Long {
        TODO("Not yet implemented")
    }

    override fun getInt(key: String): Int {
        TODO("Not yet implemented")
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

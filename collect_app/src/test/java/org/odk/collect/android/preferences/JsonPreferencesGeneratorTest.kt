package org.odk.collect.android.preferences

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.configure.qr.JsonPreferencesGenerator
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.keys.GeneralKeys

@ExperimentalStdlibApi
@RunWith(AndroidJUnit4::class)
class JsonPreferencesGeneratorTest {
    private val settingsProvider = TestSettingsProvider.getSettingsProvider()
    private lateinit var jsonPreferencesGenerator: JsonPreferencesGenerator

    @Before
    fun setup() {
        settingsProvider.getGeneralSettings().clear()
        settingsProvider.getAdminSettings().clear()

        jsonPreferencesGenerator = JsonPreferencesGenerator(settingsProvider)
    }

    @Test
    fun `When admin password included, should be present in json`() {
        val adminPrefs = buildMap<String, Any> {
            put(AdminKeys.KEY_ADMIN_PW, "123456")
        }

        settingsProvider.getAdminSettings().saveAll(adminPrefs)

        val jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(listOf(AdminKeys.KEY_ADMIN_PW))
        verifyJsonContent(jsonPrefs, emptyMap<String, Any>(), adminPrefs)
    }

    @Test
    fun `When admin password excluded, should not be present in json`() {
        val adminPrefs = buildMap<String, Any> {
            put(AdminKeys.KEY_ADMIN_PW, "123456")
        }

        settingsProvider.getAdminSettings().saveAll(adminPrefs)

        val jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(ArrayList())
        verifyJsonContent(jsonPrefs, emptyMap<String, Any>(), emptyMap<String, Any>())
    }

    @Test
    fun `When user password included, should be present in json`() {
        val generalPrefs = buildMap<String, Any> {
            put(GeneralKeys.KEY_PASSWORD, "123456")
        }

        settingsProvider.getGeneralSettings().saveAll(generalPrefs)

        val jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(listOf(GeneralKeys.KEY_PASSWORD))
        verifyJsonContent(jsonPrefs, generalPrefs, emptyMap<String, Any>())
    }

    @Test
    fun `When user password excluded, should not be present in json`() {
        val generalPrefs = buildMap<String, Any> {
            put(GeneralKeys.KEY_PASSWORD, "123456")
        }

        settingsProvider.getGeneralSettings().saveAll(generalPrefs)

        val jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(ArrayList())
        verifyJsonContent(jsonPrefs, emptyMap<String, Any>(), emptyMap<String, Any>())
    }

    @Test
    fun `Only saved settings should be included in json`() {
        val generalPrefs = buildMap<String, Any> {
            put(GeneralKeys.KEY_DELETE_AFTER_SEND, true)
            put(GeneralKeys.KEY_APP_THEME, "dark_theme")
        }

        val adminPrefs = buildMap<String, Any> {
            put(AdminKeys.KEY_GET_BLANK, false)
            put(AdminKeys.KEY_DELETE_SAVED, false)
        }

        settingsProvider.getGeneralSettings().saveAll(generalPrefs)
        settingsProvider.getAdminSettings().saveAll(adminPrefs)

        val jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(ArrayList())

        verifyJsonContent(jsonPrefs, generalPrefs, adminPrefs)
    }

    @Test
    fun `Saved but default settings should not be included in json`() {
        val generalPrefs = buildMap<String, Any> {
            put(GeneralKeys.KEY_DELETE_AFTER_SEND, false)
            put(GeneralKeys.KEY_APP_THEME, "light_theme")
        }

        val adminPrefs = buildMap<String, Any> {
            put(AdminKeys.KEY_GET_BLANK, true)
            put(AdminKeys.KEY_DELETE_SAVED, true)
        }

        settingsProvider.getGeneralSettings().saveAll(generalPrefs)
        settingsProvider.getAdminSettings().saveAll(adminPrefs)

        val jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(ArrayList())

        verifyJsonContent(jsonPrefs, emptyMap<String, Any>(), emptyMap<String, Any>())
    }

    private fun verifyJsonContent(jsonPrefsString: String, generalPrefs: Map<String, *>, adminPrefs: Map<String, *>) {
        val jsonPrefs = JSONObject(jsonPrefsString)
        assertThat(jsonPrefs.length(), `is`(2))
        assertThat(jsonPrefs.has("general"), `is`(true))
        assertThat(jsonPrefs.has("admin"), `is`(true))

        val jsonGeneralPrefs = jsonPrefs.get("general") as JSONObject
        assertThat(jsonGeneralPrefs.length(), `is`(generalPrefs.size))
        generalPrefs.entries.forEach {
            assertThat(jsonGeneralPrefs.get(it.key), `is`(it.value))
        }

        val jsonAdminPrefs = jsonPrefs.get("admin") as JSONObject
        assertThat(jsonAdminPrefs.length(), `is`(adminPrefs.size))
        adminPrefs.entries.forEach {
            assertThat(jsonAdminPrefs.get(it.key), `is`(it.value))
        }
    }
}

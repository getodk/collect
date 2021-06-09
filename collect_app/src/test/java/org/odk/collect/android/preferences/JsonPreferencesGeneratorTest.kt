package org.odk.collect.android.preferences

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.application.Collect
import org.odk.collect.android.configure.qr.JsonPreferencesGenerator
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.projects.Project

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
        val adminPrefs = mapOf<String, Any> (AdminKeys.KEY_ADMIN_PW to "123456")

        settingsProvider.getAdminSettings().saveAll(adminPrefs)

        val jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(listOf(AdminKeys.KEY_ADMIN_PW))
        verifyJsonContent(jsonPrefs, emptyMap<String, Any>(), adminPrefs)
    }

    @Test
    fun `When admin password excluded, should not be present in json`() {
        val adminPrefs = mapOf<String, Any> (AdminKeys.KEY_ADMIN_PW to "123456")

        settingsProvider.getAdminSettings().saveAll(adminPrefs)

        val jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences()
        verifyJsonContent(jsonPrefs, emptyMap<String, Any>(), emptyMap<String, Any>())
    }

    @Test
    fun `When user password included, should be present in json`() {
        val generalPrefs = mapOf<String, Any> (GeneralKeys.KEY_PASSWORD to "123456")

        settingsProvider.getGeneralSettings().saveAll(generalPrefs)

        val jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences(listOf(GeneralKeys.KEY_PASSWORD))
        verifyJsonContent(jsonPrefs, generalPrefs, emptyMap<String, Any>())
    }

    @Test
    fun `When user password excluded, should not be present in json`() {
        val generalPrefs = mapOf<String, Any> (GeneralKeys.KEY_PASSWORD to "123456")

        settingsProvider.getGeneralSettings().saveAll(generalPrefs)

        val jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences()
        verifyJsonContent(jsonPrefs, emptyMap<String, Any>(), emptyMap<String, Any>())
    }

    @Test
    fun `Only saved settings should be included in json`() {
        val generalPrefs = mapOf<String, Any> (
            GeneralKeys.KEY_DELETE_AFTER_SEND to true,
            GeneralKeys.KEY_APP_THEME to "dark_theme"
        )

        val adminPrefs = mapOf<String, Any> (
            AdminKeys.KEY_GET_BLANK to false,
            AdminKeys.KEY_DELETE_SAVED to false
        )

        settingsProvider.getGeneralSettings().saveAll(generalPrefs)
        settingsProvider.getAdminSettings().saveAll(adminPrefs)

        val jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences()

        verifyJsonContent(jsonPrefs, generalPrefs, adminPrefs)
    }

    @Test
    fun `Saved but default settings should not be included in json`() {
        val generalPrefs = mapOf<String, Any> (
            GeneralKeys.KEY_DELETE_AFTER_SEND to false,
            GeneralKeys.KEY_APP_THEME to "light_theme"
        )

        val adminPrefs = mapOf<String, Any> (
            AdminKeys.KEY_GET_BLANK to true,
            AdminKeys.KEY_DELETE_SAVED to true
        )

        settingsProvider.getGeneralSettings().saveAll(generalPrefs)
        settingsProvider.getAdminSettings().saveAll(adminPrefs)

        val jsonPrefs = jsonPreferencesGenerator.getJSONFromPreferences()

        verifyJsonContent(jsonPrefs, emptyMap<String, Any>(), emptyMap<String, Any>())
    }

    @Test
    fun `Only preferences from the current project should be included in json`() {
        // Setup settings for the demo project
        val generalPrefsForDemoProject = mapOf<String, Any> (
            GeneralKeys.KEY_DELETE_AFTER_SEND to true,
            GeneralKeys.KEY_APP_THEME to "dark_theme"
        )

        val adminPrefsForDemoProject = mapOf<String, Any> (
            AdminKeys.KEY_GET_BLANK to false,
            AdminKeys.KEY_DELETE_SAVED to false
        )

        settingsProvider.getGeneralSettings().saveAll(generalPrefsForDemoProject)
        settingsProvider.getAdminSettings().saveAll(adminPrefsForDemoProject)

        // Setup settings for another project
        DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Collect>()).projectsRepository().save(Project.Saved("2", "Project X", "x", "#cccccc"))

        val generalPrefsForProjectX = mapOf<String, Any> (
            GeneralKeys.KEY_COMPLETED_DEFAULT to false,
            GeneralKeys.KEY_IMAGE_SIZE to "large"
        )

        val adminPrefsForProjectX = mapOf<String, Any> (
            AdminKeys.KEY_SEND_FINALIZED to false,
            AdminKeys.KEY_VIEW_SENT to false
        )

        settingsProvider.getGeneralSettings("2").saveAll(generalPrefsForProjectX)
        settingsProvider.getAdminSettings("2").saveAll(adminPrefsForProjectX)

        // Verify the demo project
        verifyJsonContent(jsonPreferencesGenerator.getJSONFromPreferences(), generalPrefsForDemoProject, adminPrefsForDemoProject)

        // Verify the 'Project X' project
        DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Collect>()).currentProjectProvider().setCurrentProject("2")
        verifyJsonContent(jsonPreferencesGenerator.getJSONFromPreferences(), generalPrefsForProjectX, adminPrefsForProjectX)
    }

    @Test
    fun `When project details provided should getProjectDetailsAsJson() generate proper json`() {
        val generalPrefs = mapOf<String, Any> (
            GeneralKeys.KEY_SERVER_URL to "https://my-server.com",
            GeneralKeys.KEY_USERNAME to "adam",
            GeneralKeys.KEY_PASSWORD to "1234"
        )

        val jsonPrefs = jsonPreferencesGenerator.getProjectDetailsAsJson("https://my-server.com", "adam", "1234")

        verifyJsonContent(jsonPrefs, generalPrefs, emptyMap<String, Any>())
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

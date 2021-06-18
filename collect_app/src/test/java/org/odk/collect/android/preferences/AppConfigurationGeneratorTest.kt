package org.odk.collect.android.preferences

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.configure.qr.AppConfigurationGenerator
import org.odk.collect.android.configure.qr.AppConfigurationKeys
import org.odk.collect.android.preferences.keys.AdminKeys
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.support.InMemSettingsProvider
import org.odk.collect.projects.Project

@RunWith(AndroidJUnit4::class)
class AppConfigurationGeneratorTest {

    private val settingsProvider = InMemSettingsProvider()

    private val currentProjectProvider: CurrentProjectProvider = mock {
        on { getCurrentProject() } doReturn Project.Saved("1", "Project X", "X", "#cccccc")
    }

    val projectDetails = mapOf(
        AppConfigurationKeys.PROJECT_NAME to "Project X",
        AppConfigurationKeys.PROJECT_ICON to "X",
        AppConfigurationKeys.PROJECT_COLOR to "#cccccc"
    )

    private lateinit var appConfigurationGenerator: AppConfigurationGenerator

    @Before
    fun setup() {
        appConfigurationGenerator = AppConfigurationGenerator(settingsProvider, currentProjectProvider)
    }

    @Test
    fun `When admin password included, should be present in json`() {
        val adminPrefs = mapOf<String, Any> (AdminKeys.KEY_ADMIN_PW to "123456")

        settingsProvider.getAdminSettings().saveAll(adminPrefs)

        val jsonPrefs = appConfigurationGenerator.getAppConfigurationAsJson(listOf(AdminKeys.KEY_ADMIN_PW))
        verifyJsonContent(jsonPrefs, emptyMap<String, Any>(), adminPrefs, projectDetails)
    }

    @Test
    fun `When admin password excluded, should not be present in json`() {
        val adminPrefs = mapOf<String, Any> (AdminKeys.KEY_ADMIN_PW to "123456")

        settingsProvider.getAdminSettings().saveAll(adminPrefs)

        val jsonPrefs = appConfigurationGenerator.getAppConfigurationAsJson()
        verifyJsonContent(jsonPrefs, emptyMap<String, Any>(), emptyMap<String, Any>(), projectDetails)
    }

    @Test
    fun `When user password included, should be present in json`() {
        val generalPrefs = mapOf<String, Any> (GeneralKeys.KEY_PASSWORD to "123456")

        settingsProvider.getGeneralSettings().saveAll(generalPrefs)

        val jsonPrefs = appConfigurationGenerator.getAppConfigurationAsJson(listOf(GeneralKeys.KEY_PASSWORD))
        verifyJsonContent(jsonPrefs, generalPrefs, emptyMap<String, Any>(), projectDetails)
    }

    @Test
    fun `When user password excluded, should not be present in json`() {
        val generalPrefs = mapOf<String, Any> (GeneralKeys.KEY_PASSWORD to "123456")

        settingsProvider.getGeneralSettings().saveAll(generalPrefs)

        val jsonPrefs = appConfigurationGenerator.getAppConfigurationAsJson()
        verifyJsonContent(jsonPrefs, emptyMap<String, Any>(), emptyMap<String, Any>(), projectDetails)
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

        val jsonPrefs = appConfigurationGenerator.getAppConfigurationAsJson()

        verifyJsonContent(jsonPrefs, generalPrefs, adminPrefs, projectDetails)
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

        val jsonPrefs = appConfigurationGenerator.getAppConfigurationAsJson()

        verifyJsonContent(jsonPrefs, emptyMap<String, Any>(), emptyMap<String, Any>(), projectDetails)
    }

    @Test
    fun `When server details provided should getProjectDetailsAsJson() generate proper json`() {
        val generalPrefs = mapOf<String, Any> (
            GeneralKeys.KEY_SERVER_URL to "https://my-server.com",
            GeneralKeys.KEY_USERNAME to "adam",
            GeneralKeys.KEY_PASSWORD to "1234"
        )

        val jsonPrefs = appConfigurationGenerator.getAppConfigurationAsJsonWithServerDetails("https://my-server.com", "adam", "1234")

        verifyJsonContent(jsonPrefs, generalPrefs, emptyMap<String, Any>(), emptyMap())
    }

    private fun verifyJsonContent(jsonPrefsString: String, generalPrefs: Map<String, *>, adminPrefs: Map<String, *>, projectDetails: Map<String, String>) {
        val jsonPrefs = JSONObject(jsonPrefsString)
        assertThat(jsonPrefs.length(), `is`(3))
        assertThat(jsonPrefs.has(AppConfigurationKeys.GENERAL), `is`(true))
        assertThat(jsonPrefs.has(AppConfigurationKeys.ADMIN), `is`(true))
        assertThat(jsonPrefs.has(AppConfigurationKeys.PROJECT), `is`(true))

        val jsonGeneralPrefs = jsonPrefs.get(AppConfigurationKeys.GENERAL) as JSONObject
        assertThat(jsonGeneralPrefs.length(), `is`(generalPrefs.size))
        generalPrefs.entries.forEach {
            assertThat(jsonGeneralPrefs.get(it.key), `is`(it.value))
        }

        val jsonAdminPrefs = jsonPrefs.get(AppConfigurationKeys.ADMIN) as JSONObject
        assertThat(jsonAdminPrefs.length(), `is`(adminPrefs.size))
        adminPrefs.entries.forEach {
            assertThat(jsonAdminPrefs.get(it.key), `is`(it.value))
        }

        val projectDetailsJson = jsonPrefs.get(AppConfigurationKeys.PROJECT) as JSONObject
        assertThat(projectDetailsJson.length(), `is`(projectDetails.size))
        projectDetails.entries.forEach {
            assertThat(projectDetailsJson.get(it.key), `is`(it.value))
        }
    }
}

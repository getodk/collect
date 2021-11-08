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
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.preferences.keys.ProtectedProjectKeys
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
        val adminPrefs = mapOf<String, Any> (ProtectedProjectKeys.KEY_ADMIN_PW to "123456")

        settingsProvider.getProtectedSettings().saveAll(adminPrefs)

        val jsonPrefs = appConfigurationGenerator.getAppConfigurationAsJson(listOf(ProtectedProjectKeys.KEY_ADMIN_PW))
        verifyJsonContent(jsonPrefs, emptyMap<String, Any>(), adminPrefs, projectDetails)
    }

    @Test
    fun `When admin password excluded, should not be present in json`() {
        val adminPrefs = mapOf<String, Any> (ProtectedProjectKeys.KEY_ADMIN_PW to "123456")

        settingsProvider.getProtectedSettings().saveAll(adminPrefs)

        val jsonPrefs = appConfigurationGenerator.getAppConfigurationAsJson()
        verifyJsonContent(jsonPrefs, emptyMap<String, Any>(), emptyMap<String, Any>(), projectDetails)
    }

    @Test
    fun `When user password included, should be present in json`() {
        val generalPrefs = mapOf<String, Any> (ProjectKeys.KEY_PASSWORD to "123456")

        settingsProvider.getUnprotectedSettings().saveAll(generalPrefs)

        val jsonPrefs = appConfigurationGenerator.getAppConfigurationAsJson(listOf(ProjectKeys.KEY_PASSWORD))
        verifyJsonContent(jsonPrefs, generalPrefs, emptyMap<String, Any>(), projectDetails)
    }

    @Test
    fun `When user password excluded, should not be present in json`() {
        val generalPrefs = mapOf<String, Any> (ProjectKeys.KEY_PASSWORD to "123456")

        settingsProvider.getUnprotectedSettings().saveAll(generalPrefs)

        val jsonPrefs = appConfigurationGenerator.getAppConfigurationAsJson()
        verifyJsonContent(jsonPrefs, emptyMap<String, Any>(), emptyMap<String, Any>(), projectDetails)
    }

    @Test
    fun `Only saved settings should be included in json`() {
        val generalPrefs = mapOf<String, Any> (
            ProjectKeys.KEY_DELETE_AFTER_SEND to true,
            ProjectKeys.KEY_APP_THEME to "dark_theme"
        )

        val adminPrefs = mapOf<String, Any> (
            ProtectedProjectKeys.KEY_GET_BLANK to false,
            ProtectedProjectKeys.KEY_DELETE_SAVED to false
        )

        settingsProvider.getUnprotectedSettings().saveAll(generalPrefs)
        settingsProvider.getProtectedSettings().saveAll(adminPrefs)

        val jsonPrefs = appConfigurationGenerator.getAppConfigurationAsJson()

        verifyJsonContent(jsonPrefs, generalPrefs, adminPrefs, projectDetails)
    }

    @Test
    fun `Saved but default settings should not be included in json`() {
        val generalPrefs = ProjectKeys.defaults.filter {
            it.key == ProjectKeys.KEY_DELETE_AFTER_SEND || it.key == ProjectKeys.KEY_APP_THEME
        }

        val adminPrefs = ProtectedProjectKeys.defaults.filter {
            it.key == ProtectedProjectKeys.KEY_GET_BLANK || it.key == ProtectedProjectKeys.KEY_DELETE_SAVED
        }

        settingsProvider.getUnprotectedSettings().saveAll(generalPrefs)
        settingsProvider.getProtectedSettings().saveAll(adminPrefs)

        val jsonPrefs = appConfigurationGenerator.getAppConfigurationAsJson()

        verifyJsonContent(jsonPrefs, emptyMap<String, Any>(), emptyMap<String, Any>(), projectDetails)
    }

    @Test
    fun `When server details provided should getAppConfigurationAsJsonWithServerDetails() generate proper json`() {
        val generalPrefs = mapOf<String, Any> (
            ProjectKeys.KEY_SERVER_URL to "https://my-server.com",
            ProjectKeys.KEY_USERNAME to "adam",
            ProjectKeys.KEY_PASSWORD to "1234"
        )

        val jsonPrefs = appConfigurationGenerator.getAppConfigurationAsJsonWithServerDetails("https://my-server.com", "adam", "1234")

        verifyJsonContent(jsonPrefs, generalPrefs, emptyMap<String, Any>(), emptyMap())
    }

    @Test
    fun `When Google account provided, getAppConfigurationAsJsonWithGoogleDriveDetails generates expected json`() {
        val expectedPrefs = mapOf<String, Any> (
            ProjectKeys.KEY_SERVER_URL to "",
            ProjectKeys.KEY_PROTOCOL to ProjectKeys.PROTOCOL_GOOGLE_SHEETS,
            ProjectKeys.KEY_SELECTED_GOOGLE_ACCOUNT to "foo@bar.baz"
        )

        val jsonPrefs = appConfigurationGenerator.getAppConfigurationAsJsonWithGoogleDriveDetails("foo@bar.baz")

        verifyJsonContent(jsonPrefs, expectedPrefs, emptyMap<String, Any>(), emptyMap())
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

package org.odk.collect.android.configure

import androidx.core.util.Pair
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.containsInAnyOrder
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.application.initialization.SettingsMigrator
import org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils
import org.odk.collect.android.configure.qr.AppConfigurationKeys
import org.odk.collect.android.preferences.keys.ProjectKeys
import org.odk.collect.android.projects.ProjectDetailsCreator
import org.odk.collect.android.support.InMemSettingsProvider
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.shared.Settings

@RunWith(AndroidJUnit4::class)
class SettingsImporterTest {

    private var currentProject = Project.Saved("1", "Project X", "X", "#cccccc")

    private val settingsProvider = InMemSettingsProvider()
    private val generalSettings = settingsProvider.getUnprotectedSettings(currentProject.uuid)
    private val adminSettings = settingsProvider.getProtectedSettings(currentProject.uuid)

    private val projectsRepository = mock<ProjectsRepository> {}
    private var settingsValidator = mock<SettingsValidator> {
        on { isValid(any()) } doReturn true
    }
    private val projectDetailsCreator = mock<ProjectDetailsCreator> {
        on { createProjectFromDetails() } doReturn Project.DEMO_PROJECT
    }

    private val generalDefaults: Map<String, Any> = mapOf(
        "key1" to "default",
        "key2" to true
    )

    private val adminDefaults: Map<String, Any> = mapOf(
        "key1" to 5
    )

    private lateinit var importer: SettingsImporter

    @Before
    fun setup() {
        importer = SettingsImporter(
            settingsProvider,
            { _: Settings?, _: Settings? -> },
            settingsValidator,
            generalDefaults,
            adminDefaults,
            { _: String?, _: Any?, _: String? -> },
            projectsRepository,
            projectDetailsCreator
        )
    }

    @Test
    fun whenJSONSettingsAreInvalid_returnsFalse() {
        whenever(settingsValidator.isValid(emptySettings())).thenReturn(false)
        assertThat(importer.fromJSON(emptySettings(), currentProject), `is`(false))
    }

    @Test
    fun forSettingsKeysNotINJSON_savesDefaults() {
        assertThat(importer.fromJSON(emptySettings(), currentProject), `is`(true))
        SharedPreferenceUtils.assertPrefs(
            generalSettings,
            "key1", "default",
            "key2", true
        )
        SharedPreferenceUtils.assertPrefs(
            adminSettings,
            "key1", 5
        )
    }

    @Test
    fun whenKeysAlreadyExistInPrefs_overridesWithDefaults() {
        SharedPreferenceUtils.initPrefs(
            generalSettings,
            "key1", "existing",
            "key2", false
        )
        SharedPreferenceUtils.initPrefs(
            adminSettings,
            "key1", 0
        )
        assertThat(importer.fromJSON(emptySettings(), currentProject), `is`(true))
        SharedPreferenceUtils.assertPrefs(
            generalSettings,
            "key1", "default",
            "key2", true
        )
        SharedPreferenceUtils.assertPrefs(
            adminSettings,
            "key1", 5
        )
    }

    @Test
    fun removesUnknownKeys() {
        val json = emptySettingsObject()
            .put(
                AppConfigurationKeys.GENERAL,
                JSONObject().put("unknown_key", "value")
            )
        assertThat(importer.fromJSON(json.toString(), currentProject), `is`(true))
        assertThat(generalSettings.contains("unknown_key"), `is`(false))
    }

    @Test // Migrations might add/rename/move keys
    fun migratesPreferences_beforeLoadingDefaults() {
        val migrator =
            SettingsMigrator { _: Settings?, _: Settings? ->
                if (generalSettings.contains("key1")) {
                    throw RuntimeException("defaults already loaded!")
                }
            }
        importer = SettingsImporter(
            settingsProvider,
            migrator,
            settingsValidator,
            generalDefaults,
            adminDefaults,
            { _: String?, _: Any?, _: String? -> },
            projectsRepository,
            projectDetailsCreator
        )
        assertThat(importer.fromJSON(emptySettings(), currentProject), `is`(true))
    }

    @Test // Migrations might use old keys that are "unknown" to the app
    fun migratesPreferences_beforeClearingUnknowns() {
        val json = emptySettingsObject()
            .put(
                AppConfigurationKeys.GENERAL,
                JSONObject().put("unknown_key", "value")
            )
        val migrator =
            SettingsMigrator { _: Settings?, _: Settings? ->
                if (!generalSettings.contains("unknown_key")) {
                    throw RuntimeException("unknowns already cleared!")
                }
            }
        importer = SettingsImporter(
            settingsProvider,
            migrator,
            settingsValidator,
            generalDefaults,
            adminDefaults,
            { _: String?, _: Any?, _: String? -> },
            projectsRepository,
            projectDetailsCreator
        )
        assertThat(importer.fromJSON(json.toString(), currentProject), `is`(true))
    }

    @Test
    fun afterSettingsImportedAndMigrated_runsSettingsChangeHandlerForEveryKey() {
        val handler = RecordingSettingsChangeHandler()
        importer = SettingsImporter(
            settingsProvider,
            { _: Settings?, _: Settings? -> },
            settingsValidator,
            generalDefaults,
            adminDefaults,
            handler,
            projectsRepository,
            projectDetailsCreator
        )
        assertThat(importer.fromJSON(emptySettings(), currentProject), `is`(true))
        assertThat<List<Pair<String, Any>>>(
            handler.changes,
            containsInAnyOrder(
                Pair("key1", "default"),
                Pair("key2", true),
                Pair("key1", 5)
            )
        )
    }

    @Test
    fun projectDetailsShouldBeImportedIfIncludedInJson() {
        val newProject = Project.New("Project Y", "Y", "#000000")

        val projectJson = JSONObject()
            .put(AppConfigurationKeys.PROJECT_NAME, newProject.name)
            .put(AppConfigurationKeys.PROJECT_ICON, newProject.icon)
            .put(AppConfigurationKeys.PROJECT_COLOR, newProject.color)
        val settings = JSONObject()
            .put(AppConfigurationKeys.GENERAL, JSONObject())
            .put(AppConfigurationKeys.ADMIN, JSONObject())
            .put(AppConfigurationKeys.PROJECT, projectJson)

        whenever(projectDetailsCreator.createProjectFromDetails(newProject.name, newProject.icon, newProject.color, "")).thenReturn(newProject)

        importer.fromJSON(settings.toString(), currentProject)
        verify(projectsRepository)
            .save(Project.Saved(currentProject.uuid, newProject.name, newProject.icon, newProject.color))
    }

    @Test
    fun `when protocol is server and project name not set, project name falls back to server url`() {
        val generalJson = JSONObject()
            .put(ProjectKeys.KEY_SERVER_URL, "foo")
        val settings = JSONObject()
            .put(AppConfigurationKeys.GENERAL, generalJson)
            .put(AppConfigurationKeys.ADMIN, JSONObject())

        whenever(projectDetailsCreator.createProjectFromDetails(any(), any(), any(), any())).thenReturn(Project.New("A", "B", "C"))

        importer.fromJSON(settings.toString(), currentProject)
        verify(projectDetailsCreator).createProjectFromDetails("", "", "", "foo")
    }

    @Test
    fun `when protocol is Google Drive and project name not set, project name falls back to Google account`() {
        val generalJson = JSONObject()
            .put(ProjectKeys.KEY_PROTOCOL, ProjectKeys.PROTOCOL_GOOGLE_SHEETS)
            .put(ProjectKeys.KEY_SELECTED_GOOGLE_ACCOUNT, "foo@bar.baz")
        val settings = JSONObject()
            .put(AppConfigurationKeys.GENERAL, generalJson)
            .put(AppConfigurationKeys.ADMIN, JSONObject())

        whenever(projectDetailsCreator.createProjectFromDetails(any(), any(), any(), any())).thenReturn(Project.New("A", "B", "C"))

        importer.fromJSON(settings.toString(), currentProject)
        verify(projectDetailsCreator).createProjectFromDetails("", "", "", "foo@bar.baz")
    }

    private fun emptySettings(): String {
        return emptySettingsObject()
            .toString()
    }

    private fun emptySettingsObject(): JSONObject {
        return JSONObject()
            .put(AppConfigurationKeys.GENERAL, JSONObject())
            .put(AppConfigurationKeys.ADMIN, JSONObject())
    }

    private class RecordingSettingsChangeHandler : SettingsChangeHandler {
        var changes: MutableList<Pair<String, Any>> = ArrayList()
        override fun onSettingChanged(projectId: String, newValue: Any, changedKey: String) {
            changes.add(Pair(changedKey, newValue))
        }
    }
}

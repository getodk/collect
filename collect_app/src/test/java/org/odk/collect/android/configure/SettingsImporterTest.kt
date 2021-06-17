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
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.application.initialization.SettingsMigrator
import org.odk.collect.android.application.initialization.migration.SharedPreferenceUtils
import org.odk.collect.android.configure.qr.AppConfigurationKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.shared.Settings
import org.odk.collect.testshared.InMemSettings

@RunWith(AndroidJUnit4::class)
class SettingsImporterTest {
    private var currentProject = Project.Saved("1", "Project X", "X", "#cccccc")

    private val generalSettings = InMemSettings()
    private val adminSettings = InMemSettings()

    private val settingsProvider = mock<SettingsProvider> {
        on { getGeneralSettings(currentProject.uuid) } doReturn generalSettings
        on { getAdminSettings(currentProject.uuid) } doReturn adminSettings
    }

    private val projectsRepository = mock<ProjectsRepository> {}
    private var settingsValidator = mock<SettingsValidator> {
        on { isValid(any()) } doReturn true
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
            projectsRepository
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
            projectsRepository
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
            projectsRepository
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
            projectsRepository
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
        val projectJson = JSONObject()
            .put(AppConfigurationKeys.PROJECT_NAME, "Project X")
            .put(AppConfigurationKeys.PROJECT_ICON, "X")
            .put(AppConfigurationKeys.PROJECT_COLOR, "#cccccc")
        val settings = JSONObject()
            .put(AppConfigurationKeys.GENERAL, JSONObject())
            .put(AppConfigurationKeys.ADMIN, JSONObject())
            .put(AppConfigurationKeys.PROJECT, projectJson)
        importer = SettingsImporter(
            settingsProvider,
            { _: Settings?, _: Settings? -> },
            settingsValidator,
            generalDefaults,
            adminDefaults,
            { _: String?, _: Any?, _: String? -> },
            projectsRepository
        )
        importer.fromJSON(settings.toString(), currentProject)
        Mockito.verify(projectsRepository)
            .save(Project.Saved(currentProject.uuid, "Project X", "X", "#cccccc"))
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

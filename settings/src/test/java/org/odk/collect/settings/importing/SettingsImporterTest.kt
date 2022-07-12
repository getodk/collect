package org.odk.collect.settings.importing

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.AppConfigurationKeys
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.settings.support.SettingsUtils.assertSettings
import org.odk.collect.settings.support.SettingsUtils.initSettings
import org.odk.collect.shared.settings.Settings

class SettingsImporterTest {

    private var currentProject = Project.Saved("1", "Project X", "X", "#cccccc")

    private val settingsProvider = InMemSettingsProvider()
    private val generalSettings = settingsProvider.getUnprotectedSettings(currentProject.uuid)
    private val adminSettings = settingsProvider.getProtectedSettings(currentProject.uuid)

    private val settingsChangeHandler = mock<SettingsChangeHandler>()
    private val projectsRepository = mock<ProjectsRepository> {}
    private var settingsValidator = mock<SettingsValidator> {
        on { isValid(any()) } doReturn true
        on { isKeySupported(any(), any()) } doReturn true
        on { isValueSupported(any(), any(), any()) } doReturn true
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
            settingsChangeHandler,
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
    fun `unsupported settings should be ignored`() {
        whenever(settingsValidator.isKeySupported(AppConfigurationKeys.GENERAL, "key3")).thenReturn(false)
        whenever(settingsValidator.isKeySupported(AppConfigurationKeys.ADMIN, "key3")).thenReturn(false)

        val json = emptySettingsObject()
            .put(
                AppConfigurationKeys.GENERAL,
                JSONObject().put("key3", "foo")
            )
            .put(
                AppConfigurationKeys.ADMIN,
                JSONObject().put("key3", 5)
            )

        assertThat(importer.fromJSON(json.toString(), currentProject), `is`(true))

        assertThat(generalSettings.contains("key3"), `is`(false))
        assertThat(adminSettings.contains("key3"), `is`(false))
    }

    @Test
    fun `for supported settings that do not exist in json save defaults`() {
        assertThat(importer.fromJSON(emptySettings(), currentProject), `is`(true))
        assertSettings(
            generalSettings,
            "key1", "default",
            "key2", true
        )
        assertSettings(
            adminSettings,
            "key1", 5
        )
    }

    @Test
    fun forSettingsNotSupported_savesDefaults() {
        whenever(settingsValidator.isValueSupported(AppConfigurationKeys.GENERAL, "key1", "unsupported_value")).thenReturn(false)
        whenever(settingsValidator.isValueSupported(AppConfigurationKeys.ADMIN, "key1", 6)).thenReturn(false)

        val json = emptySettingsObject()
            .put(
                AppConfigurationKeys.GENERAL,
                JSONObject().put("key1", "unsupported_value")
            )
            .put(
                AppConfigurationKeys.ADMIN,
                JSONObject().put("key1", 6)
            )

        assertThat(importer.fromJSON(json.toString(), currentProject), `is`(true))
        assertSettings(
            generalSettings,
            "key1", "default",
            "key2", true
        )
        assertSettings(
            adminSettings,
            "key1", 5
        )
    }

    @Test
    fun whenKeysAlreadyExistInPrefs_overridesWithDefaults() {
        initSettings(
            generalSettings,
            "key1", "existing",
            "key2", false
        )
        initSettings(
            adminSettings,
            "key1", 0
        )
        assertThat(importer.fromJSON(emptySettings(), currentProject), `is`(true))
        assertSettings(
            generalSettings,
            "key1", "default",
            "key2", true
        )
        assertSettings(
            adminSettings,
            "key1", 5
        )
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
            settingsChangeHandler,
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
            settingsChangeHandler,
            projectsRepository,
            projectDetailsCreator
        )
        assertThat(importer.fromJSON(json.toString(), currentProject), `is`(true))
    }

    @Test
    fun afterSettingsImportedAndMigrated_runsSettingsChangeHandler() {
        importer = SettingsImporter(
            settingsProvider,
            { _: Settings?, _: Settings? -> },
            settingsValidator,
            generalDefaults,
            adminDefaults,
            settingsChangeHandler,
            projectsRepository,
            projectDetailsCreator
        )
        assertThat(importer.fromJSON(emptySettings(), currentProject), `is`(true))
        verify(settingsChangeHandler).onSettingsChanged("1")
        verifyNoMoreInteractions(settingsChangeHandler)
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

        whenever(
            projectDetailsCreator.createProjectFromDetails(
                newProject.name,
                newProject.icon,
                newProject.color,
                ""
            )
        ).thenReturn(newProject)

        importer.fromJSON(settings.toString(), currentProject)
        verify(projectsRepository)
            .save(
                Project.Saved(
                    currentProject.uuid,
                    newProject.name,
                    newProject.icon,
                    newProject.color
                )
            )
    }

    @Test
    fun `when protocol is server and project name not set, project name falls back to server url`() {
        val generalJson = JSONObject()
            .put(ProjectKeys.KEY_SERVER_URL, "foo")
        val settings = JSONObject()
            .put(AppConfigurationKeys.GENERAL, generalJson)
            .put(AppConfigurationKeys.ADMIN, JSONObject())

        whenever(
            projectDetailsCreator.createProjectFromDetails(
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(Project.New("A", "B", "C"))

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

        whenever(
            projectDetailsCreator.createProjectFromDetails(
                any(),
                any(),
                any(),
                any()
            )
        ).thenReturn(Project.New("A", "B", "C"))

        importer.fromJSON(settings.toString(), currentProject)
        verify(projectDetailsCreator).createProjectFromDetails("", "", "", "foo@bar.baz")
    }

    private fun emptySettings(): String {
        return emptySettingsObject().toString()
    }

    private fun emptySettingsObject(): JSONObject {
        return JSONObject()
            .put(AppConfigurationKeys.GENERAL, JSONObject())
            .put(AppConfigurationKeys.ADMIN, JSONObject())
    }
}

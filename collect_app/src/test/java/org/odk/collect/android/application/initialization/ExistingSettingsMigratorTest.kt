package org.odk.collect.android.application.initialization

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.testshared.InMemSettings

class ExistingSettingsMigratorTest {

    @Test
    fun `migrates general and admin settings for each project`() {
        val projectsRepository = InMemProjectsRepository()
        val project1 = projectsRepository.save(Project.New("1", "1", "#ffffff"))
        val project2 = projectsRepository.save(Project.New("2", "2", "#ffffff"))

        val project1GeneralSettings = InMemSettings()
        val project1AdminSettings = InMemSettings()
        val project2GeneralSettings = InMemSettings()
        val project2AdminSettings = InMemSettings()

        val settingsProvider = mock<SettingsProvider> {
            on { getGeneralSettings(project1.uuid) } doReturn project1GeneralSettings
            on { getAdminSettings(project1.uuid) } doReturn project1AdminSettings
            on { getGeneralSettings(project2.uuid) } doReturn project2GeneralSettings
            on { getAdminSettings(project2.uuid) } doReturn project2AdminSettings
        }

        val settingsMigrator = mock<SettingsMigrator>()
        val existingSettingsMigrator =
            ExistingSettingsMigrator(projectsRepository, settingsProvider, settingsMigrator)

        existingSettingsMigrator.run()
        verify(settingsMigrator).migrate(project1GeneralSettings, project1AdminSettings)
        verify(settingsMigrator).migrate(project2GeneralSettings, project2AdminSettings)
    }

    @Test
    fun `has null key`() {
        val existingSettingsMigrator = ExistingSettingsMigrator(mock(), mock(), mock())
        assertThat(existingSettingsMigrator.key(), `is`(nullValue()))
    }
}

package org.odk.collect.android.application.initialization

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.support.InMemSettingsProvider
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project

class ExistingSettingsMigratorTest {

    @Test
    fun `migrates unprotected and protected settings for each project`() {
        val projectsRepository = InMemProjectsRepository()
        val project1 = projectsRepository.save(Project.New("1", "1", "#ffffff"))
        val project2 = projectsRepository.save(Project.New("2", "2", "#ffffff"))

        val settingsProvider = InMemSettingsProvider()
        val settingsMigrator = mock<SettingsMigrator>()
        val existingSettingsMigrator =
            ExistingSettingsMigrator(projectsRepository, settingsProvider, settingsMigrator)

        existingSettingsMigrator.run()
        verify(settingsMigrator).migrate(
            settingsProvider.getUnprotectedSettings(project1.uuid),
            settingsProvider.getProtectedSettings(project1.uuid)
        )
        verify(settingsMigrator).migrate(
            settingsProvider.getUnprotectedSettings(project2.uuid),
            settingsProvider.getProtectedSettings(project2.uuid)
        )
    }

    @Test
    fun `has null key`() {
        val existingSettingsMigrator = ExistingSettingsMigrator(mock(), mock(), mock())
        assertThat(existingSettingsMigrator.key(), `is`(nullValue()))
    }
}

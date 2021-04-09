package org.odk.collect.android.projects

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.preferences.source.SharedPreferencesSettings

class CurrentProjectProviderTest {

    private lateinit var projectsRepository: ProjectsRepository
    private lateinit var metaSettings: SharedPreferencesSettings
    private lateinit var settingsProvider: SettingsProvider
    private lateinit var currentProjectProvider: CurrentProjectProvider

    @Before
    fun setup() {
        metaSettings = mock(SharedPreferencesSettings::class.java)
        settingsProvider = mock(SettingsProvider::class.java)
        projectsRepository = mock(ProjectsRepository::class.java)
        currentProjectProvider = CurrentProjectProvider(settingsProvider, projectsRepository)
    }

    @Test
    fun getCurrentProjectId_shouldReturnProjectIdOfCurrentProject() {
        `when`(settingsProvider.getMetaSettings()).thenReturn(metaSettings)
        `when`(metaSettings.getString(MetaKeys.CURRENT_PROJECT_ID)).thenReturn("123e4567")

        assertThat(currentProjectProvider.getCurrentProjectId(), `is`("123e4567"))
    }

    @Test
    fun getCurrentProject_shouldReturnProjectForGivenIdIfExist() {
        `when`(settingsProvider.getMetaSettings()).thenReturn(metaSettings)
        `when`(metaSettings.getString(MetaKeys.CURRENT_PROJECT_ID)).thenReturn("123e4567")
        val project = Project("ProjectX", "X", "#00FF00", "123e4567")
        `when`(projectsRepository.get("123e4567")).thenReturn(project)

        assertThat(currentProjectProvider.getCurrentProject(), `is`(project))
    }

    @Test
    fun getCurrentProject_shouldReturnNullIfThereIsNoProjectForGivenId() {
        `when`(settingsProvider.getMetaSettings()).thenReturn(metaSettings)
        `when`(metaSettings.getString(MetaKeys.CURRENT_PROJECT_ID)).thenReturn("123e4567")
        `when`(projectsRepository.get("123e4567")).thenReturn(null)

        assertThat(currentProjectProvider.getCurrentProject(), `is`(nullValue()))
    }

    @Test
    fun setCurrentProject_shouldCallSaveOnMetaSettingsWithProperValues() {
        `when`(settingsProvider.getMetaSettings()).thenReturn(metaSettings)

        currentProjectProvider.setCurrentProject("123e4567")
        verify(metaSettings).save(MetaKeys.CURRENT_PROJECT_ID, "123e4567")
    }
}

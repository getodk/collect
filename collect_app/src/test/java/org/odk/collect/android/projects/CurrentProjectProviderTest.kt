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
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository

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
    fun `Id of current project should be returned after calling getCurrentProjectId()`() {
        `when`(settingsProvider.getMetaSettings()).thenReturn(metaSettings)
        `when`(metaSettings.getString(MetaKeys.CURRENT_PROJECT_ID)).thenReturn("123e4567")

        assertThat(currentProjectProvider.getCurrentProjectId(), `is`("123e4567"))
    }

    @Test
    fun `A project should be returned after calling getCurrentProject() if there is a project for given id`() {
        `when`(settingsProvider.getMetaSettings()).thenReturn(metaSettings)
        `when`(metaSettings.getString(MetaKeys.CURRENT_PROJECT_ID)).thenReturn("123e4567")
        val project = Project("ProjectX", "X", "#00FF00", "123e4567")
        `when`(projectsRepository.get("123e4567")).thenReturn(project)

        assertThat(currentProjectProvider.getCurrentProject(), `is`(project))
    }

    @Test
    fun `Null should be returned after calling getCurrentProject() if there is no project for given id`() {
        `when`(settingsProvider.getMetaSettings()).thenReturn(metaSettings)
        `when`(metaSettings.getString(MetaKeys.CURRENT_PROJECT_ID)).thenReturn("123e4567")
        `when`(projectsRepository.get("123e4567")).thenReturn(null)

        assertThat(currentProjectProvider.getCurrentProject(), `is`(nullValue()))
    }

    @Test
    fun `save() on meta settings should be called after current project is set`() {
        `when`(settingsProvider.getMetaSettings()).thenReturn(metaSettings)

        currentProjectProvider.setCurrentProject("123e4567")
        verify(metaSettings).save(MetaKeys.CURRENT_PROJECT_ID, "123e4567")
    }
}

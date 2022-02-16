package org.odk.collect.android.activities.viewmodels

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.odk.collect.android.preferences.source.SharedPreferencesSettings
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.keys.ProjectKeys

class SplashScreenViewModelTest {

    private lateinit var generalSettings: SharedPreferencesSettings
    private lateinit var projectsRepository: ProjectsRepository
    private lateinit var splashScreenViewModel: SplashScreenViewModel

    @Before
    fun setup() {
        generalSettings = mock(SharedPreferencesSettings::class.java)
        projectsRepository = mock(ProjectsRepository::class.java)
        splashScreenViewModel = SplashScreenViewModel(generalSettings, projectsRepository)
    }

    @Test
    fun `shouldDisplaySplashScreen() should return false if displaying splash screen is disabled`() {
        `when`(generalSettings.getBoolean(ProjectKeys.KEY_SHOW_SPLASH)).thenReturn(false)
        assertThat(splashScreenViewModel.shouldDisplaySplashScreen, `is`(false))
    }

    @Test
    fun `shouldDisplaySplashScreen() should return true if displaying splash screen is enabled`() {
        `when`(generalSettings.getBoolean(ProjectKeys.KEY_SHOW_SPLASH)).thenReturn(true)
        assertThat(splashScreenViewModel.shouldDisplaySplashScreen, `is`(true))
    }

    @Test
    fun `splashScreenLogoFile should return empty string if no custom logo is specified`() {
        assertThat(splashScreenViewModel.splashScreenLogoFile.name, `is`(""))
    }

    @Test
    fun `splashScreenLogoFile should return file name if custom logo is specified`() {
        `when`(generalSettings.getString(ProjectKeys.KEY_SPLASH_PATH)).thenReturn("blah")
        assertThat(splashScreenViewModel.splashScreenLogoFile.name, `is`("blah"))
    }

    @Test
    fun `shouldFirstLaunchScreenBeDisplayed should return true if there are no projects`() {
        `when`(projectsRepository.getAll()).thenReturn(emptyList())
        assertThat(splashScreenViewModel.shouldFirstLaunchScreenBeDisplayed, `is`(true))
    }

    @Test
    fun `shouldFirstLaunchScreenBeDisplayed should return false if there are saved projects`() {
        `when`(projectsRepository.getAll()).thenReturn(listOf(Project.Saved("123", "Project X", "P", "#cccccc")))
        assertThat(splashScreenViewModel.shouldFirstLaunchScreenBeDisplayed, `is`(false))
    }
}

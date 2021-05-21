package org.odk.collect.android.activities.viewmodels

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.source.SharedPreferencesSettings
import org.odk.collect.android.utilities.AppStateProvider
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository

class SplashScreenViewModelTest {
    private lateinit var generalSettings: SharedPreferencesSettings
    private lateinit var appStateProvider: AppStateProvider
    private lateinit var projectsRepository: ProjectsRepository
    private lateinit var splashScreenViewModel: SplashScreenViewModel

    @Before
    fun setup() {
        generalSettings = mock(SharedPreferencesSettings::class.java)
        appStateProvider = mock(AppStateProvider::class.java)
        projectsRepository = mock(ProjectsRepository::class.java)
        splashScreenViewModel = SplashScreenViewModel(generalSettings, appStateProvider, projectsRepository)
    }

    @Test
    fun `shouldDisplaySplashScreen() should return false if displaying splash screen is disabled`() {
        `when`(generalSettings.getBoolean(GeneralKeys.KEY_SHOW_SPLASH)).thenReturn(false)
        assertThat(splashScreenViewModel.shouldDisplaySplashScreen, `is`(false))
    }

    @Test
    fun `shouldDisplaySplashScreen() should return true if displaying splash screen is enabled`() {
        `when`(generalSettings.getBoolean(GeneralKeys.KEY_SHOW_SPLASH)).thenReturn(true)
        assertThat(splashScreenViewModel.shouldDisplaySplashScreen, `is`(true))
    }

    @Test
    fun `splashScreenLogoFile should return empty string if no custom logo is specified`() {
        assertThat(splashScreenViewModel.splashScreenLogoFile.name, `is`(""))
    }

    @Test
    fun `splashScreenLogoFile should return file name if custom logo is specified`() {
        `when`(generalSettings.getString(GeneralKeys.KEY_SPLASH_PATH)).thenReturn("blah")
        assertThat(splashScreenViewModel.splashScreenLogoFile.name, `is`("blah"))
    }

    @Test
    fun `shouldFirstLaunchScreenBeDisplayed should return true if the app is newly installed`() {
        `when`(appStateProvider.isFreshInstall()).thenReturn(true)
        `when`(projectsRepository.getAll()).thenReturn(emptyList())
        assertThat(splashScreenViewModel.shouldFirstLaunchScreenBeDisplayed, `is`(true))
    }

    @Test
    fun `shouldFirstLaunchScreenBeDisplayed should return true if the app is not newly installed but there are no projects`() {
        `when`(appStateProvider.isFreshInstall()).thenReturn(false)
        `when`(projectsRepository.getAll()).thenReturn(emptyList())
        assertThat(splashScreenViewModel.shouldFirstLaunchScreenBeDisplayed, `is`(true))
    }

    @Test
    fun `shouldFirstLaunchScreenBeDisplayed should return false if the app is not newly installed and there are saved projects`() {
        `when`(appStateProvider.isFreshInstall()).thenReturn(false)
        `when`(projectsRepository.getAll()).thenReturn(listOf(Project.Saved("123", "Project X", "P", "#cccccc")))
        assertThat(splashScreenViewModel.shouldFirstLaunchScreenBeDisplayed, `is`(false))
    }
}

package org.odk.collect.android.activities.viewmodels

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.source.SharedPreferencesSettings

class SplashScreenViewModelTest {
    private lateinit var generalSettings: SharedPreferencesSettings
    private lateinit var metaSettings: SharedPreferencesSettings
    private lateinit var splashScreenViewModel: SplashScreenViewModel

    @Before
    fun setup() {
        generalSettings = Mockito.mock(SharedPreferencesSettings::class.java)
        metaSettings = Mockito.mock(SharedPreferencesSettings::class.java)
        splashScreenViewModel = SplashScreenViewModel(generalSettings, metaSettings)
    }

    @Test
    fun whenSplashScreenDisabled_shouldShouldDisplaySplashScreenReturnFalse() {
        `when`(generalSettings.getBoolean(GeneralKeys.KEY_SHOW_SPLASH)).thenReturn(false)
        assertThat(splashScreenViewModel.shouldDisplaySplashScreen, `is`(false))
    }

    @Test
    fun whenSplashScreenEnabled_shouldShouldDisplaySplashScreenReturnTrue() {
        `when`(generalSettings.getBoolean(GeneralKeys.KEY_SHOW_SPLASH)).thenReturn(true)
        assertThat(splashScreenViewModel.shouldDisplaySplashScreen, `is`(true))
    }

    @Test
    fun whenSplashScreenLogoNotSpecified_shouldReturnEmptyString() {
        assertThat(splashScreenViewModel.splashScreenLogoFile.name, `is`(""))
    }

    @Test
    fun whenSplashScreenLogoSpecified_shouldReturnSavedValue() {
        `when`(generalSettings.getString(GeneralKeys.KEY_SPLASH_PATH)).thenReturn("blah")
        assertThat(splashScreenViewModel.splashScreenLogoFile.name, `is`("blah"))
    }

    @Test
    fun whenAppStartedForTheFirstTime_shouldShouldFirstLaunchDialogBeDisplayedTrue() {
        assertThat(splashScreenViewModel.shouldFirstLaunchDialogBeDisplayed(), `is`(true))
        verify(metaSettings).save(MetaKeys.KEY_FIRST_LAUNCH, false)
    }

    @Test
    fun whenAppStartedNotForTheFirstTime_shouldShouldFirstLaunchDialogBeDisplayedFalse() {
        `when`(metaSettings.contains(MetaKeys.KEY_FIRST_LAUNCH)).thenReturn(true)
        assertThat(splashScreenViewModel.shouldFirstLaunchDialogBeDisplayed(), `is`(false))
    }
}

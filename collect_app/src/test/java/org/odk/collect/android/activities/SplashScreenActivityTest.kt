package org.odk.collect.android.activities

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.activities.viewmodels.SplashScreenViewModel
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.rules.MainCoroutineScopeRule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.androidtest.ActivityScenarioLauncherRule
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider

@RunWith(AndroidJUnit4::class)
class SplashScreenActivityTest {

    @ExperimentalCoroutinesApi
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private val splashScreenViewModel = mock<SplashScreenViewModel> {}

    private val currentProjectProvider = mock<CurrentProjectProvider> {}

    @get:Rule
    val launcherRule = ActivityScenarioLauncherRule()

    @Before
    fun setup() {

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesSplashScreenViewModel(
                settingsProvider: SettingsProvider,
                projectsRepository: ProjectsRepository
            ): SplashScreenViewModel.Factory {
                return object : SplashScreenViewModel.Factory(
                    settingsProvider.getUnprotectedSettings(),
                    projectsRepository
                ) {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return splashScreenViewModel as T
                    }
                }
            }

            override fun providesCurrentProjectProvider(settingsProvider: SettingsProvider?, projectsRepository: ProjectsRepository?): CurrentProjectProvider {
                return currentProjectProvider
            }
        })
    }

    @Test
    fun `The Splash screen should be displayed with the default logo if it's enabled and no other logo is set`() {
        whenever(splashScreenViewModel.shouldDisplaySplashScreen).thenReturn(true)
        whenever(splashScreenViewModel.doesLogoFileExist).thenReturn(false)

        val scenario = launcherRule.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            assertThat(activity.findViewById<View>(R.id.splash_default).visibility, `is`(View.VISIBLE))
            assertThat(activity.findViewById<View>(R.id.splash).visibility, `is`(View.GONE))
        }
    }

    @Test
    fun `The Splash screen should be displayed with custom logo if it's enabled and custom logo is set`() {
        whenever(splashScreenViewModel.shouldDisplaySplashScreen).thenReturn(true)
        whenever(splashScreenViewModel.doesLogoFileExist).thenReturn(true)

        val scenario = launcherRule.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            assertThat(activity.findViewById<View>(R.id.splash_default).visibility, `is`(View.GONE))
            assertThat(activity.findViewById<View>(R.id.splash).visibility, `is`(View.VISIBLE))
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `The main menu should be displayed automatically after 2s if the Splash screen is enabled and the app is not newly installed`() = coroutineScope.runBlockingTest {
        whenever(splashScreenViewModel.shouldDisplaySplashScreen).thenReturn(true)
        whenever(splashScreenViewModel.shouldFirstLaunchScreenBeDisplayed).thenReturn(false)

        Intents.init()

        val scenario = launcherRule.launch(SplashScreenActivity::class.java)
        advanceTimeBy(1000)
        assertThat(scenario.state, `is`(Lifecycle.State.RESUMED))
        assertThat(Intents.getIntents().isEmpty(), `is`(true))
        advanceTimeBy(1000)
        Intents.intended(hasComponent(MainMenuActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun `The main menu should be displayed immediately if the splash screen is disabled and the app is not newly installed`() {
        whenever(splashScreenViewModel.shouldDisplaySplashScreen).thenReturn(false)
        whenever(splashScreenViewModel.shouldFirstLaunchScreenBeDisplayed).thenReturn(false)

        Intents.init()
        val scenario = launcherRule.launch(SplashScreenActivity::class.java)
        assertThat(scenario.state, `is`(Lifecycle.State.DESTROYED))
        Intents.intended(hasComponent(MainMenuActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun `The First Launch Screen should be displayed immediately if the app is newly installed`() {
        whenever(splashScreenViewModel.shouldDisplaySplashScreen).thenReturn(false)
        whenever(splashScreenViewModel.shouldFirstLaunchScreenBeDisplayed).thenReturn(true)

        Intents.init()
        val scenario = launcherRule.launch(SplashScreenActivity::class.java)
        assertThat(scenario.state, `is`(Lifecycle.State.DESTROYED))
        Intents.intended(hasComponent(FirstLaunchActivity::class.java.name))
        Intents.release()
    }
}

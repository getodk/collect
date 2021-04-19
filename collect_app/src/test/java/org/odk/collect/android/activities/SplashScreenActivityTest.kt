package org.odk.collect.android.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.SavedStateRegistryOwner
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.odk.collect.android.R
import org.odk.collect.android.activities.viewmodels.SplashScreenViewModel
import org.odk.collect.android.fragments.dialogs.FirstLaunchDialog
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.source.Settings
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.support.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class SplashScreenActivityTest {

    private lateinit var splashScreenViewModel: SplashScreenViewModel

    @Before
    fun setup() {
        splashScreenViewModel = spy(SplashScreenViewModel(mock(Settings::class.java), mock(Settings::class.java)))

        RobolectricHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesSplashScreenViewModelFactoryFactory(settingsProvider: SettingsProvider): SplashScreenViewModel.FactoryFactory {
                return object : SplashScreenViewModel.FactoryFactory {
                    override fun create(owner: SavedStateRegistryOwner, defaultArgs: Bundle?): ViewModelProvider.Factory {
                        return object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                            override fun <T : ViewModel?> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
                                return splashScreenViewModel as T
                            }
                        }
                    }
                }
            }
        })
    }

    @Test
    fun `The Fist Launch Screen should be displayed if the app is newly installed`() {
        doReturn(true).`when`(splashScreenViewModel).shouldFirstLaunchDialogBeDisplayed()

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            assertThat(RobolectricHelpers.getFragmentByClass(activity.supportFragmentManager, FirstLaunchDialog::class.java), `is`(notNullValue()))
        }
    }

    @Test
    fun `The Fist Launch Screen should not be displayed if the app is not newly installed`() {
        doReturn(false).`when`(splashScreenViewModel).shouldFirstLaunchDialogBeDisplayed()
        doReturn(true).`when`(splashScreenViewModel).shouldDisplaySplashScreen
        doReturn(false).`when`(splashScreenViewModel).doesLogoFileExist

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            assertThat(RobolectricHelpers.getFragmentByClass(activity.supportFragmentManager, FirstLaunchDialog::class.java), `is`(nullValue()))
        }
    }

    @Test
    fun `The Splash screen should be displayed with the default logo if it's enabled and no other logo is set`() {
        doReturn(false).`when`(splashScreenViewModel).shouldFirstLaunchDialogBeDisplayed()
        doReturn(true).`when`(splashScreenViewModel).shouldDisplaySplashScreen
        doReturn(false).`when`(splashScreenViewModel).doesLogoFileExist

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            assertThat(activity.findViewById<View>(R.id.splash_default).visibility, `is`(View.VISIBLE))
            assertThat(activity.findViewById<View>(R.id.splash).visibility, `is`(View.GONE))
        }
    }

    @Test
    fun `The Splash screen should be displayed with custom logo if it's enabled and custom logo is set`() {
        doReturn(false).`when`(splashScreenViewModel).shouldFirstLaunchDialogBeDisplayed()
        doReturn(true).`when`(splashScreenViewModel).shouldDisplaySplashScreen
        doReturn(true).`when`(splashScreenViewModel).doesLogoFileExist
        doReturn(null).`when`(splashScreenViewModel).scaledSplashScreenLogoBitmap
        doReturn(null).`when`(splashScreenViewModel).splashScreenLogoFile

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            assertThat(activity.findViewById<View>(R.id.splash_default).visibility, `is`(View.GONE))
            assertThat(activity.findViewById<View>(R.id.splash).visibility, `is`(View.VISIBLE))
        }
    }

    @Test
    fun `The main menu should be displayed automatically after 2s if the Splash screen is enabled and the app is not newly installed`() {
        doReturn(false).`when`(splashScreenViewModel).shouldFirstLaunchDialogBeDisplayed()
        doReturn(true).`when`(splashScreenViewModel).shouldDisplaySplashScreen
        doReturn(false).`when`(splashScreenViewModel).doesLogoFileExist

        Intents.init()

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            activity.lifecycleScope.launch {
                delay(2000)
                assertThat(scenario.state, `is`(Lifecycle.State.DESTROYED))
                Intents.intended(hasComponent(MainMenuActivity::class.java.name))
            }
        }

        Intents.release()
    }

    @Test
    fun `The main menu should be displayed immediately if the splash screen is disabled and the app is not newly installed`() {
        doReturn(false).`when`(splashScreenViewModel).shouldFirstLaunchDialogBeDisplayed()
        doReturn(false).`when`(splashScreenViewModel).shouldDisplaySplashScreen

        Intents.init()
        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        assertThat(scenario.state, `is`(Lifecycle.State.DESTROYED))
        Intents.intended(hasComponent(MainMenuActivity::class.java.name))
        Intents.release()
    }
}

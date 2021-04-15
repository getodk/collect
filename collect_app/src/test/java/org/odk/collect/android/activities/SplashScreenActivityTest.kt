package org.odk.collect.android.activities

import android.os.Bundle
import android.view.View
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.savedstate.SavedStateRegistryOwner
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
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

    private val generalSettings: Settings = mock(Settings::class.java)
    private val metaSettings: Settings = mock(Settings::class.java)
    private lateinit var splashScreenViewModel: SplashScreenViewModel

    @Before
    fun setup() {
        splashScreenViewModel = spy(SplashScreenViewModel(generalSettings, metaSettings))

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
    fun whenAppLaunchedForTheFistTime_showFirstLaunchScreen() {
        doReturn(true).`when`(splashScreenViewModel).shouldFirstLaunchDialogBeDisplayed()

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            MatcherAssert.assertThat(activity.supportFragmentManager.findFragmentByTag(FirstLaunchDialog::class.qualifiedName), Matchers.`is`(notNullValue()))
        }
    }

    @Test
    fun whenAppLaunchedNotForTheFistTime_doNotShowFirstLaunchScreen() {
        doReturn(false).`when`(splashScreenViewModel).shouldFirstLaunchDialogBeDisplayed()
        doReturn(true).`when`(splashScreenViewModel).shouldDisplaySplashScreen
        doReturn(false).`when`(splashScreenViewModel).doesLogoFileExist

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            MatcherAssert.assertThat(activity.supportFragmentManager.findFragmentByTag(FirstLaunchDialog::class.qualifiedName), Matchers.`is`(nullValue()))
        }
    }

    @Test
    fun whenShowSplashScreenEnabledWithDefaultLogo_showSplashScreenWithDefaultLogo() {
        doReturn(false).`when`(splashScreenViewModel).shouldFirstLaunchDialogBeDisplayed()
        doReturn(true).`when`(splashScreenViewModel).shouldDisplaySplashScreen
        doReturn(false).`when`(splashScreenViewModel).doesLogoFileExist

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            MatcherAssert.assertThat(activity.findViewById<View>(R.id.splash_default).visibility, Matchers.`is`(View.VISIBLE))
            MatcherAssert.assertThat(activity.findViewById<View>(R.id.splash).visibility, Matchers.`is`(View.GONE))
        }
    }

    @Test
    fun whenShowSplashScreenEnabledWithCustomLogo_showSplashScreenWithCustomLogo() {
        doReturn(false).`when`(splashScreenViewModel).shouldFirstLaunchDialogBeDisplayed()
        doReturn(true).`when`(splashScreenViewModel).shouldDisplaySplashScreen
        doReturn(true).`when`(splashScreenViewModel).doesLogoFileExist
        doReturn(null).`when`(splashScreenViewModel).scaledSplashScreenLogoBitmap
        doReturn(null).`when`(splashScreenViewModel).splashScreenLogoFile

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            MatcherAssert.assertThat(activity.findViewById<View>(R.id.splash_default).visibility, Matchers.`is`(View.GONE))
            MatcherAssert.assertThat(activity.findViewById<View>(R.id.splash).visibility, Matchers.`is`(View.VISIBLE))
        }
    }

    @Test
    fun whenAppLaunchedNotForTheFistTimeAndSplashScreenEnabled_showMainMenuActivityAutomaticallyAfterTwoSeconds() {
        doReturn(false).`when`(splashScreenViewModel).shouldFirstLaunchDialogBeDisplayed()
        doReturn(true).`when`(splashScreenViewModel).shouldDisplaySplashScreen
        doReturn(false).`when`(splashScreenViewModel).doesLogoFileExist

        Intents.init()

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            activity.lifecycleScope.launch {
                delay(2000)
                Intents.intended(IntentMatchers.hasComponent(MainMenuActivity::class.java.name))
            }
        }

        Intents.release()
    }
}

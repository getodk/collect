package org.odk.collect.android.activities

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.Matchers.notNullValue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.R
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.activities.viewmodels.SplashScreenViewModel
import org.odk.collect.android.fragments.dialogs.FirstLaunchDialog
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.keys.GeneralKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.projects.ProjectImporter
import org.odk.collect.android.rules.MainCoroutineScopeRule
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.AppStateProvider
import org.odk.collect.projects.AddProjectDialog
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class SplashScreenActivityTest {
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private val splashScreenViewModel = mock<SplashScreenViewModel> {}

    private val currentProjectProvider = mock<CurrentProjectProvider> {}

    private val projectImporter = mock<ProjectImporter> {}

    @Before
    fun setup() {

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesSplashScreenViewModel(settingsProvider: SettingsProvider, appStateProvider: AppStateProvider, projectsRepository: ProjectsRepository): SplashScreenViewModel.Factory {
                return object : SplashScreenViewModel.Factory(settingsProvider.getGeneralSettings(), appStateProvider, projectsRepository) {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return splashScreenViewModel as T
                    }
                }
            }

            override fun providesCurrentProjectProvider(settingsProvider: SettingsProvider?, projectsRepository: ProjectsRepository?): CurrentProjectProvider {
                return currentProjectProvider
            }

            override fun providesProjectImporter(projectsRepository: ProjectsRepository, storagePathProvider: StoragePathProvider, context: Context, settingsProvider: SettingsProvider): ProjectImporter? {
                return projectImporter
            }
        })
    }

    @Test
    fun `SplashScreenActivity should implement AddProjectDialogListener`() {
        doReturn(true).`when`(splashScreenViewModel).shouldFirstLaunchScreenBeDisplayed

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            assertThat(activity is AddProjectDialog.AddProjectDialogListener, `is`(true))
        }
    }

    @Test
    fun `The main menu should be displayed when onProjectAdded() is called`() {
        doReturn(true).`when`(splashScreenViewModel).shouldFirstLaunchScreenBeDisplayed

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            val newProject = Project.Saved("456", "Project2", "V", "#cccccc")

            Intents.init()
            activity.onProjectAdded(newProject, "https://my-project.com", "John", "1234")
            Intents.intended(hasComponent(MainMenuActivity::class.java.name))
            Intents.release()
        }
    }

    @Test
    fun `New project should be set up when onProjectAdded() is called`() {
        doReturn(true).`when`(splashScreenViewModel).shouldFirstLaunchScreenBeDisplayed

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            val newProject = Project.Saved("456", "Project2", "V", "#cccccc")
            activity.onProjectAdded(newProject, "https://my-project.com", "John", "1234")

            verify(projectImporter).setupProject(newProject)
        }
    }

    @Test
    fun `Current project should be set when onProjectAdded() is called`() {
        doReturn(true).`when`(splashScreenViewModel).shouldFirstLaunchScreenBeDisplayed

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            val newProject = Project.Saved("456", "Project2", "V", "#cccccc")
            activity.onProjectAdded(newProject, "https://my-project.com", "John", "1234")

            verify(currentProjectProvider).setCurrentProject("456")
        }
    }

    @Test
    fun `New settings should be saved when onProjectAdded() is called`() {
        doReturn(true).`when`(splashScreenViewModel).shouldFirstLaunchScreenBeDisplayed

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            val newProject = Project.Saved("456", "Project2", "V", "#cccccc")

            activity.onProjectAdded(newProject, "https://my-project.com", "John", "1234")

            val generalSettings = TestSettingsProvider.getGeneralSettings("456")

            assertThat(generalSettings.getString(GeneralKeys.KEY_SERVER_URL), `is`("https://my-project.com"))
            assertThat(generalSettings.getString(GeneralKeys.KEY_USERNAME), `is`("John"))
            assertThat(generalSettings.getString(GeneralKeys.KEY_PASSWORD), `is`("1234"))
        }
    }

    @Test
    fun `The First Launch Screen should be displayed if the app is newly installed`() {
        doReturn(true).`when`(splashScreenViewModel).shouldFirstLaunchScreenBeDisplayed

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            assertThat(RobolectricHelpers.getFragmentByClass(activity.supportFragmentManager, FirstLaunchDialog::class.java), `is`(notNullValue()))
        }
    }

    @Test
    fun `The First Launch Screen should not be displayed if the app is not newly installed`() {
        doReturn(false).`when`(splashScreenViewModel).shouldFirstLaunchScreenBeDisplayed
        doReturn(true).`when`(splashScreenViewModel).shouldDisplaySplashScreen
        doReturn(false).`when`(splashScreenViewModel).doesLogoFileExist

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        scenario.onActivity { activity: SplashScreenActivity ->
            assertThat(RobolectricHelpers.getFragmentByClass(activity.supportFragmentManager, FirstLaunchDialog::class.java), `is`(nullValue()))
        }
    }

    @Test
    fun `The Splash screen should be displayed with the default logo if it's enabled and no other logo is set`() {
        doReturn(false).`when`(splashScreenViewModel).shouldFirstLaunchScreenBeDisplayed
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
        doReturn(false).`when`(splashScreenViewModel).shouldFirstLaunchScreenBeDisplayed
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
    fun `The main menu should be displayed automatically after 2s if the Splash screen is enabled and the app is not newly installed`() = coroutineScope.runBlockingTest {
        doReturn(false).`when`(splashScreenViewModel).shouldFirstLaunchScreenBeDisplayed
        doReturn(true).`when`(splashScreenViewModel).shouldDisplaySplashScreen
        doReturn(false).`when`(splashScreenViewModel).doesLogoFileExist

        Intents.init()

        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        advanceTimeBy(1000)
        assertThat(scenario.state, `is`(Lifecycle.State.RESUMED))
        assertThat(Intents.getIntents().isEmpty(), `is`(true))
        advanceTimeBy(1000)
        Intents.intended(hasComponent(MainMenuActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun `The main menu should be displayed immediately if the splash screen is disabled and the app is not newly installed`() {
        doReturn(false).`when`(splashScreenViewModel).shouldFirstLaunchScreenBeDisplayed
        doReturn(false).`when`(splashScreenViewModel).shouldDisplaySplashScreen

        Intents.init()
        val scenario = ActivityScenario.launch(SplashScreenActivity::class.java)
        assertThat(scenario.state, `is`(Lifecycle.State.DESTROYED))
        Intents.intended(hasComponent(MainMenuActivity::class.java.name))
        Intents.release()
    }
}

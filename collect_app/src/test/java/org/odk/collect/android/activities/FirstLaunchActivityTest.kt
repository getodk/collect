package org.odk.collect.android.activities

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.odk.collect.android.R
import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.projects.AutomaticProjectCreatorDialog
import org.odk.collect.android.projects.ManualProjectCreatorDialog
import org.odk.collect.android.projects.ProjectImporter
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.TranslationHandler.getString
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.projects.ProjectsRepository

@RunWith(AndroidJUnit4::class)
class FirstLaunchActivityTest {
    @Test
    fun `The main menu should be started after clicking on the 'Try a demo' link`() {
        val scenario = ActivityScenario.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            Intents.init()
            onView(withText(R.string.try_demo)).perform(click())
            intended(hasComponent(MainMenuActivity::class.java.name))
            Intents.release()
        }
    }

    @Test
    fun `Importing default project should be triggered after clicking on the 'Try a demo' link`() {
        val projectImporter = mock(ProjectImporter::class.java)
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesProjectImporter(
                projectsRepository: ProjectsRepository,
                storagePathProvider: StoragePathProvider
            ): ProjectImporter? {
                return projectImporter
            }
        })

        val scenario = ActivityScenario.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            onView(withText(R.string.try_demo)).perform(click())
            verify(projectImporter).importDemoProject()
        }
    }

    @Test
    fun `The AutomaticProjectCreatorDialog should be displayed after clicking on the 'Configure with QR code' button`() {
        val scenario = ActivityScenario.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            onView(withText(R.string.configure_with_qr_code)).perform(click())
            assertThat(it.supportFragmentManager.findFragmentByTag(AutomaticProjectCreatorDialog::class.java.name), `is`(notNullValue()))
        }
    }

    @Test
    fun `The ManualProjectCreatorDialog should be displayed after clicking on the 'Configure manually' button`() {
        val scenario = ActivityScenario.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            onView(withText(R.string.configure_manually)).perform(click())
            assertThat(it.supportFragmentManager.findFragmentByTag(ManualProjectCreatorDialog::class.java.name), `is`(notNullValue()))
        }
    }

    @Test
    fun `The ODK logo should be displayed`() {
        val scenario = ActivityScenario.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            onView(withId(R.id.logo)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun `The app name with its version should be displayed`() {
        val versionInformation = mock(VersionInformation::class.java)
        `when`(versionInformation.versionToDisplay).thenReturn("vfake")
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesVersionInformation(): VersionInformation {
                return versionInformation
            }
        })

        val scenario = ActivityScenario.launch(FirstLaunchActivity::class.java)
        scenario.onActivity {
            verify(versionInformation).versionToDisplay
            onView(withText(getString(ApplicationProvider.getApplicationContext<Collect>(), R.string.app_name) + " vfake")).check(matches(isDisplayed()))
        }
    }
}

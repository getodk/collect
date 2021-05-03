package org.odk.collect.android.fragments.dialogs

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.odk.collect.android.R
import org.odk.collect.android.activities.MainMenuActivity
import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.ProjectImporter
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.utilities.TranslationHandler.getString
import org.odk.collect.android.version.VersionInformation
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class FirstLaunchDialogTest {

    @Test
    fun `The dialog should not be dismissed after clicking on a device back button`() {
        val scenario = RobolectricHelpers.launchDialogFragment(FirstLaunchDialog::class.java, R.style.Theme_Collect_Light)
        scenario.onFragment {
            MatcherAssert.assertThat(it.isVisible, `is`(true))
            onView(isRoot()).perform(pressBack())
            MatcherAssert.assertThat(it.isVisible, `is`(true))
        }
    }

    @Test
    fun `Importing default project should be triggered after clicking on the 'Configure later' button`() {
        val projectImporter = mock(ProjectImporter::class.java)
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesProjectImporter(projectsRepository: ProjectsRepository, settingsProvider: SettingsProvider): ProjectImporter {
                return projectImporter
            }
        })

        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(FirstLaunchDialog::class.java, R.style.Theme_Collect_Light)
        scenario.onFragment {
            onView(withText(R.string.configure_later)).perform(click())
            verify(projectImporter).importDemoProject()
        }
    }

    @Test
    fun `The main menu should be started after clicking on the 'Configure later' button`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(FirstLaunchDialog::class.java, R.style.Theme_Collect_Light)
        scenario.onFragment {
            Intents.init()
            onView(withText(R.string.configure_later)).perform(click())
            intended(hasComponent(MainMenuActivity::class.java.name))
            Intents.release()
        }
    }

    @Test
    fun `The FirstLaunchDialog should not be dismissed after clicking on the 'Configure manually' button`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(FirstLaunchDialog::class.java, R.style.Theme_Collect_Light)
        scenario.onFragment {
            onView(withText(R.string.configure_manually)).perform(click())
            assertThat(it.activity!!.supportFragmentManager.findFragmentByTag(it.tag), `is`(notNullValue()))
        }
    }

    @Test
    fun `The app logo should be displayed`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(FirstLaunchDialog::class.java, R.style.Theme_Collect_Light)
        scenario.onFragment {
            onView(withId(R.id.logo)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun `The app name with its version should be displayed`() {
        val versionInformation = mock(VersionInformation::class.java)
        `when`(versionInformation.versionToDisplay).thenReturn("v30.1.1")
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesVersionInformation(): VersionInformation {
                return versionInformation
            }
        })

        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(FirstLaunchDialog::class.java, R.style.Theme_Collect_Light)
        scenario.onFragment {
            verify(versionInformation).versionToDisplay
            onView(withText(getString(ApplicationProvider.getApplicationContext<Collect>(), R.string.app_name, "v1.30.1.1"))).check(matches(isDisplayed()))
        }
    }
}

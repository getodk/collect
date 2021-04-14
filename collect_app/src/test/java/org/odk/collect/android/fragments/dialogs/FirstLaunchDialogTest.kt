package org.odk.collect.android.fragments.dialogs

import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers
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
import org.odk.collect.android.projects.ProjectsRepository
import org.odk.collect.android.support.RobolectricHelpers
import org.odk.collect.android.utilities.TranslationHandler.getString
import org.odk.collect.android.version.VersionInformation

@RunWith(AndroidJUnit4::class)
class FirstLaunchDialogTest {

    @Test
    fun clickingDeviceBackButton_shouldNotDismissDialog() {
        val scenario = RobolectricHelpers.launchDialogFragment(FirstLaunchDialog::class.java)
        scenario.onFragment { f: FirstLaunchDialog ->
            val dialog = f.dialog
            MatcherAssert.assertThat(dialog!!.isShowing, CoreMatchers.`is`(true))
            Espresso.onView(ViewMatchers.isRoot()).perform(ViewActions.pressBack())
            MatcherAssert.assertThat(dialog.isShowing, CoreMatchers.`is`(true))
        }
    }

    @Test
    fun clickingConfigureLater_shouldTriggerImportingDefaultProject() {
        val projectImporter = mock(ProjectImporter::class.java)
        RobolectricHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesProjectImporter(projectsRepository: ProjectsRepository, settingsProvider: SettingsProvider): ProjectImporter {
                return projectImporter
            }
        })

        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(FirstLaunchDialog::class.java)
        scenario.onFragment {
            Espresso.onView(withText(getString(ApplicationProvider.getApplicationContext<Collect>(), R.string.configure_later))).perform(click())
            verify(projectImporter).importDemoProject()
        }
    }

    @Test
    fun clickingConfigureLater_shouldStartMainMenu() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(FirstLaunchDialog::class.java)
        scenario.onFragment {
            Intents.init()
            Espresso.onView(withText(getString(ApplicationProvider.getApplicationContext<Collect>(), R.string.configure_later))).perform(click())
            intended(hasComponent(MainMenuActivity::class.java.name))
        }
    }

    @Test
    fun appNameWithVersionShouldBeDisplayed() {
        val versionInformation = mock(VersionInformation::class.java)
        `when`(versionInformation.versionToDisplay).thenReturn("v30.1.1")
        RobolectricHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesVersionInformation(): VersionInformation {
                return versionInformation
            }
        })

        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(FirstLaunchDialog::class.java)
        scenario.onFragment {
            verify(versionInformation).versionToDisplay
            Espresso.onView(withText(getString(ApplicationProvider.getApplicationContext<Collect>(), R.string.app_name, "v1.30.1.1"))).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }
}

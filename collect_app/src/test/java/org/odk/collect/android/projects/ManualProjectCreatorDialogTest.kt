package org.odk.collect.android.projects

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.R
import org.odk.collect.android.activities.MainMenuActivity
import org.odk.collect.android.configure.SettingsImporter
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.Matchers.isPasswordHidden
import org.odk.collect.androidshared.system.IntentLauncher
import org.odk.collect.fragmentstest.DialogFragmentTest
import org.odk.collect.fragmentstest.DialogFragmentTest.onViewInDialog
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.testshared.ErrorIntentLauncher
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
class ManualProjectCreatorDialogTest {

    @Test
    fun `Password should be protected`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ManualProjectCreatorDialog::class.java)
        scenario.onFragment {
            onViewInDialog(withHint(R.string.server_url)).perform(replaceText("123456789"))
            onViewInDialog(withHint(R.string.server_url)).check(matches(not(isPasswordHidden())))

            onViewInDialog(withHint(R.string.username)).perform(replaceText("123456789"))
            onViewInDialog(withHint(R.string.username)).check(matches(not(isPasswordHidden())))

            onViewInDialog(withHint(R.string.password)).perform(replaceText("123456789"))
            onViewInDialog(withHint(R.string.password)).check(matches(isPasswordHidden()))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on the 'Cancel' button`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ManualProjectCreatorDialog::class.java)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onViewInDialog(withText(R.string.cancel)).perform(click())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on a device back button`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ManualProjectCreatorDialog::class.java)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(isRoot()).perform(pressBack())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `The 'Add' button should be disabled when url is blank`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ManualProjectCreatorDialog::class.java)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))

            onViewInDialog(withText(R.string.add)).perform(click())
            assertThat(it.isVisible, `is`(true))

            onViewInDialog(withHint(R.string.server_url)).perform(replaceText(" "))
            onViewInDialog(withText(R.string.add)).perform(click())
            assertThat(it.isVisible, `is`(true))
        }
    }

    @Test
    fun `When URL has no protocol, a toast is displayed`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ManualProjectCreatorDialog::class.java)
        scenario.onFragment {
            onViewInDialog(withHint(R.string.server_url)).perform(replaceText("demo.getodk.org"))
            onViewInDialog(withText(R.string.add)).perform(click())
            assertThat(it.isVisible, `is`(true))

            val toastText = ShadowToast.getTextOfLatestToast()
            assertThat(toastText, `is`(it.getString(R.string.url_error)))
        }
    }

    @Test
    fun `Server project creation should be triggered after clicking on the 'Add' button`() {
        val projectCreator = mock<ProjectCreator> {}
        val currentProjectProvider = mock<CurrentProjectProvider> {
            on { getCurrentProject() } doReturn Project.DEMO_PROJECT
        }

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesProjectCreator(
                projectsRepository: ProjectsRepository,
                currentProjectProvider: CurrentProjectProvider,
                settingsImporter: SettingsImporter,
                context: Context
            ): ProjectCreator? {
                return projectCreator
            }

            override fun providesCurrentProjectProvider(
                settingsProvider: SettingsProvider,
                projectsRepository: ProjectsRepository
            ): CurrentProjectProvider {
                return currentProjectProvider
            }
        })

        val scenario = DialogFragmentTest.launchDialogFragment(ManualProjectCreatorDialog::class.java)
        scenario.onFragment {
            onViewInDialog(withHint(R.string.server_url)).perform(replaceText("https://my-server.com"))
            onViewInDialog(withHint(R.string.username)).perform(replaceText("adam"))
            onViewInDialog(withHint(R.string.password)).perform(replaceText("1234"))

            onViewInDialog(withText(R.string.add)).perform(click())
            verify(projectCreator).createNewProject("{\"general\":{\"server_url\":\"https:\\/\\/my-server.com\",\"username\":\"adam\",\"password\":\"1234\"},\"admin\":{},\"project\":{}}")
        }
    }

    @Test
    fun `Server project creation goes to main menu`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ManualProjectCreatorDialog::class.java)
        scenario.onFragment {
            onViewInDialog(withHint(R.string.server_url)).perform(replaceText("https://my-server.com"))

            Intents.init()
            onViewInDialog(withText(R.string.add)).perform(click())
            Intents.intended(IntentMatchers.hasComponent(MainMenuActivity::class.java.name))
            Intents.release()
        }
    }

    @Test
    fun `If activity to choose google account is not found the app should not crash`() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesIntentLauncher(): IntentLauncher {
                return ErrorIntentLauncher()
            }
        })

        DialogFragmentTest.launchDialogFragment(ManualProjectCreatorDialog::class.java)
        onViewInDialog(withText(R.string.gdrive_configure)).perform(scrollTo(), click())
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertThat(ShadowToast.getTextOfLatestToast(), `is`(context.getString(R.string.activity_not_found, context.getString(R.string.choose_account))))
    }
}

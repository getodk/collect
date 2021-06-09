package org.odk.collect.android.projects

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.R
import org.odk.collect.android.configure.SettingsImporter
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.Matchers.isPasswordHidden
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class ManualProjectCreatorDialogTest {

    @Test
    fun `Password should be protected`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(ManualProjectCreatorDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            onView(withHint(R.string.server_url)).perform(replaceText("123456789"))
            onView(withHint(R.string.server_url)).check(matches(not(isPasswordHidden())))

            onView(withHint(R.string.username)).perform(replaceText("123456789"))
            onView(withHint(R.string.username)).check(matches(not(isPasswordHidden())))

            onView(withHint(R.string.password)).perform(replaceText("123456789"))
            onView(withHint(R.string.password)).check(matches(isPasswordHidden()))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on the 'Cancel' button`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(ManualProjectCreatorDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(withText(R.string.cancel)).perform(click())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on a device back button`() {
        val scenario = RobolectricHelpers.launchDialogFragment(ManualProjectCreatorDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(isRoot()).perform(pressBack())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking the 'Add' button`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(ManualProjectCreatorDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(withHint(R.string.server_url)).perform(replaceText("https://my-server.com"))
            onView(withText(R.string.add)).perform(click())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `The 'Add' button should be disabled when url is blank`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(ManualProjectCreatorDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))

            onView(withText(R.string.add)).perform(click())
            assertThat(it.isVisible, `is`(true))

            onView(withHint(R.string.server_url)).perform(replaceText(" "))
            onView(withText(R.string.add)).perform(click())
            assertThat(it.isVisible, `is`(true))
        }
    }

    @Test
    fun `Project creation should be triggered after clicking on the 'Add' button`() {
        val projectCreator = mock<ProjectCreator> {}

        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesProjectCreator(
                projectImporter: ProjectImporter?,
                projectsRepository: ProjectsRepository?,
                currentProjectProvider: CurrentProjectProvider?,
                settingsImporter: SettingsImporter?
            ): ProjectCreator {
                return projectCreator
            }
        })

        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(ManualProjectCreatorDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            onView(withHint(R.string.server_url)).perform(replaceText("https://my-server.com"))
            onView(withHint(R.string.username)).perform(replaceText("adam"))
            onView(withHint(R.string.password)).perform(replaceText("1234"))

            onView(withText(R.string.add)).perform(click())
            verify(projectCreator).createNewProject("{\"general\":{\"server_url\":\"https:\\/\\/my-server.com\",\"username\":\"adam\",\"password\":\"1234\"},\"admin\":{}}")
        }
    }
}

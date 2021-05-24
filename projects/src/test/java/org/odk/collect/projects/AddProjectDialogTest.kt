package org.odk.collect.projects

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class AddProjectDialogTest {

    @Test
    fun `The dialog should be dismissed after clicking on the 'Cancel' button`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(withText(R.string.cancel)).perform(click())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on a device back button`() {
        val scenario = RobolectricHelpers.launchDialogFragment(AddProjectDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(isRoot()).perform(pressBack())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking the 'Add' button`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(withText(R.string.add)).perform(click())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `NewProject object should be passed to the listener after adding a new project`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java, R.style.Theme_MaterialComponents)

        val listener = mock(AddProjectDialog.AddProjectDialogListener::class.java)

        scenario.onFragment {
            it.setAddProjectDialogListener(listener)

            onView(withHint(R.string.server_url)).perform(scrollTo(), typeText("my-server.com"))
            onView(withHint(R.string.username)).perform(scrollTo(), typeText("Adam"))
            onView(withHint(R.string.password)).perform(scrollTo(), typeText("1234"))
            onView(withHint(R.string.project_name)).perform(scrollTo(), typeText("ProjectX"))
            onView(withHint(R.string.project_icon)).perform(scrollTo(), typeText("X"))
            onView(withHint(R.string.project_color)).perform(scrollTo(), typeText("#cccccc"))

            onView(withText(R.string.add)).perform(click())

            verify(listener).onProjectAdded(Project("my-server.com", "Adam", "1234", "ProjectX", "X", "#cccccc"))
        }
    }

    @Test
    fun `Only one character should be accepted as a project icon`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            onView(withHint(R.string.project_icon)).perform(scrollTo(), typeText("XYZ"))
            onView(allOf(withHint(R.string.project_icon), withText("X"))).check(matches(isDisplayed()))
        }
    }

    @Test
    fun `Only one emoji should be accepted as a project icon`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            onView(withHint(R.string.project_icon)).perform(scrollTo(), replaceText("\uD83D\uDC22"))
            onView(allOf(withHint(R.string.project_icon), withText("\uD83D\uDC22"))).check(matches(isDisplayed()))
        }
    }

    @Test
    fun `Project name should be generated after passing url`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            onView(withHint(R.string.server_url)).perform(scrollTo(), replaceText("https://my-server.com"))
            onView(allOf(withHint(R.string.project_name), withText("my-server"))).perform(scrollTo()).check(matches(isDisplayed()))

            onView(withHint(R.string.server_url)).perform(scrollTo(), replaceText("https://second-server.com"))
            onView(allOf(withHint(R.string.project_name), withText("second-server"))).perform(scrollTo()).check(matches(isDisplayed()))

            scenario.recreate()

            onView(withHint(R.string.server_url)).perform(scrollTo(), replaceText("https://third-server.com"))
            onView(allOf(withHint(R.string.project_name), withText("third-server"))).perform(scrollTo()).check(matches(isDisplayed()))
        }
    }

    @Test
    fun `Project icon should be generated after passing url`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            onView(withHint(R.string.server_url)).perform(scrollTo(), replaceText("https://my-server.com"))
            onView(allOf(withHint(R.string.project_icon), withText("M"))).perform(scrollTo()).check(matches(isDisplayed()))

            onView(withHint(R.string.server_url)).perform(scrollTo(), replaceText("https://second-server.com"))
            onView(allOf(withHint(R.string.project_icon), withText("S"))).perform(scrollTo()).check(matches(isDisplayed()))

            scenario.recreate()

            onView(withHint(R.string.server_url)).perform(scrollTo(), replaceText("https://third-server.com"))
            onView(allOf(withHint(R.string.project_icon), withText("T"))).perform(scrollTo()).check(matches(isDisplayed()))
        }
    }

    @Test
    fun `Project name should not be generated again after being changed by a user`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            onView(withHint(R.string.server_url)).perform(scrollTo(), replaceText("https://my-server.com"))
            onView(allOf(withHint(R.string.project_name))).perform(scrollTo(), replaceText("Project X"))
            onView(withHint(R.string.server_url)).perform(scrollTo(), replaceText("https://my-server.com"))
            onView(allOf(withHint(R.string.project_name), withText("Project X"))).perform(scrollTo()).check(matches(isDisplayed()))

            scenario.recreate()

            onView(withHint(R.string.server_url)).perform(scrollTo(), replaceText("https://my-server.com"))
            onView(allOf(withHint(R.string.project_name), withText("Project X"))).perform(scrollTo()).check(matches(isDisplayed()))
        }
    }

    @Test
    fun `Project icon should not be generated again after being changed by a user`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            onView(withHint(R.string.server_url)).perform(scrollTo(), replaceText("https://my-server.com"))
            onView(allOf(withHint(R.string.project_icon))).perform(scrollTo(), replaceText("X"))
            onView(withHint(R.string.server_url)).perform(scrollTo(), replaceText("https://my-server.com"))
            onView(allOf(withHint(R.string.project_icon), withText("X"))).perform(scrollTo()).check(matches(isDisplayed()))

            scenario.recreate()

            onView(withHint(R.string.server_url)).perform(scrollTo(), replaceText("https://my-server.com"))
            onView(allOf(withHint(R.string.project_icon), withText("X"))).perform(scrollTo()).check(matches(isDisplayed()))
        }
    }
}

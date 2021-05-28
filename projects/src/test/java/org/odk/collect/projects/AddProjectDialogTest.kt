package org.odk.collect.projects

import androidx.test.core.app.ApplicationProvider
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.odk.collect.projects.support.Matchers.isPasswordHidden
import org.odk.collect.projects.support.RobolectricApplication
import org.odk.collect.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class AddProjectDialogTest {

    @Test
    fun `Password should be protected`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java, R.style.Theme_MaterialComponents)
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
            onView(withHint(R.string.server_url)).perform(replaceText("https://my-server.com"))
            onView(withText(R.string.add)).perform(click())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `The 'Add' button should be disabled when url is blank`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java, R.style.Theme_MaterialComponents)
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
    fun `A new project should be added with generated details after clicking on the 'Add' button`() {
        val projectsRepository = mock(ProjectsRepository::class.java)
        val application = ApplicationProvider.getApplicationContext<RobolectricApplication>()
        application.projectsDependencyComponent = DaggerProjectsDependencyComponent.builder()
            .projectsDependencyModule(object : ProjectsDependencyModule() {
                override fun providesProjectsRepository(): ProjectsRepository {
                    return projectsRepository
                }
            })
            .build()

        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java, R.style.Theme_MaterialComponents)
        scenario.onFragment {
            onView(withHint(R.string.server_url)).perform(replaceText("https://my-server.com"))

            onView(withText(R.string.add)).perform(click())
            verify(projectsRepository).save(Project.New("my-server.com", "M", "#3e9fcc"))
        }
    }
}

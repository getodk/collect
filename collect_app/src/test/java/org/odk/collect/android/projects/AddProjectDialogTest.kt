package org.odk.collect.android.projects

import androidx.core.os.bundleOf
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.odk.collect.android.R
import org.odk.collect.android.activities.MainMenuActivity
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.projects.AddProjectDialog.Companion.STARTED_FROM_FIRST_LAUNCH_SCREEN
import org.odk.collect.android.support.RobolectricHelpers
import org.odk.collect.android.utilities.UUIDGenerator

@RunWith(AndroidJUnit4::class)
class AddProjectDialogTest {

    @Test
    fun clickingCancel_dismissesDialog() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(withText(R.string.cancel)).perform(click())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun clickingDeviceBackButton_dismissesDialog() {
        val scenario = RobolectricHelpers.launchDialogFragment(AddProjectDialog::class.java)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(isRoot()).perform(pressBack())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun clickingAddButton_dismissesDialog() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(withText(R.string.add)).perform(click())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun clickingAddButton_shouldAddNewProject() {
        val projectsRepository = mock(ProjectsRepository::class.java)
        RobolectricHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesProjectsRepository(uuidGenerator: UUIDGenerator, gson: Gson, settingsProvider: SettingsProvider): ProjectsRepository {
                return projectsRepository
            }
        })

        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java)
        scenario.onFragment {
            onView(withText(R.string.add)).perform(click())
            verify(projectsRepository).add(Project("", "", ""))
        }
    }

    @Test
    fun clickingAddButton_shouldSetCurrentProjectIfStartedFromFirstLaunchScreen() {
        val projectsRepository = mock(ProjectsRepository::class.java)
        `when`(projectsRepository.getAll()).thenReturn(listOf(Project("", "", "", "1")))
        val currentProjectProvider = mock(CurrentProjectProvider::class.java)
        RobolectricHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesProjectsRepository(uuidGenerator: UUIDGenerator, gson: Gson, settingsProvider: SettingsProvider): ProjectsRepository {
                return projectsRepository
            }

            override fun providesCurrentProjectProvider(settingsProvider: SettingsProvider, projectsRepository: ProjectsRepository): CurrentProjectProvider {
                return currentProjectProvider
            }
        })

        val fragmentArgs = bundleOf(STARTED_FROM_FIRST_LAUNCH_SCREEN to true)
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java, fragmentArgs)
        scenario.onFragment {
            onView(withText(R.string.add)).perform(click())
            verify(currentProjectProvider).setCurrentProject("1")
        }
    }

    @Test
    fun clickingAddButton_shouldNotSetCurrentProjectIfNotStartedFromFirstLaunchScreen() {
        val currentProjectProvider = mock(CurrentProjectProvider::class.java)
        RobolectricHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesCurrentProjectProvider(settingsProvider: SettingsProvider, projectsRepository: ProjectsRepository): CurrentProjectProvider {
                return currentProjectProvider
            }
        })

        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java)
        scenario.onFragment {
            onView(withText(R.string.add)).perform(click())
            verifyNoInteractions(currentProjectProvider)
        }
    }

    @Test
    fun clickingAddButton_shouldStartMainMenuIfStartedFromFirstLaunchScreen() {
        val fragmentArgs = bundleOf(STARTED_FROM_FIRST_LAUNCH_SCREEN to true)
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java, fragmentArgs)
        scenario.onFragment {
            Intents.init()
            onView(withText(R.string.add)).perform(click())
            assertThat(Intents.getIntents()[0], hasComponent(MainMenuActivity::class.java.name))
            Intents.release()
        }
    }

    @Test
    fun clickingAddButton_shouldNotStartMainMenuIfNotStartedFromFirstLaunchScreen() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java)
        scenario.onFragment {
            Intents.init()
            onView(withText(R.string.add)).perform(click())
            assertThat(Intents.getIntents().isEmpty(), `is`(true))
            Intents.release()
        }
    }

    @Test
    fun noMoreThanOneCharacter_shouldBeAcceptedAsProjectIcon() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java)
        scenario.onFragment {
            onView(withHint(R.string.project_icon)).perform(typeText("XYZ"))
            onView(allOf(withHint(R.string.project_icon), withText("X"))).check(matches(isDisplayed()))
        }
    }

    @Test
    fun noMoreThanOneEmoji_shouldBeAcceptedAsProjectIcon() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java)
        scenario.onFragment {
            onView(withHint(R.string.project_icon)).perform(replaceText("\uD83D\uDC22"))
            onView(allOf(withHint(R.string.project_icon), withText("\uD83D\uDC22"))).check(matches(isDisplayed()))
        }
    }
}

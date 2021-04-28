package org.odk.collect.android.projects

import androidx.lifecycle.ViewModel
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.odk.collect.android.R
import org.odk.collect.android.activities.AboutActivity
import org.odk.collect.android.activities.viewmodels.CurrentProjectViewModel
import org.odk.collect.android.application.Collect
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.injection.config.AppDependencyComponent
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.preferences.screens.AdminPreferencesActivity
import org.odk.collect.android.preferences.screens.GeneralPreferencesActivity
import org.odk.collect.android.support.RobolectricHelpers
import org.odk.collect.projects.Project

@RunWith(AndroidJUnit4::class)
class ProjectSettingsDialogTest {
    lateinit var appDependencyComponent: AppDependencyComponent

    lateinit var currentProjectViewModel: CurrentProjectViewModel

    @Before
    fun setup() {
        currentProjectViewModel = mock(CurrentProjectViewModel::class.java)
        appDependencyComponent = DaggerUtils.getComponent(Collect.getInstance())
        appDependencyComponent.projectsRepository().deleteAll()

        RobolectricHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesCurrentProjectViewModel(currentProjectProvider: CurrentProjectProvider): CurrentProjectViewModel.Factory {
                return object : CurrentProjectViewModel.Factory(currentProjectProvider) {
                    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                        return currentProjectViewModel as T
                    }
                }
            }
        })
    }

    @Test
    fun `The dialog should be dismissed after clicking on the 'X' button`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(ProjectSettingsDialog::class.java)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(withId(R.id.close_icon)).perform(click())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on a device back button`() {
        val scenario = RobolectricHelpers.launchDialogFragment(ProjectSettingsDialog::class.java)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(isRoot()).perform(pressBack())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun `General settings should be started after clicking on the 'General Settings' button`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(ProjectSettingsDialog::class.java)
        scenario.onFragment {
            Intents.init()
            assertThat(it.isVisible, `is`(true))
            onView(withText(R.string.general_preferences)).perform(click())
            assertThat(it.isVisible, `is`(false))
            assertThat(Intents.getIntents()[0], hasComponent(GeneralPreferencesActivity::class.java.name))
            Intents.release()
        }
    }

    @Test
    fun `Admin settings should be started after clicking on the 'Admin Settings' button`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(ProjectSettingsDialog::class.java)
        scenario.onFragment {
            Intents.init()
            assertThat(it.isVisible, `is`(true))
            onView(withText(R.string.admin_preferences)).perform(click())
            assertThat(it.isVisible, `is`(false))
            assertThat(Intents.getIntents()[0], hasComponent(AdminPreferencesActivity::class.java.name))
            Intents.release()
        }
    }

    @Test
    fun `About section should be started after clicking on the 'About' button`() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(ProjectSettingsDialog::class.java)
        scenario.onFragment {
            Intents.init()
            assertThat(it.isVisible, `is`(true))
            onView(withText(R.string.about_preferences)).perform(click())
            assertThat(it.isVisible, `is`(false))
            assertThat(Intents.getIntents()[0], hasComponent(AboutActivity::class.java.name))
            Intents.release()
        }
    }

    @Test
    fun `currentProjectViewModel should be notified when project switched`() {
        appDependencyComponent.projectsRepository().add(Project("Project X", "X", "#cccccc", "1"))
        appDependencyComponent.projectsRepository().add(Project("Project Y", "Y", "#ffffff", "2"))
        appDependencyComponent.currentProjectProvider().setCurrentProject("1")

        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(ProjectSettingsDialog::class.java)
        scenario.onFragment {
            onView(withText("Project Y")).perform(click())
            verify(currentProjectViewModel).setCurrentProject(Project("Project Y", "Y", "#ffffff", "2"))
        }
    }
}

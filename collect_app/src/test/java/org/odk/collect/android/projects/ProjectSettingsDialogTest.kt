package org.odk.collect.android.projects

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.activities.AboutActivity
import org.odk.collect.android.preferences.screens.AdminPreferencesActivity
import org.odk.collect.android.preferences.screens.GeneralPreferencesActivity
import org.odk.collect.android.support.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class ProjectSettingsDialogTest {

    @Test
    fun clickingCloseButton_dismissesDialog() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(ProjectSettingsDialog::class.java)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(ViewMatchers.withId(R.id.close_icon)).perform(ViewActions.click())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun clickingDeviceBackButton_dismissesDialog() {
        val scenario = RobolectricHelpers.launchDialogFragment(ProjectSettingsDialog::class.java)
        scenario.onFragment {
            assertThat(it.isVisible, `is`(true))
            onView(ViewMatchers.isRoot()).perform(pressBack())
            assertThat(it.isVisible, `is`(false))
        }
    }

    @Test
    fun clickingGeneralSettingsButton_startsGeneralSettings() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(ProjectSettingsDialog::class.java)
        scenario.onFragment {
            Intents.init()
            assertThat(it.isVisible, `is`(true))
            onView(withText(R.string.general_preferences)).perform(click())
            assertThat(it.isVisible, `is`(false))
            MatcherAssert.assertThat(Intents.getIntents()[0], IntentMatchers.hasComponent(GeneralPreferencesActivity::class.java.name))
            Intents.release()
        }
    }

    @Test
    fun clickingAdminSettingsButton_startsAdminSettings() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(ProjectSettingsDialog::class.java)
        scenario.onFragment {
            Intents.init()
            assertThat(it.isVisible, `is`(true))
            onView(withText(R.string.admin_preferences)).perform(click())
            assertThat(it.isVisible, `is`(false))
            MatcherAssert.assertThat(Intents.getIntents()[0], IntentMatchers.hasComponent(AdminPreferencesActivity::class.java.name))
            Intents.release()
        }
    }

    @Test
    fun clickingAboutButton_startsAboutSection() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(ProjectSettingsDialog::class.java)
        scenario.onFragment {
            Intents.init()
            assertThat(it.isVisible, `is`(true))
            onView(withText(R.string.about)).perform(click())
            assertThat(it.isVisible, `is`(false))
            MatcherAssert.assertThat(Intents.getIntents()[0], IntentMatchers.hasComponent(AboutActivity::class.java.name))
            Intents.release()
        }
    }
}

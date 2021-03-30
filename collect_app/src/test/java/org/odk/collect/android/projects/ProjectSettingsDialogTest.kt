package org.odk.collect.android.projects

import android.widget.ImageView
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.support.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class ProjectSettingsDialogTest {

    @Test
    fun clickingCloseButton_dismissesDialog() {
        val scenario = RobolectricHelpers.launchDialogFragment(ProjectSettingsDialog::class.java)
        scenario.onFragment { f: ProjectSettingsDialog ->
            val dialog = f.dialog
            val closeButton = dialog!!.findViewById<ImageView>(R.id.close_icon)
            closeButton.performClick()
            MatcherAssert.assertThat(dialog.isShowing, CoreMatchers.`is`(false))
        }
    }

    @Test
    fun clickingDeviceBackButton_dismissesDialog() {
        val scenario = RobolectricHelpers.launchDialogFragment(ProjectSettingsDialog::class.java)
        scenario.onFragment { f: ProjectSettingsDialog ->
            val dialog = f.dialog
            Espresso.onView(ViewMatchers.isRoot()).perform(ViewActions.pressBack())
            MatcherAssert.assertThat(dialog!!.isShowing, CoreMatchers.`is`(false))
        }
    }
}

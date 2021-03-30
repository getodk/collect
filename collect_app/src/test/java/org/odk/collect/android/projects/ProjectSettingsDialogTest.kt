package org.odk.collect.android.projects

import android.widget.ImageView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
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
            assertThat(dialog!!.isShowing, `is`(true))
            val closeButton = dialog.findViewById<ImageView>(R.id.close_icon)
            closeButton.performClick()
            assertThat(dialog.isShowing, `is`(false))
        }
    }

    @Test
    fun clickingDeviceBackButton_dismissesDialog() {
        val scenario = RobolectricHelpers.launchDialogFragment(ProjectSettingsDialog::class.java)
        scenario.onFragment { f: ProjectSettingsDialog ->
            val dialog = f.dialog
            assertThat(dialog!!.isShowing, `is`(true))
            onView(ViewMatchers.isRoot()).perform(pressBack())
            assertThat(dialog.isShowing, `is`(false))
        }
    }
}

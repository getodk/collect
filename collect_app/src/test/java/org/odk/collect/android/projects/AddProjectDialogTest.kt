package org.odk.collect.android.projects

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.button.MaterialButton
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.support.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class AddProjectDialogTest {

    @Test
    fun clickingCancel_dismissesDialog() {
        val scenario = RobolectricHelpers.launchDialogFragment(AddProjectDialog::class.java)
        scenario.onFragment { f: AddProjectDialog ->
            val dialog = f.dialog
            val cancelButton = dialog?.findViewById<MaterialButton>(R.id.cancel_button)
            assertThat(cancelButton?.text, `is`(f.getString(R.string.cancel)))
            cancelButton?.performClick()
            assertThat(dialog?.isShowing, `is`(false))
        }
    }

    @Test
    fun clickingDeviceBackButton_dismissesDialog() {
        val scenario = RobolectricHelpers.launchDialogFragment(AddProjectDialog::class.java)
        scenario.onFragment { f: AddProjectDialog ->
            val dialog = f.dialog
            Espresso.onView(ViewMatchers.isRoot()).perform(ViewActions.pressBack())
            assertThat(dialog?.isShowing, `is`(false))
        }
    }
}

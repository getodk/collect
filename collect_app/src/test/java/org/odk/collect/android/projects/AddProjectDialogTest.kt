package org.odk.collect.android.projects

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.button.MaterialButton
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
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
            assertThat(dialog!!.isShowing, `is`(true))
            val cancelButton = dialog.findViewById<MaterialButton>(R.id.cancel_button)
            assertThat(cancelButton!!.text, `is`(f.getString(R.string.cancel)))
            cancelButton.performClick()
            assertThat(dialog.isShowing, `is`(false))
        }
    }

    @Test
    fun clickingDeviceBackButton_dismissesDialog() {
        val scenario = RobolectricHelpers.launchDialogFragment(AddProjectDialog::class.java)
        scenario.onFragment { f: AddProjectDialog ->
            val dialog = f.dialog
            assertThat(dialog!!.isShowing, `is`(true))
            onView(isRoot()).perform(ViewActions.pressBack())
            assertThat(dialog.isShowing, `is`(false))
        }
    }

    @Test
    fun noMoreThanOneCharacter_shouldBeAcceptedAsProjectIcon() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java)
        scenario.onFragment {
            onView(withHint(R.string.project_icon)).perform(ViewActions.typeText("XYZ"))
            onView(Matchers.allOf(withHint(R.string.project_icon), withText("X"))).check(matches(isDisplayed()))
        }
    }

    @Test
    fun noMoreThanOneEmoji_shouldBeAcceptedAsProjectIcon() {
        val scenario = RobolectricHelpers.launchDialogFragmentInContainer(AddProjectDialog::class.java)
        scenario.onFragment {
            onView(withHint(R.string.project_icon)).perform(ViewActions.replaceText("\uD83D\uDC22"))
            onView(Matchers.allOf(withHint(R.string.project_icon), withText("\uD83D\uDC22"))).check(matches(isDisplayed()))
        }
    }
}

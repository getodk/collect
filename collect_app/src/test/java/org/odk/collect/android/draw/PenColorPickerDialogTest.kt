package org.odk.collect.android.draw

import android.graphics.Color
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.R
import org.odk.collect.fragmentstest.DialogFragmentTest
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule

@RunWith(AndroidJUnit4::class)
class PenColorPickerDialogTest {

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    @Test
    fun `dialog should be cancelable`() {
        val scenario = launcherRule.launchDialogFragment(PenColorPickerDialog::class.java)
        scenario.onFragment {
            assertThat(it.isCancelable, `is`(true))
        }
    }

    @Test
    fun `pen color in view model should be set after clicking ok`() {
        val scenario = launcherRule.launchDialogFragment(PenColorPickerDialog::class.java)
        scenario.onFragment {
            val testViewModel = mock<PenColorPickerViewModel>()
            it.model = testViewModel

            DialogFragmentTest.onViewInDialog(withText(R.string.ok)).perform(click())

            verify(testViewModel).setPenColor(Color.BLACK)
        }
    }
}

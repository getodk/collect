package org.odk.collect.material

import android.app.Application
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.testshared.RobolectricHelpers
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class MaterialAlertDialogFragmentTest {

    private val args = Bundle().apply {
        putString(MaterialAlertDialogFragment.ARG_MESSAGE, "blah")
    }

    @get:Rule
    val launcherRule =
        FragmentScenarioLauncherRule(defaultThemeResId = R.style.Theme_MaterialComponents)

    @Test
    fun `dialog should be cancelable`() {
        val scenario = launcherRule.launch(MaterialAlertDialogFragment::class.java, args)
        scenario.onFragment {
            assertThat(it.isCancelable, `is`(true))
        }
    }

    @Test
    fun `The title of the 'POSITIVE BUTTON' should be 'OK'`() {
        val scenario = launcherRule.launch(MaterialAlertDialogFragment::class.java, args)
        scenario.onFragment {
            assertThat(
                (it.dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).text,
                `is`(ApplicationProvider.getApplicationContext<Application>().getString(R.string.ok))
            )
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on the 'POSITIVE BUTTON' button`() {
        val scenario = launcherRule.launch(MaterialAlertDialogFragment::class.java, args)
        scenario.onFragment {
            assertThat(it.dialog!!.isShowing, `is`(true))
            (it.dialog!! as AlertDialog).getButton((AlertDialog.BUTTON_POSITIVE)).performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(nullValue()))
        }
    }

    @Test
    fun `The dialog should have no title`() {
        val scenario = launcherRule.launch(MaterialAlertDialogFragment::class.java, args)
        scenario.onFragment {
            assertThat(Shadows.shadowOf(it.dialog).title, `is`(""))
        }
    }

    @Test
    fun `The dialog should display proper message`() {
        val scenario = launcherRule.launch(MaterialAlertDialogFragment::class.java, args)
        scenario.onFragment {
            assertThat((it.dialog!!.findViewById(android.R.id.message) as TextView).text, `is`("blah"))
        }
    }
}

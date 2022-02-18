package org.odk.collect.android.preferences.dialogs

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule
import org.odk.collect.permissions.R
import org.odk.collect.strings.localization.getLocalizedString

@RunWith(AndroidJUnit4::class)
class ResetProgressDialogTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    @Test
    fun `The dialog should not be dismissed after clicking out of its area or on device back button`() {
        val scenario = launcherRule.launchDialogFragment(ResetProgressDialog::class.java)
        scenario.onFragment {
            assertThat(it.isCancelable, `is`(false))
        }
    }

    @Test
    fun `The dialog should display proper content`() {
        val scenario = launcherRule.launchDialogFragment(ResetProgressDialog::class.java)
        scenario.onFragment {
            // Title
            assertThat(it.title, `is`(context.getLocalizedString(R.string.please_wait)))

            // Message
            assertThat(it.message, `is`(context.getLocalizedString(R.string.reset_in_progress)))
        }
    }
}

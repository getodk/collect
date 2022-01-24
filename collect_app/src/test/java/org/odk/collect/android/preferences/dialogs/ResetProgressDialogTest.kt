package org.odk.collect.android.preferences.dialogs

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.fragmentstest.DialogFragmentTest
import org.odk.collect.permissions.R
import org.odk.collect.strings.localization.getLocalizedString

@RunWith(AndroidJUnit4::class)
class ResetProgressDialogTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `The dialog should not be dismissed after clicking out of its area or on device back button`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ResetProgressDialog::class.java)
        scenario.onFragment {
            assertThat(it.isCancelable, `is`(false))
        }
    }

    @Test
    fun `The dialog should display proper content`() {
        val scenario = DialogFragmentTest.launchDialogFragment(ResetProgressDialog::class.java)
        scenario.onFragment {
            // Button positive
            assertThat((it.dialog!! as AlertDialog).getButton((AlertDialog.BUTTON_POSITIVE)).visibility, `is`(View.GONE))

            // Button neutral
            assertThat((it.dialog!! as AlertDialog).getButton((AlertDialog.BUTTON_NEUTRAL)).visibility, `is`(View.GONE))

            // Button negative
            assertThat((it.dialog!! as AlertDialog).getButton((AlertDialog.BUTTON_NEGATIVE)).visibility, `is`(View.GONE))

            // Title
            val titleId: Int = context.resources.getIdentifier("alertTitle", "id", context.packageName)
            assertThat((it.dialog!!.findViewById(titleId) as TextView).text, `is`(context.getLocalizedString(R.string.please_wait)))

            // Message
            assertThat((it.dialog!!.findViewById(R.id.message) as TextView).text, `is`(context.getLocalizedString(R.string.reset_in_progress)))
        }
    }
}

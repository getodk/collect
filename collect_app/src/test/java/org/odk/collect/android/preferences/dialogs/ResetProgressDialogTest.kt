package org.odk.collect.android.preferences.dialogs

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.fragmentstest.DialogFragmentTest
import org.odk.collect.permissions.R
import org.odk.collect.strings.localization.getLocalizedString
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowView

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
            // Title
            assertThat(Shadows.shadowOf(it.dialog).title, `is`(context.getLocalizedString(R.string.please_wait)))

            // Message
            assertThat(ShadowView.innerText(it.dialogView), `is`(context.getLocalizedString(R.string.reset_in_progress)))
        }
    }
}

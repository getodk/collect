package org.odk.collect.android.fragments.dialogs

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.R
import org.odk.collect.android.application.Collect
import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.forms.ManifestFile
import org.odk.collect.fragmentstest.DialogFragmentTest
import org.odk.collect.testshared.RobolectricHelpers
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class FormDownloadResultDialogTest {
    companion object {
        private val DUMMY_SERVER_FORM_DETAILS_1 = ServerFormDetails("1", "", "", "", "", false, false, ManifestFile("", emptyList()))
        private val DUMMY_SERVER_FORM_DETAILS_2 = ServerFormDetails("2", "", "", "", "", false, false, ManifestFile("", emptyList()))
        private const val DUMMY_ERROR_MSG = "error"
        private val SUCCESS_MSG = ApplicationProvider.getApplicationContext<Collect>().getString(R.string.success)
    }

    val listener = mock<FormDownloadResultDialog.FormDownloadResultDialogListener>()

    @Test
    fun `The dialog should have no title`() {
        val args = Bundle()
        args.putSerializable(FormDownloadResultDialog.FAILURES, hashMapOf(DUMMY_SERVER_FORM_DETAILS_1 to SUCCESS_MSG))

        val scenario = DialogFragmentTest.launchDialogFragment(FormDownloadResultDialog::class.java, args)
        scenario.onFragment {
            assertThat(Shadows.shadowOf(it.dialog).title, `is`(""))
        }
    }

    @Test
    fun `The dialog should not be dismissed after clicking out of it's area or on device back button`() {
        val args = Bundle()
        args.putSerializable(FormDownloadResultDialog.FAILURES, hashMapOf(DUMMY_SERVER_FORM_DETAILS_1 to SUCCESS_MSG))

        val scenario = DialogFragmentTest.launchDialogFragment(FormDownloadResultDialog::class.java, args)
        scenario.onFragment {
            assertThat(it.isCancelable, `is`(false))
        }
    }

    @Test
    fun `The title of the 'POSITIVE BUTTON' should be 'OK'`() {
        val args = Bundle()
        args.putSerializable(FormDownloadResultDialog.FAILURES, hashMapOf(DUMMY_SERVER_FORM_DETAILS_1 to SUCCESS_MSG))

        val scenario = DialogFragmentTest.launchDialogFragment(FormDownloadResultDialog::class.java, args)
        scenario.onFragment {
            assertThat((it.dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).text, `is`(ApplicationProvider.getApplicationContext<Collect>().getString(R.string.ok)))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on the 'POSITIVE BUTTON'`() {
        val args = Bundle()
        args.putSerializable(FormDownloadResultDialog.FAILURES, hashMapOf(DUMMY_SERVER_FORM_DETAILS_1 to SUCCESS_MSG))

        val scenario = DialogFragmentTest.launchDialogFragment(FormDownloadResultDialog::class.java, args)
        scenario.onFragment {
            it.setListener(listener)
            assertThat(it.dialog!!.isShowing, `is`(true))
            (it.dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(nullValue()))
        }
    }

    @Test
    fun `onFormDownloadResultDialogOkButtonClicked() should be called after clicking on the 'POSITIVE BUTTON'`() {
        val args = Bundle()
        args.putSerializable(FormDownloadResultDialog.FAILURES, hashMapOf(DUMMY_SERVER_FORM_DETAILS_1 to SUCCESS_MSG))

        val scenario = DialogFragmentTest.launchDialogFragment(FormDownloadResultDialog::class.java, args)
        scenario.onFragment {
            it.setListener(listener)
            (it.dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).performClick()
            RobolectricHelpers.runLooper()
            verify(listener).onFormDownloadResultDialogOkButtonClicked()
        }
    }

    @Test
    fun `If there are no errors an appropriate message should be displayed`() {
        val args = Bundle()
        args.putSerializable(FormDownloadResultDialog.FAILURES, hashMapOf(DUMMY_SERVER_FORM_DETAILS_1 to SUCCESS_MSG))

        DialogFragmentTest.launchDialogFragment(FormDownloadResultDialog::class.java, args)
        DialogFragmentTest
            .onViewInDialog(
                withText(R.string.all_downloads_succeeded)
            ).check(
                matches(isDisplayed())
            )
    }

    @Test
    fun `If there are no errors 'SHOW DETAILS' button should be hidden`() {
        val args = Bundle()
        args.putSerializable(FormDownloadResultDialog.FAILURES, hashMapOf(DUMMY_SERVER_FORM_DETAILS_1 to SUCCESS_MSG))

        val scenario = DialogFragmentTest.launchDialogFragment(FormDownloadResultDialog::class.java, args)
        scenario.onFragment {
            assertThat((it.dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE).visibility, `is`(View.GONE))
        }
    }

    @Test
    fun `If there are errors an appropriate message should be displayed`() {
        val args = Bundle()
        val results = hashMapOf(
            DUMMY_SERVER_FORM_DETAILS_1 to SUCCESS_MSG,
            DUMMY_SERVER_FORM_DETAILS_2 to DUMMY_ERROR_MSG
        )
        args.putSerializable(FormDownloadResultDialog.FAILURES, results)

        DialogFragmentTest.launchDialogFragment(FormDownloadResultDialog::class.java, args)
        DialogFragmentTest
            .onViewInDialog(
                withText(ApplicationProvider.getApplicationContext<Collect>().getString(R.string.some_downloads_failed, "1", "2"))
            ).check(
                matches(
                    isDisplayed()
                )
            )
    }

    @Test
    fun `If there are errors 'SHOW DETAILS' button should be displayed`() {
        val args = Bundle()
        val results = hashMapOf(
            DUMMY_SERVER_FORM_DETAILS_1 to SUCCESS_MSG,
            DUMMY_SERVER_FORM_DETAILS_2 to DUMMY_ERROR_MSG
        )
        args.putSerializable(FormDownloadResultDialog.FAILURES, results)

        val scenario = DialogFragmentTest.launchDialogFragment(FormDownloadResultDialog::class.java, args)
        scenario.onFragment {
            assertThat((it.dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE).visibility, `is`(View.VISIBLE))
            assertThat((it.dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE).text, `is`(ApplicationProvider.getApplicationContext<Collect>().getString(R.string.show_details)))
        }
    }

    @Test
    fun `The dialog should be dismissed after clicking on the 'NEGATIVE BUTTON'`() {
        val args = Bundle()
        val results = hashMapOf(
            DUMMY_SERVER_FORM_DETAILS_1 to SUCCESS_MSG,
            DUMMY_SERVER_FORM_DETAILS_2 to DUMMY_ERROR_MSG
        )
        args.putSerializable(FormDownloadResultDialog.FAILURES, results)

        val scenario = DialogFragmentTest.launchDialogFragment(FormDownloadResultDialog::class.java, args)
        scenario.onFragment {
            it.setListener(listener)
            assertThat(it.dialog!!.isShowing, `is`(true))
            (it.dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
            RobolectricHelpers.runLooper()
            assertThat(it.dialog, `is`(nullValue()))
        }
    }

    @Test
    fun `Recreation should not change the state of dialog`() {
        val args = Bundle()
        val results = hashMapOf(
            DUMMY_SERVER_FORM_DETAILS_1 to SUCCESS_MSG,
            DUMMY_SERVER_FORM_DETAILS_2 to DUMMY_ERROR_MSG
        )
        args.putSerializable(FormDownloadResultDialog.FAILURES, results)

        val scenario = DialogFragmentTest.launchDialogFragment(FormDownloadResultDialog::class.java, args)
        scenario.onFragment {
            assertThat(Shadows.shadowOf(it.dialog).title, `is`(""))
            assertThat((it.dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).text, `is`(ApplicationProvider.getApplicationContext<Collect>().getString(R.string.ok)))
            assertThat((it.dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE).text, `is`(ApplicationProvider.getApplicationContext<Collect>().getString(R.string.show_details)))
        }

        scenario.recreate()

        scenario.onFragment {
            assertThat(Shadows.shadowOf(it.dialog).title, `is`(""))
            assertThat((it.dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).text, `is`(ApplicationProvider.getApplicationContext<Collect>().getString(R.string.ok)))
            assertThat((it.dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEGATIVE).text, `is`(ApplicationProvider.getApplicationContext<Collect>().getString(R.string.show_details)))
        }
    }
}

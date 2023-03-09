package org.odk.collect.android.formentry

import android.app.Activity
import android.content.DialogInterface
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.formentry.saving.FormSaveViewModel
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.shadows.ShadowAndroidXAlertDialog
import org.odk.collect.testshared.RobolectricHelpers.runLooper
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow.extract

@RunWith(AndroidJUnit4::class)
@Config(shadows = [ShadowAndroidXAlertDialog::class])
class QuitFormDialogTest {

    private val formSaveViewModel = mock(FormSaveViewModel::class.java)
    private val formEntryViewModel = mock(FormEntryViewModel::class.java)
    private val settingsProvider = InMemSettingsProvider()
    private val currentProjectProvider = mock(CurrentProjectProvider::class.java)

    @Test
    fun shouldShowCorrectButtons() {
        val activity = Robolectric.buildActivity(Activity::class.java).get()
        val dialog = showDialog(activity)

        assertThat(dialog.getButton(DialogInterface.BUTTON_POSITIVE).visibility, equalTo(View.GONE))
        assertThat(
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).visibility,
            equalTo(View.VISIBLE),
        )
        assertThat(
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).text,
            equalTo(activity.getString(R.string.do_not_exit))
        )
    }

    @Test
    fun shouldShowCorrectTitle_whenNoFormIsLoaded() {
        val activity = Robolectric.buildActivity(Activity::class.java).get()
        val dialog = showDialog(activity)

        val dialogTitle = dialog.findViewById<TextView>(R.id.alertTitle)
        assertThat(
            dialogTitle!!.text.toString(),
            equalTo(
                activity.getString(
                    R.string.quit_application,
                    activity.getString(R.string.no_form_loaded)
                )
            )
        )
    }

    @Test
    fun shouldShowCorrectTitle_whenFormIsLoaded() {
        whenever(formSaveViewModel.formName).thenReturn("blah")

        val activity = Robolectric.buildActivity(Activity::class.java).get()
        val dialog = showDialog(activity)

        val dialogTitle = dialog.findViewById<TextView>(R.id.alertTitle)
        assertThat(
            dialogTitle!!.text.toString(),
            equalTo(activity.getString(R.string.quit_application, "blah")),
        )
    }

    @Test
    fun isCancellable() {
        val activity = Robolectric.buildActivity(Activity::class.java).get()
        val dialog = showDialog(activity)

        assertThat(shadowOf(dialog).isCancelable, equalTo(true))
    }

    @Test
    fun clickingCancel_shouldDismissDialog() {
        val activity = Robolectric.buildActivity(Activity::class.java).get()
        val dialog = showDialog(activity)

        assertThat(dialog.isShowing, equalTo(true))

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick()
        runLooper()
        assertThat(dialog.isShowing, equalTo(false))
    }

    @Test
    fun clickingIgnoreChanges_callsExitOnFormEntryViewModel() {
        val activity = Robolectric.buildActivity(Activity::class.java).get()
        val dialog = showDialog(activity)

        val shadowDialog = extract<ShadowAndroidXAlertDialog>(dialog)
        val view = shadowDialog.getView() as ListView
        view.onItemClickListener?.onItemClick(null, null, 0, -1)

        verify(formEntryViewModel).exit()
    }

    private fun showDialog(activity: Activity) = QuitFormDialog.show(
        activity,
        formSaveViewModel,
        formEntryViewModel,
        settingsProvider,
        currentProjectProvider,
        null
    )
}

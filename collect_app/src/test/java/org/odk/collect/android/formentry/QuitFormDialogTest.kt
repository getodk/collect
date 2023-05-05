package org.odk.collect.android.formentry

import android.app.Activity
import android.content.DialogInterface
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.not
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.adapters.model.IconMenuItem
import org.odk.collect.android.formentry.saving.FormSaveViewModel
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys
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
    private val activity = Robolectric.buildActivity(Activity::class.java).get()

    @Test
    fun `The 'Save' button should be hidden if 'Save Form' is disabled in protected settings`() {
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_SAVE_MID, false)
        val dialog = showDialog(activity)

        val shadowDialog = extract<ShadowAndroidXAlertDialog>(dialog)
        val view = shadowDialog.getView() as ListView
        val iconMenuItem: IconMenuItem = view.adapter.getItem(0) as IconMenuItem

        assertThat(view.adapter.count, equalTo(1))
        assertThat(iconMenuItem.imageResId, not(equalTo(R.drawable.ic_save)))
        assertThat(iconMenuItem.textResId, not(equalTo(R.string.save_as_draft)))
    }

    @Test
    fun `The 'Save' button should be visible if 'Save Form' is enabled in protected settings`() {
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_SAVE_MID, true)
        val dialog = showDialog(activity)

        val shadowDialog = extract<ShadowAndroidXAlertDialog>(dialog)
        val view = shadowDialog.getView() as ListView
        val iconMenuItem: IconMenuItem = view.adapter.getItem(0) as IconMenuItem

        assertThat(iconMenuItem.imageResId, equalTo(R.drawable.ic_save))
        assertThat(iconMenuItem.textResId, equalTo(R.string.save_as_draft))
    }

    @Test
    fun `The 'Discard' button text should be 'Discard Form' if there are no saved changes`() {
        val dialog = showDialog(activity)

        val shadowDialog = extract<ShadowAndroidXAlertDialog>(dialog)
        val view = shadowDialog.getView() as ListView
        val iconMenuItem: IconMenuItem = view.adapter.getItem(0) as IconMenuItem

        assertThat(iconMenuItem.imageResId, equalTo(R.drawable.ic_delete))
        assertThat(iconMenuItem.textResId, equalTo(R.string.do_not_save))
    }

    @Test
    fun `The 'Discard' button text should be 'Discard Changes' if there are saved changes`() {
        whenever(formSaveViewModel.hasSaved()).thenReturn(true)
        val dialog = showDialog(activity)

        val shadowDialog = extract<ShadowAndroidXAlertDialog>(dialog)
        val view = shadowDialog.getView() as ListView
        val iconMenuItem: IconMenuItem = view.adapter.getItem(0) as IconMenuItem

        assertThat(iconMenuItem.imageResId, equalTo(R.drawable.ic_delete))
        assertThat(iconMenuItem.textResId, equalTo(R.string.discard_changes))
    }

    @Test
    fun `Only the 'Cancel' button should be visible`() {
        val dialog = showDialog(activity)

        assertThat(dialog.getButton(DialogInterface.BUTTON_POSITIVE).visibility, equalTo(View.GONE))
        assertThat(
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).visibility,
            equalTo(View.VISIBLE)
        )
        assertThat(
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).text,
            equalTo(activity.getString(R.string.do_not_exit))
        )
    }

    @Test
    fun `Correct tittle should be displayed when no form is loaded`() {
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
    fun `Correct tittle should be displayed when form is loaded`() {
        whenever(formSaveViewModel.formName).thenReturn("blah")
        val dialog = showDialog(activity)

        val dialogTitle = dialog.findViewById<TextView>(R.id.alertTitle)
        assertThat(
            dialogTitle!!.text.toString(),
            equalTo(activity.getString(R.string.quit_application, "blah"))
        )
    }

    @Test
    fun `Dialog should be cancelable`() {
        val dialog = showDialog(activity)

        assertThat(shadowOf(dialog).isCancelable, equalTo(true))
    }

    @Test
    fun `Clicking the 'Cancel' button should dismiss the dialog`() {
        val dialog = showDialog(activity)

        assertThat(dialog.isShowing, equalTo(true))

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick()
        runLooper()
        assertThat(dialog.isShowing, equalTo(false))
    }

    @Test
    fun `Ignoring changes calls exit on formEntryViewModel`() {
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

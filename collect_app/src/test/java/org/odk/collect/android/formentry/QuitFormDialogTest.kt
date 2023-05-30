package org.odk.collect.android.formentry

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.formentry.saving.FormSaveViewModel
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys
import org.odk.collect.shadows.ShadowAndroidXAlertDialog
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow.extract
import java.text.SimpleDateFormat
import java.util.Locale

@RunWith(AndroidJUnit4::class)
@Config(shadows = [ShadowAndroidXAlertDialog::class])
class QuitFormDialogTest {

    private val formSaveViewModel = mock<FormSaveViewModel>() {
        on { lastSavedTime } doReturn null
    }

    private val formEntryViewModel = mock<FormEntryViewModel>()
    private val settingsProvider = InMemSettingsProvider()

    @Test
    fun isCancellable() {
        val activity = Robolectric.buildActivity(Activity::class.java).get()
        val dialog = showDialog(activity)

        assertThat(shadowOf(dialog).isCancelable, equalTo(true))
    }

    @Test
    fun clickingDiscardChanges_callsExitOnFormEntryViewModel() {
        val activity = Robolectric.buildActivity(Activity::class.java).get()
        val dialog = showDialog(activity)

        val shadowDialog = extract<ShadowAndroidXAlertDialog>(dialog)
        val view = shadowDialog.getView()
        view.findViewById<View>(R.id.discard_changes).performClick()

        verify(formEntryViewModel).exit()
    }

    @Test
    fun clickingKeepEditing_dismissesDialog() {
        val activity = Robolectric.buildActivity(Activity::class.java).get()
        val dialog = showDialog(activity)

        val shadowDialog = extract<ShadowAndroidXAlertDialog>(dialog)
        val view = shadowDialog.getView()
        view.findViewById<View>(R.id.keep_editing).performClick()

        assertThat(dialog.isShowing, equalTo(false))
    }

    @Test
    fun whenSaveAsDraftIsEnabled_andLastSavedTimeIsNull_showsSaveExplanation() {
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT, true)
        whenever(formSaveViewModel.lastSavedTime).doReturn(null)

        val activity = Robolectric.buildActivity(Activity::class.java).get()
        val dialog = showDialog(activity)

        val shadowDialog = extract<ShadowAndroidXAlertDialog>(dialog)
        assertThat(
            shadowDialog.getView().findViewById<TextView>(R.id.save_explanation).text,
            equalTo(activity.getString(R.string.save_explanation))
        )
    }

    @Test
    fun whenSaveAsDraftIsEnabled_andLastSavedTimeIsNotNull_showsLastSavedTime() {
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT, true)
        whenever(formSaveViewModel.lastSavedTime).doReturn(456L)

        val activity = Robolectric.buildActivity(Activity::class.java).get()
        val dialog = showDialog(activity)

        val shadowDialog = extract<ShadowAndroidXAlertDialog>(dialog)

        assertThat(
            shadowDialog.getView().findViewById<TextView>(R.id.save_explanation).text,
            equalTo(
                SimpleDateFormat(
                    activity.getString(R.string.save_explanation_with_last_saved),
                    Locale.getDefault()
                ).format(456L)
            )
        )
    }

    @Test
    fun whenSaveAsDraftIsDisabled_andLastSavedTimeIsNull_showsWarningTitleAndMessage_andHidesButton() {
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT, false)
        whenever(formSaveViewModel.lastSavedTime).doReturn(null)

        val activity = Robolectric.buildActivity(Activity::class.java).get()
        val dialog = showDialog(activity)

        val shadowDialog = extract<ShadowAndroidXAlertDialog>(dialog)

        assertThat(
            shadowDialog.title,
            equalTo(activity.getString(R.string.quit_form_continue_title))
        )
        assertThat(
            shadowDialog.getView().findViewById<TextView>(R.id.save_explanation).text,
            equalTo(activity.getString(R.string.discard_form_warning))
        )
        assertThat(
            shadowDialog.getView().findViewById<View>(R.id.save_changes).visibility,
            equalTo(View.GONE)
        )
    }

    @Test
    fun whenSaveAsDraftIsDisabled_andLastSavedTimeIsNotNull_showsWarningTitleAndMessage_andHidesButton() {
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_SAVE_AS_DRAFT, false)
        whenever(formSaveViewModel.lastSavedTime).doReturn(456L)

        val activity = Robolectric.buildActivity(Activity::class.java).get()
        val dialog = showDialog(activity)

        val shadowDialog = extract<ShadowAndroidXAlertDialog>(dialog)

        assertThat(
            shadowDialog.title,
            equalTo(activity.getString(R.string.quit_form_continue_title))
        )
        assertThat(
            shadowDialog.getView().findViewById<TextView>(R.id.save_explanation).text,
            equalTo(
                SimpleDateFormat(
                    activity.getString(R.string.discard_changes_warning),
                    Locale.getDefault()
                ).format(456L)
            )
        )
        assertThat(
            shadowDialog.getView().findViewById<View>(R.id.save_changes).visibility,
            equalTo(View.GONE)
        )
    }

    private fun showDialog(activity: Activity) = QuitFormDialog.show(
        activity,
        formSaveViewModel,
        formEntryViewModel,
        settingsProvider,
        null
    )
}

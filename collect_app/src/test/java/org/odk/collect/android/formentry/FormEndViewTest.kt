package org.odk.collect.android.formentry

import android.app.Application
import android.graphics.drawable.VectorDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickSafeMaterialButton

@RunWith(AndroidJUnit4::class)
class FormEndViewTest {
    private val context: Application =
        ApplicationProvider.getApplicationContext<Application>().also {
            it.setTheme(R.style.Theme_Collect) // Needed for ?colorSurfaceContainerHighest
        }
    private val formEndViewModel = mock<FormEndViewModel>()
    private val listener = mock<FormEndView.Listener>()

    @Test
    fun `form title is displayed correctly`() {
        val view = FormEndView(context, "blah", false, formEndViewModel, listener)
        assertThat(
            view.findViewById<TextView>(R.id.description).text,
            equalTo(
                context.getString(
                    org.odk.collect.strings.R.string.save_enter_data_description,
                    "blah"
                )
            )
        )
    }

    @Test
    fun `when saving drafts is enabled in settings should 'Save as draft' button be visible`() {
        whenever(formEndViewModel.isSaveDraftEnabled()).thenReturn(true)
        val view = FormEndView(context, "blah", false, formEndViewModel, listener)
        assertThat(
            view.findViewById<MultiClickSafeMaterialButton>(R.id.save_as_draft).visibility,
            equalTo(View.VISIBLE)
        )
    }

    @Test
    fun `when saving drafts is disabled in settings should 'Save as draft' button be hidden`() {
        whenever(formEndViewModel.isSaveDraftEnabled()).thenReturn(false)
        val view = FormEndView(context, "blah", false, formEndViewModel, listener)
        assertThat(
            view.findViewById<MultiClickSafeMaterialButton>(R.id.save_as_draft).visibility,
            equalTo(View.GONE)
        )
    }

    @Test
    fun `when 'Save as draft' button is clicked then onSaveClicked is called with false value`() {
        whenever(formEndViewModel.isSaveDraftEnabled()).thenReturn(true)
        val view = FormEndView(context, "blah", false, formEndViewModel, listener)
        view.findViewById<MultiClickSafeMaterialButton>(R.id.save_as_draft).performClick()
        verify(listener).onSaveClicked(false)
    }

    @Test
    fun `when finalizing forms is enabled in settings should 'Finalize' button be visible`() {
        whenever(formEndViewModel.isFinalizeEnabled()).thenReturn(true)
        val view = FormEndView(context, "blah", false, formEndViewModel, listener)
        assertThat(
            view.findViewById<MultiClickSafeMaterialButton>(R.id.finalize).visibility,
            equalTo(View.VISIBLE)
        )
    }

    @Test
    fun `when finalizing forms is disabled in settings should 'Finalize' button be hidden`() {
        whenever(formEndViewModel.isFinalizeEnabled()).thenReturn(false)
        val view = FormEndView(context, "blah", false, formEndViewModel, listener)
        assertThat(view.findViewById<MultiClickSafeMaterialButton>(R.id.finalize).visibility, equalTo(View.GONE))
    }

    @Test
    fun `when 'Finalize' button is clicked then onSaveClicked is called with true value`() {
        whenever(formEndViewModel.isFinalizeEnabled()).thenReturn(true)
        val view = FormEndView(context, "blah", false, formEndViewModel, listener)
        view.findViewById<MultiClickSafeMaterialButton>(R.id.finalize).performClick()
        verify(listener).onSaveClicked(true)
    }

    @Test
    fun `when form should not be sent automatically then 'Finalize' button should be displayed`() {
        whenever(formEndViewModel.shouldFormBeSentAutomatically()).thenReturn(false)
        val view = FormEndView(context, "blah", false, formEndViewModel, listener)
        assertThat(
            view.findViewById<MultiClickSafeMaterialButton>(R.id.finalize).text,
            equalTo(context.getString(org.odk.collect.strings.R.string.finalize))
        )
    }

    @Test
    fun `when form should be sent automatically then 'Send' button should be displayed`() {
        whenever(formEndViewModel.shouldFormBeSentAutomatically()).thenReturn(true)
        val view = FormEndView(context, "blah", false, formEndViewModel, listener)
        assertThat(
            view.findViewById<MultiClickSafeMaterialButton>(R.id.finalize).text,
            equalTo(
                context.getString(
                    org.odk.collect.strings.R.string.send
                )
            )
        )
    }

    @Test
    fun `when 'Save as draft' and 'Send' buttons are visible display correct info for forms that can be edited after finalization`() {
        whenever(formEndViewModel.isSaveDraftEnabled()).thenReturn(true)
        whenever(formEndViewModel.isFinalizeEnabled()).thenReturn(true)
        whenever(formEndViewModel.shouldFormBeSentAutomatically()).thenReturn(true)

        val view = FormEndView(context, "blah", true, formEndViewModel, listener)

        assertThat(
            (view.findViewById<ImageView>(R.id.form_edits_icon).drawable as VectorDrawable).toBitmap().sameAs((context.getDrawable(R.drawable.ic_edit_24) as VectorDrawable).toBitmap()),
            equalTo(true)
        )
        assertThat(
            view.findViewById<MaterialCardView>(R.id.form_edits_warning).visibility,
            equalTo(View.VISIBLE)
        )
        assertThat(
            view.findViewById<MaterialTextView>(R.id.form_edits_warning_title).text,
            equalTo(
                context.getString(
                    org.odk.collect.strings.R.string.form_editing_enabled_after_sending
                )
            )
        )
        assertThat(
            view.findViewById<MaterialTextView>(R.id.form_edits_warning_message).text.toString(),
            equalTo(
                context.getString(org.odk.collect.strings.R.string.form_editing_enabled_after_sending_hint)
            )
        )
    }

    @Test
    fun `when 'Save as draft' and 'Send' buttons are visible display correct info for forms that can not be edited after finalization`() {
        whenever(formEndViewModel.isSaveDraftEnabled()).thenReturn(true)
        whenever(formEndViewModel.isFinalizeEnabled()).thenReturn(true)
        whenever(formEndViewModel.shouldFormBeSentAutomatically()).thenReturn(true)

        val view = FormEndView(context, "blah", false, formEndViewModel, listener)

        assertThat(
            (view.findViewById<ImageView>(R.id.form_edits_icon).drawable as VectorDrawable).toBitmap().sameAs((context.getDrawable(R.drawable.ic_edit_off_24) as VectorDrawable).toBitmap()),
            equalTo(true)
        )
        assertThat(
            view.findViewById<MaterialCardView>(R.id.form_edits_warning).visibility,
            equalTo(View.VISIBLE)
        )
        assertThat(
            view.findViewById<MaterialTextView>(R.id.form_edits_warning_title).text,
            equalTo(
                context.getString(
                    org.odk.collect.strings.R.string.form_editing_disabled_after_sending
                )
            )
        )
        assertThat(
            view.findViewById<MaterialTextView>(R.id.form_edits_warning_message).text.toString(),
            equalTo(context.getString(org.odk.collect.strings.R.string.form_editing_disabled_hint))
        )
    }

    @Test
    fun `when 'Save as draft' and 'Finalize' buttons are visible correct info for forms that can be edited after finalization`() {
        whenever(formEndViewModel.isSaveDraftEnabled()).thenReturn(true)
        whenever(formEndViewModel.isFinalizeEnabled()).thenReturn(true)
        whenever(formEndViewModel.shouldFormBeSentAutomatically()).thenReturn(false)

        val view = FormEndView(context, "blah", true, formEndViewModel, listener)

        assertThat(
            (view.findViewById<ImageView>(R.id.form_edits_icon).drawable as VectorDrawable).toBitmap().sameAs((context.getDrawable(R.drawable.ic_edit_24) as VectorDrawable).toBitmap()),
            equalTo(true)
        )
        assertThat(
            view.findViewById<MaterialCardView>(R.id.form_edits_warning).visibility,
            equalTo(View.VISIBLE)
        )
        assertThat(
            view.findViewById<MaterialTextView>(R.id.form_edits_warning_title).text,
            equalTo(
                context.getString(
                    org.odk.collect.strings.R.string.form_editing_enabled_after_finalizing
                )
            )
        )
        assertThat(
            view.findViewById<MaterialTextView>(R.id.form_edits_warning_message).text.toString(),
            equalTo(
                context.getString(org.odk.collect.strings.R.string.form_editing_enabled_after_finalizing_hint)
            )
        )
    }

    @Test
    fun `when 'Save as draft' and 'Finalize' buttons are visible correct info for forms that can not be edited after finalization`() {
        whenever(formEndViewModel.isSaveDraftEnabled()).thenReturn(true)
        whenever(formEndViewModel.isFinalizeEnabled()).thenReturn(true)
        whenever(formEndViewModel.shouldFormBeSentAutomatically()).thenReturn(false)

        val view = FormEndView(context, "blah", false, formEndViewModel, listener)

        assertThat(
            (view.findViewById<ImageView>(R.id.form_edits_icon).drawable as VectorDrawable).toBitmap().sameAs((context.getDrawable(R.drawable.ic_edit_off_24) as VectorDrawable).toBitmap()),
            equalTo(true)
        )
        assertThat(
            view.findViewById<MaterialCardView>(R.id.form_edits_warning).visibility,
            equalTo(View.VISIBLE)
        )
        assertThat(
            view.findViewById<MaterialTextView>(R.id.form_edits_warning_title).text,
            equalTo(
                context.getString(
                    org.odk.collect.strings.R.string.form_editing_disabled_after_finalizing
                )
            )
        )
        assertThat(
            view.findViewById<MaterialTextView>(R.id.form_edits_warning_message).text.toString(),
            equalTo(context.getString(org.odk.collect.strings.R.string.form_editing_disabled_hint))
        )
    }

    @Test
    fun `when only 'Send' button is visible display correct info for forms that can be edited after sending`() {
        whenever(formEndViewModel.isSaveDraftEnabled()).thenReturn(false)
        whenever(formEndViewModel.isFinalizeEnabled()).thenReturn(true)
        whenever(formEndViewModel.shouldFormBeSentAutomatically()).thenReturn(true)

        val view = FormEndView(context, "blah", true, formEndViewModel, listener)

        assertThat(
            (view.findViewById<ImageView>(R.id.form_edits_icon).drawable as VectorDrawable).toBitmap().sameAs((context.getDrawable(R.drawable.ic_edit_24) as VectorDrawable).toBitmap()),
            equalTo(true)
        )
        assertThat(
            view.findViewById<MaterialCardView>(R.id.form_edits_warning).visibility,
            equalTo(View.VISIBLE)
        )
        assertThat(
            view.findViewById<MaterialTextView>(R.id.form_edits_warning_title).text,
            equalTo(
                context.getString(
                    org.odk.collect.strings.R.string.form_editing_enabled_after_sending
                )
            )
        )
        assertThat(
            view.findViewById<MaterialTextView>(R.id.form_edits_warning_message).text.toString(),
            equalTo(
                context.getString(org.odk.collect.strings.R.string.form_editing_enabled_after_sending_hint)
            )
        )
    }

    @Test
    fun `when only 'Send' button is visible display correct info for forms that can not be edited after sending`() {
        whenever(formEndViewModel.isSaveDraftEnabled()).thenReturn(false)
        whenever(formEndViewModel.isFinalizeEnabled()).thenReturn(true)
        whenever(formEndViewModel.shouldFormBeSentAutomatically()).thenReturn(true)

        val view = FormEndView(context, "blah", false, formEndViewModel, listener)

        assertThat(
            (view.findViewById<ImageView>(R.id.form_edits_icon).drawable as VectorDrawable).toBitmap().sameAs((context.getDrawable(R.drawable.ic_edit_off_24) as VectorDrawable).toBitmap()),
            equalTo(true)
        )
        assertThat(
            view.findViewById<MaterialCardView>(R.id.form_edits_warning).visibility,
            equalTo(View.VISIBLE)
        )
        assertThat(
            view.findViewById<MaterialTextView>(R.id.form_edits_warning_title).text,
            equalTo(
                context.getString(
                    org.odk.collect.strings.R.string.form_editing_disabled_after_sending
                )
            )
        )
    }

    @Test
    fun `when only 'Finalize' button is visible display correct info for forms that can be edited after finalization`() {
        whenever(formEndViewModel.isSaveDraftEnabled()).thenReturn(false)
        whenever(formEndViewModel.isFinalizeEnabled()).thenReturn(true)
        whenever(formEndViewModel.shouldFormBeSentAutomatically()).thenReturn(false)

        val view = FormEndView(context, "blah", true, formEndViewModel, listener)

        assertThat(
            (view.findViewById<ImageView>(R.id.form_edits_icon).drawable as VectorDrawable).toBitmap().sameAs((context.getDrawable(R.drawable.ic_edit_24) as VectorDrawable).toBitmap()),
            equalTo(true)
        )
        assertThat(
            view.findViewById<MaterialCardView>(R.id.form_edits_warning).visibility,
            equalTo(View.VISIBLE)
        )
        assertThat(
            view.findViewById<MaterialTextView>(R.id.form_edits_warning_title).text,
            equalTo(
                context.getString(
                    org.odk.collect.strings.R.string.form_editing_enabled_after_finalizing
                )
            )
        )
        assertThat(
            view.findViewById<MaterialTextView>(R.id.form_edits_warning_message).text.toString(),
            equalTo(
                context.getString(org.odk.collect.strings.R.string.form_editing_enabled_after_finalizing_hint)
            )
        )
    }

    @Test
    fun `when only 'Finalize' button is visible display correct info for forms that can not be edited after finalization`() {
        whenever(formEndViewModel.isSaveDraftEnabled()).thenReturn(false)
        whenever(formEndViewModel.isFinalizeEnabled()).thenReturn(true)
        whenever(formEndViewModel.shouldFormBeSentAutomatically()).thenReturn(false)

        val view = FormEndView(context, "blah", false, formEndViewModel, listener)

        assertThat(
            (view.findViewById<ImageView>(R.id.form_edits_icon).drawable as VectorDrawable).toBitmap().sameAs((context.getDrawable(R.drawable.ic_edit_off_24) as VectorDrawable).toBitmap()),
            equalTo(true)
        )
        assertThat(
            view.findViewById<MaterialCardView>(R.id.form_edits_warning).visibility,
            equalTo(View.VISIBLE)
        )
        assertThat(
            view.findViewById<MaterialTextView>(R.id.form_edits_warning_title).text,
            equalTo(
                context.getString(
                    org.odk.collect.strings.R.string.form_editing_disabled_after_finalizing
                )
            )
        )
    }

    @Test
    fun `when only 'Save as draft' button is visible do not display the info banner for forms that can be edited after finalization`() {
        whenever(formEndViewModel.isSaveDraftEnabled()).thenReturn(true)
        whenever(formEndViewModel.isFinalizeEnabled()).thenReturn(false)

        val view = FormEndView(context, "blah", true, formEndViewModel, listener)

        assertThat(
            view.findViewById<MaterialCardView>(R.id.form_edits_warning).visibility,
            equalTo(View.GONE)
        )
    }

    @Test
    fun `when only 'Save as draft' button is visible do not display the info banner for forms that can not be edited after finalization`() {
        whenever(formEndViewModel.isSaveDraftEnabled()).thenReturn(true)
        whenever(formEndViewModel.isFinalizeEnabled()).thenReturn(false)

        val view = FormEndView(context, "blah", false, formEndViewModel, listener)

        assertThat(
            view.findViewById<MaterialCardView>(R.id.form_edits_warning).visibility,
            equalTo(View.GONE)
        )
    }
}

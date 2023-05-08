package org.odk.collect.android.formentry

import android.app.Application
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.material.button.MaterialButton
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.formstest.FormFixtures

@RunWith(AndroidJUnit4::class)
class FormEndViewTest {
    private val context: Application =
        ApplicationProvider.getApplicationContext<Application>().also {
            it.setTheme(R.style.Theme_Material3_Light)
        }
    private val formEndViewModel = mock<FormEndViewModel>()
    private val listener = mock<FormEndView.Listener>()

    @Test
    fun `form title is displayed correctly`() {
        val view = FormEndView(context, "blah", FormFixtures.form(), formEndViewModel, listener)
        assertThat(view.findViewById<TextView>(R.id.description).text, equalTo(context.getString(R.string.save_enter_data_description, "blah")))
    }

    @Test
    fun `when saving drafts is enabled in settings should 'Save as draft' button be enabled`() {
        whenever(formEndViewModel.isSaveDraftEnabled()).thenReturn(true)
        val view = FormEndView(context, "blah", FormFixtures.form(), formEndViewModel, listener)
        assertThat(view.findViewById<MaterialButton>(R.id.save_as_draft).isEnabled, equalTo(true))
    }

    @Test
    fun `when saving drafts is disabled in settings should 'Save as draft' button be disabled`() {
        whenever(formEndViewModel.isSaveDraftEnabled()).thenReturn(false)
        val view = FormEndView(context, "blah", FormFixtures.form(), formEndViewModel, listener)
        assertThat(view.findViewById<MaterialButton>(R.id.save_as_draft).isEnabled, equalTo(false))
    }

    @Test
    fun `when 'Save as draft' button is clicked then onSaveClicked is called with false value`() {
        whenever(formEndViewModel.isSaveDraftEnabled()).thenReturn(true)
        val view = FormEndView(context, "blah", FormFixtures.form(), formEndViewModel, listener)
        view.findViewById<MaterialButton>(R.id.save_as_draft).performClick()
        verify(listener).onSaveClicked(false)
    }

    @Test
    fun `when finalizing forms is enabled in settings should 'Finalize' button be enabled`() {
        whenever(formEndViewModel.isFinalizeEnabled()).thenReturn(true)
        val view = FormEndView(context, "blah", FormFixtures.form(), formEndViewModel, listener)
        assertThat(view.findViewById<MaterialButton>(R.id.finalize).isEnabled, equalTo(true))
    }

    @Test
    fun `when finalizing forms is disabled in settings should 'Finalize' button be disabled`() {
        whenever(formEndViewModel.isFinalizeEnabled()).thenReturn(false)
        val view = FormEndView(context, "blah", FormFixtures.form(), formEndViewModel, listener)
        assertThat(view.findViewById<MaterialButton>(R.id.finalize).isEnabled, equalTo(false))
    }

    @Test
    fun `when 'Finalize' button is clicked then onSaveClicked is called with true value`() {
        whenever(formEndViewModel.isFinalizeEnabled()).thenReturn(true)
        val view = FormEndView(context, "blah", FormFixtures.form(), formEndViewModel, listener)
        view.findViewById<MaterialButton>(R.id.finalize).performClick()
        verify(listener).onSaveClicked(true)
    }

    @Test
    fun `when autoSend is disabled then 'Finalize' button should be displayed`() {
        val form = FormFixtures.form()
        whenever(formEndViewModel.shouldFormBeSentAutomatically(form)).thenReturn(false)
        val view = FormEndView(context, "blah", form, formEndViewModel, listener)
        assertThat(view.findViewById<MaterialButton>(R.id.finalize).text, equalTo(context.getString(R.string.finalize)))
    }

    @Test
    fun `when autoSend is enabled then 'Send' button should be displayed`() {
        val form = FormFixtures.form()
        whenever(formEndViewModel.shouldFormBeSentAutomatically(form)).thenReturn(true)
        val view = FormEndView(context, "blah", form, formEndViewModel, listener)
        assertThat(view.findViewById<MaterialButton>(R.id.finalize).text, equalTo(context.getString(R.string.send)))
    }
}

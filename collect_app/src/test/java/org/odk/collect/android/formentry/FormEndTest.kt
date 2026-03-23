package org.odk.collect.android.formentry

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.strings.R.string

@RunWith(AndroidJUnit4::class)
class FormEndTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val application = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `form title is displayed correctly`() {
        composeTestRule.setContent {
            FormEnd(
                formTitle = "blah",
                isEditableAfterFinalization = false,
                shouldBeSentAutomatically = true,
                saveAsDraftEnabled = true,
                finalizeEnabled = true
            )
        }

        composeTestRule
            .onNodeWithText(application.getString(string.save_enter_data_description, "blah"))
            .assertIsDisplayed()
    }

    @Test
    fun `when saving drafts is enabled in settings should 'Save as draft' button be visible`() {
        composeTestRule.setContent {
            FormEnd(
                formTitle = "blah",
                isEditableAfterFinalization = false,
                shouldBeSentAutomatically = false,
                saveAsDraftEnabled = true,
                finalizeEnabled = true
            )
        }

        composeTestRule
            .onNodeWithText(application.getString(string.save_as_draft))
            .assertIsDisplayed()
    }

    @Test
    fun `when saving drafts is disabled in settings should 'Save as draft' button be hidden`() {
        composeTestRule.setContent {
            FormEnd(
                formTitle = "blah",
                isEditableAfterFinalization = false,
                shouldBeSentAutomatically = false,
                saveAsDraftEnabled = false,
                finalizeEnabled = true
            )
        }

        composeTestRule
            .onNodeWithText(application.getString(string.save_as_draft))
            .assertIsNotDisplayed()
    }

    @Test
    fun `when 'Save as draft' button is clicked then onSaveClicked is called with false value`() {
        val onSave = mock<(Boolean) -> Unit>()
        composeTestRule.setContent {
            FormEnd(
                formTitle = "blah",
                isEditableAfterFinalization = false,
                shouldBeSentAutomatically = false,
                saveAsDraftEnabled = true,
                finalizeEnabled = true,
                onSave = onSave
            )
        }

        composeTestRule
            .onNodeWithText(application.getString(string.save_as_draft))
            .performClick()

        verify(onSave).invoke(false)
    }

    @Test
    fun `when finalizing forms is enabled in settings should 'Finalize' button be visible`() {
        composeTestRule.setContent {
            FormEnd(
                formTitle = "blah",
                isEditableAfterFinalization = false,
                shouldBeSentAutomatically = false,
                saveAsDraftEnabled = true,
                finalizeEnabled = true
            )
        }

        composeTestRule
            .onNodeWithText(application.getString(string.finalize))
            .assertIsDisplayed()
    }

    @Test
    fun `when finalizing forms is disabled in settings should 'Finalize' button be hidden`() {
        composeTestRule.setContent {
            FormEnd(
                formTitle = "blah",
                isEditableAfterFinalization = false,
                shouldBeSentAutomatically = false,
                saveAsDraftEnabled = true,
                finalizeEnabled = false
            )
        }

        composeTestRule
            .onNodeWithText(application.getString(string.finalize))
            .assertIsNotDisplayed()
    }

    @Test
    fun `when 'Finalize' button is clicked then onSaveClicked is called with true value`() {
        val onSave = mock<(Boolean) -> Unit>()
        composeTestRule.setContent {
            FormEnd(
                formTitle = "blah",
                isEditableAfterFinalization = false,
                shouldBeSentAutomatically = false,
                saveAsDraftEnabled = true,
                finalizeEnabled = true,
                onSave = onSave
            )
        }

        composeTestRule
            .onNodeWithText(application.getString(string.finalize))
            .performClick()

        verify(onSave).invoke(true)
    }

    @Test
    fun `when form should be sent automatically then 'Send' button should be displayed`() {
        composeTestRule.setContent {
            FormEnd(
                formTitle = "blah",
                isEditableAfterFinalization = false,
                shouldBeSentAutomatically = true,
                saveAsDraftEnabled = true,
                finalizeEnabled = true
            )
        }

        composeTestRule
            .onNodeWithText(application.getString(string.send))
            .assertIsDisplayed()
    }

    @Test
    fun `shows warning when form is not editable`() {
        composeTestRule.setContent {
            FormEnd(
                formTitle = "blah",
                isEditableAfterFinalization = false,
                shouldBeSentAutomatically = false,
                saveAsDraftEnabled = true,
                finalizeEnabled = true
            )
        }

        composeTestRule
            .onNodeWithText(application.getString(string.form_editing_disabled_after_finalizing))
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(application.getString(string.form_editing_disabled_hint))
            .assertIsDisplayed()
    }
}
package org.odk.collect.android.formentry

import android.app.Application
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditOff
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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

        composeTestRule
            .onNodeWithText(application.getString(string.send))
            .assertIsNotDisplayed()
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

        composeTestRule
            .onNodeWithText(application.getString(string.finalize))
            .assertIsNotDisplayed()
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
            .onNodeWithTag(EditWarningSemantics.TAG)
            .assert(
                hasContentDescription(
                    warningContentDescription(
                        string.form_editing_disabled_after_finalizing,
                        string.form_editing_disabled_hint
                    )
                )
            )
            .assert(hasIcon(Icons.Default.EditOff))
            .assert(hasErrorBackground(true))
    }

    @Test
    fun `does not show warning message when form is not editable and save as draft is disabled`() {
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
            .onNodeWithTag(EditWarningSemantics.TAG)
            .assert(
                hasContentDescription(application.getString(string.form_editing_disabled_after_finalizing))
            )
            .assert(hasIcon(Icons.Default.EditOff))
            .assert(hasErrorBackground(true))
    }

    @Test
    fun `shows warning when form is not editable after sending`() {
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
            .onNodeWithTag(EditWarningSemantics.TAG)
            .assert(
                hasContentDescription(
                    warningContentDescription(
                        string.form_editing_disabled_after_sending,
                        string.form_editing_disabled_hint
                    )
                )
            )
            .assert(hasIcon(Icons.Default.EditOff))
            .assert(hasErrorBackground(true))
    }

    @Test
    fun `shows warning when form is editable`() {
        composeTestRule.setContent {
            FormEnd(
                formTitle = "blah",
                isEditableAfterFinalization = true,
                shouldBeSentAutomatically = false,
                saveAsDraftEnabled = true,
                finalizeEnabled = true
            )
        }

        composeTestRule
            .onNodeWithTag(EditWarningSemantics.TAG)
            .assert(
                hasContentDescription(
                    warningContentDescription(
                        string.form_editing_enabled_after_finalizing,
                        string.form_editing_enabled_after_finalizing_hint
                    )
                )
            )
            .assert(hasIcon(Icons.Default.Edit))
            .assert(hasErrorBackground(false))
    }

    @Test
    fun `shows warning when form is editable after sending`() {
        composeTestRule.setContent {
            FormEnd(
                formTitle = "blah",
                isEditableAfterFinalization = true,
                shouldBeSentAutomatically = true,
                saveAsDraftEnabled = true,
                finalizeEnabled = true
            )
        }

        composeTestRule
            .onNodeWithTag(EditWarningSemantics.TAG)
            .assert(
                hasContentDescription(
                    warningContentDescription(
                        string.form_editing_enabled_after_sending,
                        string.form_editing_enabled_after_sending_hint
                    )
                )
            )
            .assert(hasIcon(Icons.Default.Edit))
            .assert(hasErrorBackground(false))
    }

    @Test
    fun `does not show warning if finalize is disabled`() {
        composeTestRule.setContent {
            FormEnd(
                formTitle = "blah",
                isEditableAfterFinalization = true,
                shouldBeSentAutomatically = true,
                saveAsDraftEnabled = true,
                finalizeEnabled = false
            )
        }

        composeTestRule
            .onNodeWithTag(EditWarningSemantics.TAG)
            .assertIsNotDisplayed()
    }

    private fun warningContentDescription(title: Int, message: Int): String {
        return "${application.getString(title)}: ${application.getString(message)}"
    }
}

private fun hasIcon(icon: ImageVector): SemanticsMatcher {
    return SemanticsMatcher.expectValue(EditWarningSemantics.iconProperty, icon.name)
}

private fun hasErrorBackground(errorBackground: Boolean): SemanticsMatcher {
    return SemanticsMatcher.expectValue(
        EditWarningSemantics.errorBackgroundProperty,
        errorBackground
    )
}
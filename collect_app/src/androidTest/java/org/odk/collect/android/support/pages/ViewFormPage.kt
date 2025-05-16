package org.odk.collect.android.support.pages

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.contrib.RecyclerViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.odk.collect.android.R
import org.odk.collect.strings.R.string

class ViewFormPage(private val formName: String) : Page<ViewFormPage>() {

    override fun assertOnPage(): ViewFormPage {
        assertToolbarTitle(formName)
        assertText(string.exit)
        return this
    }

    fun clickOnGroup(groupLabel: String): ViewFormPage {
        onView(withId(R.id.list)).perform(scrollTo<RecyclerView.ViewHolder>(
            hasDescendant(withText(groupLabel)))
        )

        clickOnText(groupLabel)
        return this
    }

    fun editForm(formName: String): FormHierarchyPage {
        clickOnContentDescription(string.edit_finalized_form)
        checkIsSnackbarWithMessageDisplayed(string.finalized_form_edit_started)
        return FormHierarchyPage(formName).assertOnPage()
    }

    fun editFormWithError(): ViewFormPage {
        clickOnContentDescription(string.edit_finalized_form)
        return this
    }

    fun acceptEditingNewerDraftEdit(formName: String): FormHierarchyPage {
        clickOnTextInDialog(string.newer_draft_edit_found_dialog_positive_button)
        return FormHierarchyPage(formName).assertOnPage()
    }

    fun acceptEditingNewerFinalizedEdit(formName: String): FormHierarchyPage {
        clickOnTextInDialog(string.newer_finalized_edit_found_dialog_positive_button)
        checkIsSnackbarWithMessageDisplayed(string.finalized_form_edit_started)
        return FormHierarchyPage(formName).assertOnPage()
    }

    fun discardEditingNewerEdit(): ViewFormPage {
        clickOnTextInDialog(string.cancel)
        return this
    }

    fun assertNonEditableForm(): ViewFormPage {
        onView(withContentDescription(string.edit_finalized_form)).check(doesNotExist())
        return this
    }
}

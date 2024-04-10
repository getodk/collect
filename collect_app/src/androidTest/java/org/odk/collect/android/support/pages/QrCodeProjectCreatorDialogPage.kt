package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText

class QrCodeProjectCreatorDialogPage : Page<QrCodeProjectCreatorDialogPage>() {
    override fun assertOnPage(): QrCodeProjectCreatorDialogPage {
        assertText(org.odk.collect.strings.R.string.add_project)
        return this
    }

    fun switchToManualMode(): ManualProjectCreatorDialogPage {
        return clickOnTextInDialog(org.odk.collect.strings.R.string.configure_manually, ManualProjectCreatorDialogPage())
    }

    fun assertDuplicateDialogShown(): QrCodeProjectCreatorDialogPage {
        onView(withText(getTranslatedString(org.odk.collect.strings.R.string.duplicate_project_details)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        return this
    }

    fun switchToExistingProject(): MainMenuPage {
        return clickOnTextInDialog(org.odk.collect.strings.R.string.switch_to_existing, MainMenuPage())
    }

    fun addDuplicateProject(): MainMenuPage {
        return clickOnTextInDialog(org.odk.collect.strings.R.string.add_duplicate_project, MainMenuPage())
    }
}

package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.odk.collect.android.R

class QrCodeProjectCreatorDialogPage : Page<QrCodeProjectCreatorDialogPage>() {
    override fun assertOnPage(): QrCodeProjectCreatorDialogPage {
        assertText(R.string.add_project)
        return this
    }

    fun switchToManualMode(): ManualProjectCreatorDialogPage {
        clickOnString(R.string.configure_manually)
        return ManualProjectCreatorDialogPage().assertOnPage()
    }

    fun assertDuplicateDialogShown(): QrCodeProjectCreatorDialogPage {
        onView(withText(getTranslatedString(R.string.duplicate_project_details)))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        return this
    }

    fun switchToExistingProject(): MainMenuPage {
        return clickOnButtonInDialog(R.string.switch_to_existing, MainMenuPage())
    }

    fun addDuplicateProject(): MainMenuPage {
        return clickOnButtonInDialog(R.string.add_duplicate_project, MainMenuPage())
    }
}

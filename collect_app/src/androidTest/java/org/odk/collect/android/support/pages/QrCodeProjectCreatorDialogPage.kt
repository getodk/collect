package org.odk.collect.android.support.pages

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
        assertText(R.string.duplicate_project_details)
        return this
    }

    fun switchToExistingProject(): MainMenuPage {
        clickOnString(R.string.switch_to_existing)
        return MainMenuPage().assertOnPage()
    }

    fun addDuplicateProject(): MainMenuPage {
        clickOnString(R.string.add_duplicate_project)
        return MainMenuPage().assertOnPage()
    }
}

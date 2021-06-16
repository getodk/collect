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
}

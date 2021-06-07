package org.odk.collect.android.support.pages

import org.odk.collect.android.R

class AutomaticProjectCreatorDialogPage : Page<AutomaticProjectCreatorDialogPage>() {
    override fun assertOnPage(): AutomaticProjectCreatorDialogPage {
        assertText(R.string.add_project)
        return this
    }

    fun switchToManualMode(): ManualProjectCreatorDialogPage {
        clickOnString(R.string.configure_manually)
        return ManualProjectCreatorDialogPage().assertOnPage()
    }
}

package org.odk.collect.android.support.pages

import org.odk.collect.android.R

class FirstLaunchPage : Page<FirstLaunchPage>() {

    override fun assertOnPage(): FirstLaunchPage {
        assertText(R.string.configure_with_qr_code)
        return this
    }

    fun clickTryCollect(): MainMenuPage {
        clickOnString(R.string.configure_later)
        return MainMenuPage().assertOnPage()
    }

    fun clickManuallyEnterProjectDetails(): AddProjectDialogPage {
        clickOnString(R.string.configure_manually)
        return AddProjectDialogPage().assertOnPage()
    }
}

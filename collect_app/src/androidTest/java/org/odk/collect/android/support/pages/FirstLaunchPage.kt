package org.odk.collect.android.support.pages

import org.odk.collect.android.R

class FirstLaunchPage : Page<FirstLaunchPage>() {

    override fun assertOnPage(): FirstLaunchPage {
        assertText(org.odk.collect.strings.R.string.configure_with_qr_code)
        return this
    }

    fun clickTryCollect(): MainMenuPage {
        scrollToAndClickText(org.odk.collect.strings.R.string.try_demo)
        return MainMenuPage().assertOnPage()
    }

    fun clickManuallyEnterProjectDetails(): ManualProjectCreatorDialogPage {
        scrollToAndClickText(org.odk.collect.strings.R.string.configure_manually)
        return ManualProjectCreatorDialogPage().assertOnPage()
    }

    fun clickConfigureWithQrCode(): QrCodeProjectCreatorDialogPage {
        scrollToAndClickText(org.odk.collect.strings.R.string.configure_with_qr_code)
        return QrCodeProjectCreatorDialogPage().assertOnPage()
    }
}

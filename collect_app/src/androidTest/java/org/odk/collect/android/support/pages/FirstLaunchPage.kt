package org.odk.collect.android.support.pages

class FirstLaunchPage : Page<FirstLaunchPage>() {

    override fun assertOnPage(): FirstLaunchPage {
        assertText(org.odk.collect.strings.R.string.configure_with_qr_code)
        return this
    }

    fun clickTryCollect(): MainMenuPage {
        return tryAgainOnFail(MainMenuPage()) {
            scrollToAndClickSubtext(org.odk.collect.strings.R.string.try_demo)
        }
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

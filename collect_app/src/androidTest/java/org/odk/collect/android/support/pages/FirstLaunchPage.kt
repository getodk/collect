package org.odk.collect.android.support.pages

import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import org.odk.collect.android.support.Interactions
import org.odk.collect.strings.R.string

class FirstLaunchPage : Page<FirstLaunchPage>() {

    override fun assertOnPage(): FirstLaunchPage {
        assertText(string.configure_with_qr_code)
        return this
    }

    fun clickTryCollect(): MainMenuPage {
        Interactions.clickOn(withSubstring(getTranslatedString(string.try_demo))) {
            MainMenuPage().assertOnPage()
        }

        return MainMenuPage()
    }

    fun clickManuallyEnterProjectDetails(): ManualProjectCreatorDialogPage {
        return clickOnString(
            string.configure_manually,
            ManualProjectCreatorDialogPage()
        )
    }

    fun clickConfigureWithQrCode(): QrCodeProjectCreatorDialogPage {
        return clickOnString(
            string.configure_with_qr_code,
            QrCodeProjectCreatorDialogPage()
        )
    }
}

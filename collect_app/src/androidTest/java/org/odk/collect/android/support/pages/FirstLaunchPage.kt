package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withSubstring
import org.odk.collect.strings.R.string

class FirstLaunchPage : Page<FirstLaunchPage>() {

    override fun assertOnPage(): FirstLaunchPage {
        assertText(string.configure_with_qr_code)
        return this
    }

    fun clickTryCollect(): MainMenuPage {
        return tryAgainOnFail(MainMenuPage()) {
            try {
                onView(withSubstring(getTranslatedString(string.try_demo))).perform(click())
            } catch (e: Exception) {
                onView(withSubstring(getTranslatedString(string.try_demo)))
                    .perform(scrollTo(), click())
            }
        }
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

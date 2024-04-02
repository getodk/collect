package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.odk.collect.strings.R

class SavepointRecoveryDialogPage : Page<SavepointRecoveryDialogPage>() {
    override fun assertOnPage(): SavepointRecoveryDialogPage {
        val title = getTranslatedString(R.string.savepoint_recovery_dialog_title)
        onView(withText(title)).inRoot(isDialog()).check(matches(isDisplayed()))
        return this
    }

    fun <D : Page<D>> clickRecover(destination: D): D {
        return this.clickOnTextInDialog(R.string.recover, destination)
    }

    fun <D : Page<D>> clickDoNotRecover(destination: D): D {
        return this.clickOnTextInDialog(R.string.do_not_recover, destination)
    }
}

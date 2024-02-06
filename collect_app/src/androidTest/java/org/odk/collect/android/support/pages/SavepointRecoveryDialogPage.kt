package org.odk.collect.android.support.pages

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.odk.collect.strings.R
import org.odk.collect.strings.localization.getLocalizedString

class SavepointRecoveryDialogPage : Page<SavepointRecoveryDialogPage>() {
    override fun assertOnPage(): SavepointRecoveryDialogPage {
        val title = ApplicationProvider
            .getApplicationContext<Application>()
            .getLocalizedString(R.string.savepoint_recovery_dialog_title)
        onView(withText(title)).inRoot(isDialog()).check(matches(isDisplayed()))
        return this
    }

    fun clickRecover(formName: String): FormHierarchyPage {
        return this.clickOnButtonInDialog(R.string.recover, FormHierarchyPage(formName))
    }

    fun clickDoNotRecover(formName: String): FormHierarchyPage {
        return this.clickOnButtonInDialog(R.string.do_not_recover, FormHierarchyPage(formName))
    }
}

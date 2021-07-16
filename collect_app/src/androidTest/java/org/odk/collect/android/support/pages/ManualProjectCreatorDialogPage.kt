package org.odk.collect.android.support.pages

import android.accounts.AccountManager
import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.odk.collect.android.R

class ManualProjectCreatorDialogPage : Page<ManualProjectCreatorDialogPage>() {
    override fun assertOnPage(): ManualProjectCreatorDialogPage {
        assertText(R.string.add_project)
        return this
    }

    fun inputUrl(url: String): ManualProjectCreatorDialogPage {
        inputText(R.string.server_url, url)
        return this
    }

    fun inputUsername(username: String): ManualProjectCreatorDialogPage {
        inputText(R.string.username, username)
        return this
    }

    fun inputPassword(password: String): ManualProjectCreatorDialogPage {
        inputText(R.string.password, password)
        return this
    }

    fun openGooglePickerAndSelect(googleAccount: String): MainMenuPage {
        val data = Intent()
        data.putExtra(AccountManager.KEY_ACCOUNT_NAME, googleAccount)
        val activityResult = Instrumentation.ActivityResult(Activity.RESULT_OK, data)
        intending(hasAction("PICK_GOOGLE_ACCOUNT")).respondWith(activityResult)

        onView(withText(R.string.gdrive_configure)).perform(scrollTo(), click())
        return MainMenuPage().assertOnPage()
    }

    fun addProject(): MainMenuPage {
        onView(withText(R.string.add)).perform(click())
        return MainMenuPage().assertOnPage()
    }

    fun addProjectAndAssertDuplicateDialogShown(): ManualProjectCreatorDialogPage {
        onView(withText(R.string.add)).perform(click())
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

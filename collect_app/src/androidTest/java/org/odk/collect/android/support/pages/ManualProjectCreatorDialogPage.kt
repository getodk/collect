package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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

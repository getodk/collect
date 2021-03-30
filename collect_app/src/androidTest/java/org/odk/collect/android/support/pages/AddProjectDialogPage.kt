package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import org.odk.collect.android.R

internal class AddProjectDialogPage(rule: ActivityTestRule<*>) : Page<AddProjectDialogPage>(rule) {
    override fun assertOnPage(): AddProjectDialogPage {
        assertText(R.string.add_project)
        return this
    }

    fun inputProjectName(projectName: String?): AddProjectDialogPage {
        inputText(R.string.project_name, projectName)
        return this
    }

    fun addProject(): MainMenuPage {
        Espresso.onView(withId(R.id.add_button)).perform(ViewActions.click())
        return MainMenuPage(rule).assertOnPage()
    }
}

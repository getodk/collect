package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers.allOf
import org.odk.collect.android.R

internal class ProjectSettingsDialogPage() : Page<ProjectSettingsDialogPage>() {
    override fun assertOnPage(): ProjectSettingsDialogPage {
        assertText(R.string.projects)
        return this
    }

    fun clickGeneralSettings(): GeneralSettingsPage {
        clickOnString(R.string.general_preferences)
        return GeneralSettingsPage().assertOnPage()
    }

    fun clickAdminSettings(): AdminSettingsPage {
        clickOnString(R.string.admin_preferences)
        return AdminSettingsPage().assertOnPage()
    }

    fun clickAdminSettingsWithPassword(password: String?): AdminSettingsPage {
        clickOnString(R.string.admin_preferences)
        inputText(password)
        clickOKOnDialog()
        return AdminSettingsPage().assertOnPage()
    }

    fun clickAbout(): AboutPage {
        clickOnString(R.string.about_preferences)
        return AboutPage().assertOnPage()
    }

    fun clickAddProject(): AutomaticProjectCreatorDialogPage {
        onView(withId(R.id.add_project_button)).perform(ViewActions.click())
        return AutomaticProjectCreatorDialogPage().assertOnPage()
    }

    fun assertCurrentProject(projectName: String, subtext: String): ProjectSettingsDialogPage {
        onView(allOf(hasDescendant(withText(projectName)), hasDescendant(withText(subtext)), withContentDescription(getTranslatedString(R.string.using_project, projectName)))).check(matches(isDisplayed()))
        return this
    }

    fun assertInactiveProject(projectName: String, subtext: String): ProjectSettingsDialogPage {
        onView(allOf(hasDescendant(withText(projectName)), hasDescendant(withText(subtext)), withContentDescription(getTranslatedString(R.string.switch_to_project, projectName)))).check(matches(isDisplayed()))
        return this
    }

    fun selectProject(projectName: String): MainMenuPage {
        clickOnText(projectName)
        return MainMenuPage().assertOnPage()
    }
}

package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.rule.ActivityTestRule
import org.hamcrest.Matchers.allOf
import org.odk.collect.android.R

internal class ProjectSettingsDialogPage(rule: ActivityTestRule<*>) : Page<ProjectSettingsDialogPage>(rule) {
    override fun assertOnPage(): ProjectSettingsDialogPage {
        assertText(R.string.projects)
        return this
    }

    fun clickGeneralSettings(): GeneralSettingsPage {
        clickOnString(R.string.general_preferences)
        return GeneralSettingsPage(rule).assertOnPage()
    }

    fun clickAdminSettings(): AdminSettingsPage {
        clickOnString(R.string.admin_preferences)
        return AdminSettingsPage(rule).assertOnPage()
    }

    fun clickAdminSettingsWithPassword(password: String?): AdminSettingsPage {
        clickOnString(R.string.admin_preferences)
        inputText(password)
        clickOKOnDialog()
        return AdminSettingsPage(rule).assertOnPage()
    }

    fun clickAbout(): AboutPage {
        clickOnString(R.string.about_preferences)
        return AboutPage(rule).assertOnPage()
    }

    fun clickAddProject(): AddProjectDialogPage {
        onView(withId(R.id.add_project_button)).perform(ViewActions.click())
        return AddProjectDialogPage(rule).assertOnPage()
    }

    fun assertCurrentProject(name: String): ProjectSettingsDialogPage {
        onView(allOf(hasDescendant(withText(name)), withContentDescription(getTranslatedString(R.string.using_project, name)))).check(matches(isDisplayed()))
        return this
    }

    fun assertInactiveProject(name: String): ProjectSettingsDialogPage {
        onView(allOf(hasDescendant(withText(name)), withContentDescription(getTranslatedString(R.string.switch_to_project, name)))).check(matches(isDisplayed()))
        return this
    }
}

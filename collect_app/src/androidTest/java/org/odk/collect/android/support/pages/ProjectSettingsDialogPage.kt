package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.rule.ActivityTestRule
import org.odk.collect.android.R
import org.odk.collect.android.projects.Project
import org.odk.collect.android.support.matchers.ProjectMatcher.withProject

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
        Espresso.onView(withId(R.id.add_project_button)).perform(ViewActions.click())
        return AddProjectDialogPage(rule).assertOnPage()
    }

    fun assertCurrentProject(project: Project): ProjectSettingsDialogPage {
        Espresso.onView(withId(R.id.current_project)).check(matches(withProject(project)))
        return this
    }

    fun assertInactiveProject(project: Project): ProjectSettingsDialogPage {
        Espresso.onView(withId(R.id.project_item)).check(matches(withProject(project)))
        return this
    }
}

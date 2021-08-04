package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.odk.collect.android.R

internal class ProjectSettingsDialogPage() : Page<ProjectSettingsDialogPage>() {
    override fun assertOnPage(): ProjectSettingsDialogPage {
        assertText(R.string.projects)
        return this
    }

    fun clickGeneralSettings(): ProjectSettingsPage {
        clickOnString(R.string.settings)
        return ProjectSettingsPage().assertOnPage()
    }

    fun clickAbout(): AboutPage {
        clickOnString(R.string.about_preferences)
        return AboutPage().assertOnPage()
    }

    fun clickAddProject(): QrCodeProjectCreatorDialogPage {
        onView(withId(R.id.add_project_button)).perform(ViewActions.click())
        return QrCodeProjectCreatorDialogPage().assertOnPage()
    }

    fun assertCurrentProject(projectName: String, subtext: String): ProjectSettingsDialogPage {
        onView(allOf(hasDescendant(withText(projectName)), hasDescendant(withText(subtext)), withContentDescription(getTranslatedString(R.string.using_project, projectName)))).check(matches(isDisplayed()))
        return this
    }

    fun assertInactiveProject(projectName: String, subtext: String): ProjectSettingsDialogPage {
        onView(allOf(hasDescendant(withText(projectName)), hasDescendant(withText(subtext)), withContentDescription(getTranslatedString(R.string.switch_to_project, projectName)))).check(matches(isDisplayed()))
        return this
    }

    fun assertNotInactiveProject(projectName: String): ProjectSettingsDialogPage {
        onView(allOf(hasDescendant(withText(projectName)), withContentDescription(getTranslatedString(R.string.switch_to_project, projectName)))).check(doesNotExist())
        return this
    }

    fun selectProject(projectName: String): MainMenuPage {
        onView(allOf(hasDescendant(withText(projectName)), withContentDescription(getTranslatedString(R.string.switch_to_project, projectName)))).perform(click())
        return MainMenuPage().assertOnPage()
    }
}

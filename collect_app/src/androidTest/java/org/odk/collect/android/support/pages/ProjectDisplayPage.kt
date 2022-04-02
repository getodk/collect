package org.odk.collect.android.support.pages

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import org.odk.collect.android.R

class ProjectDisplayPage : Page<ProjectDisplayPage>() {
    override fun assertOnPage(): ProjectDisplayPage {
        assertText(R.string.project_display_title)
        return this
    }

    fun setProjectName(projectName: String?): ProjectDisplayPage {
        clickOnString(R.string.project_name)
        inputText(projectName)
        clickOKOnDialog()
        return this
    }

    fun setProjectIcon(projectIcon: String): ProjectDisplayPage {
        clickOnString(R.string.project_icon)
        inputText(projectIcon)
        clickOKOnDialog()
        return this
    }

    fun setProjectColor(projectColor: String?): ProjectDisplayPage {
        clickOnString(R.string.project_color)
        onView(withContentDescription(R.string.hex_color)).perform(replaceText(projectColor))
        clickOKOnDialog()
        return this
    }
}

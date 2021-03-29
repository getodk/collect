package org.odk.collect.android.feature.projects

import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.odk.collect.android.activities.MainMenuActivity
import org.odk.collect.android.support.pages.MainMenuPage

class AddNewProjectTest {

    @get:Rule var rule = ActivityTestRule(MainMenuActivity::class.java)

    @Test
    fun addProjectTest() {
        MainMenuPage(rule)
            .openProjectSettingsDialog()
            .clickAddProject()
            .inputProjectName("Project 1")
            .addProject()

        MainMenuPage(rule)
            .openProjectSettingsDialog()
            .assertText("Project 1")
    }
}

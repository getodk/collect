package org.odk.collect.android.feature.projects

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

class UpdateProjectTest {

    val rule = CollectTestRule()

    @get:Rule
    var chain: RuleChain = TestRuleChain
        .chain()
        .around(rule)

    @Test
    fun updateProjectTest() {
        rule.startAtMainMenu()
            .assertProjectIcon("D")
            .openProjectSettingsDialog()
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .clickSettings()
            .clickProjectDisplay()
            .setProjectName("Project X")
            .assertFileWithProjectNameUpdated("Demo project", "Project X")
            .setProjectIcon("XY")
            .setProjectColor("cccccc")
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .openProjectSettingsDialog()
            .clickSettings()
            .clickServerSettings()
            .clickServerUsername()
            .inputText("Anna")
            .clickOKOnDialog()
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())

            .assertProjectIcon("X")
            .openProjectSettingsDialog()
            .assertCurrentProject("Project X", "Anna / demo.getodk.org")
    }

    @Test
    fun updateProjectName_updatesProjectNameFileSanitizingIt() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickProjectDisplay()
            .setProjectName("Project<>")
            .assertFileWithProjectNameUpdated("Demo project", "Project__")
            .setProjectName(":*Project<>")
            .assertFileWithProjectNameUpdated("Project__", "__Project__")
    }
}

package org.odk.collect.android.feature.projects

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage

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
            .openProjectSettings()
            .assertCurrentProject("Demo project", "demo.getodk.org")
            .clickGeneralSettings()
            .clickProjectDisplay()
            .setProjectName("Project X")
            .assertFileWithProjectNameUpdated("Demo project", "Project X")
            .setProjectIcon("XY")
            .setProjectColor("cccccc")
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .openProjectSettings()
            .clickGeneralSettings()
            .clickServerSettings()
            .clickServerUsername()
            .inputText("Anna")
            .clickOKOnDialog()
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())

            .assertProjectIcon("X")
            .openProjectSettings()
            .assertCurrentProject("Project X", "Anna / demo.getodk.org")
    }
}

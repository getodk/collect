package org.odk.collect.android.feature.projects

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.projects.Project
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.SaveProjectsRule
import org.odk.collect.android.support.TestRuleChain

class SwitchProjectTest {

    val rule = CollectTestRule()
    private val projects = listOf(
        Project("Turtle nesting", "T", "#00FF00", "1"),
        Project("Polio - Banadir", "P", "#FF0000", "2")
    )

    @get:Rule
    var chain: RuleChain = TestRuleChain
        .chain()
        .around(SaveProjectsRule(projects))
        .around(rule)

    @Test
    fun switchProjectTest() {
        rule.mainMenu()
            .openProjectSettingsDialog()
            .assertCurrentProject(projects[0])
            .assertInactiveProject(projects[1])
            .clickOnText("Polio - Banadir")
            .checkIsToastWithMessageDisplayed(R.string.switched_project, "Polio - Banadir")

        rule.mainMenu()
            .openProjectSettingsDialog()
            .assertCurrentProject(projects[1])
            .assertInactiveProject(projects[0])
    }
}

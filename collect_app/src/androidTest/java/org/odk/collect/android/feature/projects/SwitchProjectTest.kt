package org.odk.collect.android.feature.projects

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestRuleChain

class SwitchProjectTest {

    val rule = CollectTestRule()

    @get:Rule
    var chain: RuleChain = TestRuleChain
        .chain()
        .around(rule)

    @Test
    fun switchProjectTest() {
        rule.mainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()
            .inputProjectName("Turtle nesting")
            .inputProjectIcon("T")
            .inputProjectColor("#0000FF")
            .addProject()

            .openProjectSettingsDialog()
            .clickAddProject()
            .inputProjectName("Polio - Banadir")
            .inputProjectIcon("P")
            .inputProjectColor("#0000FF")
            .addProject()

            .openProjectSettingsDialog()
            .clickOnText("Polio - Banadir")
            .checkIsToastWithMessageDisplayed(R.string.switched_project, "Polio - Banadir")

        rule.mainMenu()
            .openProjectSettingsDialog()
            .assertCurrentProject("Polio - Banadir")
            .assertInactiveProject("Turtle nesting")
    }
}

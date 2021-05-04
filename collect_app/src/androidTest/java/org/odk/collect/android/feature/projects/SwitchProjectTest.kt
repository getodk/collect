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
    fun canSwitchActiveProjectToAnotherInList() {
        // Add project Turtle nesting
        rule.mainMenu()
            .assertProjectIcon("D", "#3e9fcc")
            .openProjectSettings()
            .clickAddProject()
            .inputProjectName("Turtle nesting")
            .inputProjectIcon("T")
            .inputProjectColor("#0000FF")
            .addProject()

        // Switch to Turtle nesting
        rule.mainMenu()
            .openProjectSettings()
            .assertCurrentProject("Demo project")
            .assertInactiveProject("Turtle nesting")
            .clickOnText("Turtle nesting")
        rule.mainMenu()
            .checkIsToastWithMessageDisplayed(R.string.switched_project, "Turtle nesting")
            .assertProjectIcon("T", "#0000FF")
    }
}

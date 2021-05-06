package org.odk.collect.android.feature.projects

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.R
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestRuleChain

class DeleteProjectTest {

    val rule = CollectTestRule()

    @get:Rule
    var chain: RuleChain = TestRuleChain
        .chain()
        .around(rule)

    @Test
    fun deleteProjectTest() {
        // Add project Turtle nesting
        rule.mainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()
            .inputProjectName("Turtle nesting")
            .inputProjectIcon("T")
            .inputProjectColor("#0000FF")
            .addProject()

        // Delete Demo project
        rule.mainMenu()
            .openProjectSettingsDialog()
            .clickAdminSettings()
            .deleteProject()

        // Assert switching to Turtle nesting
        rule.mainMenu()
            .checkIsToastWithMessageDisplayed(R.string.switched_project, "Turtle nesting")
            .assertProjectIcon("T", "#0000FF")

        // Delete Turtle nesting project
        rule.mainMenu()
            .openProjectSettingsDialog()
            .clickAdminSettings()
            .deleteLastProject()
    }
}

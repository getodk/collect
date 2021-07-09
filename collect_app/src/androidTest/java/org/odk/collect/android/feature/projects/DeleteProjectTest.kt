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
        rule.startAtMainMenu()
            .openProjectSettings()
            .clickAddProject()
            .switchToManualMode()
            .inputUrl("https://my-server.com")
            .inputUsername("John")
            .addProject()

            // Delete Turtle nesting project
            .openProjectSettings()
            .clickGeneralSettings()
            .clickProjectManagement()
            .deleteProject()

            // Assert switching to Turtle nesting
            .checkIsToastWithMessageDisplayed(R.string.switched_project, "Demo project")
            .assertProjectIcon("D")

            // Delete Demo project
            .openProjectSettings()
            .clickGeneralSettings()
            .clickProjectManagement()
            .deleteLastProject()
    }
}

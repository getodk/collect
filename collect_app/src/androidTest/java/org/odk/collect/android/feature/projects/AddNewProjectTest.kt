package org.odk.collect.android.feature.projects

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestRuleChain

class AddNewProjectTest {

    val rule = CollectTestRule()

    @get:Rule var chain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun addProjectTest() {
        rule.mainMenu()
            .openProjectSettingsDialog()
            .clickAddProject()
            .inputProjectName("Project 1")
            .addProject()

        rule.mainMenu()
            .openProjectSettingsDialog()
            .assertText("Project 1")
    }
}

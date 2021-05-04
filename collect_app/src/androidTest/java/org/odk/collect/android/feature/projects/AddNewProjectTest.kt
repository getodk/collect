package org.odk.collect.android.feature.projects

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestRuleChain

class AddNewProjectTest {

    val rule = CollectTestRule()

    @get:Rule
    var chain: RuleChain = TestRuleChain.chain(false).around(rule)

    @Test
    fun addingProject_addsNewProject() {
        rule.mainMenu()
            .openProjectSettings()
            .clickAddProject()
            .inputProjectName("Project 1")
            .inputProjectIcon("X")
            .inputProjectColor("#0000FF")
            .addProject()

        rule.mainMenu()
            .openProjectSettings()
            .assertInactiveProject("Project 1")
    }
}

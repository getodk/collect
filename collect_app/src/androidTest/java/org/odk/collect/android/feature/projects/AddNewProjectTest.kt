package org.odk.collect.android.feature.projects

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestRuleChain

class AddNewProjectTest {

    val rule = CollectTestRule()

    @get:Rule
    var chain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun addingProject_addsNewProject() {
        rule.startAtMainMenu()
            .openProjectSettings()
            .clickAddProject()
            .inputUrl("https://my-server.com")
            .inputUsername("John")
            .addProject()

            .openProjectSettings()
            .assertInactiveProject("my-server.com", "John / my-server.com")
    }
}

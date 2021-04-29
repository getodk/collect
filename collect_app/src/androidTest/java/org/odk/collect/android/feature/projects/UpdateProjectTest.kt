package org.odk.collect.android.feature.projects

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.pressBack
import androidx.test.espresso.matcher.ViewMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestRuleChain

class UpdateProjectTest {

    val rule = CollectTestRule()

    @get:Rule
    var chain: RuleChain = TestRuleChain
        .chain()
        .around(rule)

    @Test
    fun updateProjectTest() {
        rule.mainMenu()
            .assertProjectIcon("D", "#3e9fcc")
            .openProjectSettingsDialog()
            .assertCurrentProject("Demo project")
            .clickAdminSettings()
            .setProjectName("Project X")
            .setProjectIcon("X")
            .setProjectColor("#cccccc")

        onView(ViewMatchers.isRoot()).perform(pressBack())

        rule.mainMenu()
            .assertProjectIcon("X", "#cccccc")
            .openProjectSettingsDialog()
            .assertCurrentProject("Project X")
    }
}

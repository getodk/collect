package org.odk.collect.android.feature.projects

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestRuleChain

@RunWith(AndroidJUnit4::class)
class LaunchScreenTest {

    private val rule = CollectTestRule(false)

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain()
        .around(rule)

    @Test
    fun clickingTryCollectAtLaunch_setsAppUpWithDemoProject() {
        rule.startAtFirstLaunch()
            .clickTryCollect()
            .openProjectSettings()
            .assertCurrentProject("Demo project")
            .clickGeneralSettings()
            .clickServerSettings()
            .clickOnURL()
            .assertText("https://demo.getodk.org")
    }

    @Test
    fun clickingManuallyEnter_andAddingProjectDetails_setsAppUpWithProjectDetails() {
        rule.startAtFirstLaunch()
            .clickManuallyEnterProjectDetails()
            .inputProjectName("Strange Days")
            .inputProjectIcon("S")
            .inputProjectColor("#FA8072")
            .addProject()
            .assertProjectIcon("S", "#FA8072")
            .openProjectSettings()
            .assertCurrentProject("Strange Days")
    }
}

package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

@RunWith(AndroidJUnit4::class)
class GuidanceTest {

    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    val ruleChain: RuleChain = chain(testDependencies).around(rule)

    @Test
    fun guidanceForQuestion_ShouldBeHiddenIfNotSelectedInSettings() {
        rule.withProject(testDependencies.server, "hints_textq.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .openFormManagement()
            .openShowGuidanceForQuestions()
            .clickOnString(org.odk.collect.strings.R.string.guidance_no)
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())

            .startBlankForm("hints textq")
            .assertText("Hint 1")
            .checkIfElementIsGone(R.id.help_icon)
            .assertTextDoesNotExist("1 very very very very very very very very very very long text")
    }

    @Test
    fun guidanceForQuestion_ShouldBeFullyDisplayedIfAlwaysShownEnabledInSettings() {
        rule.withProject(testDependencies.server, "hints_textq.xml")
            .openProjectSettingsDialog()
            .clickSettings()
            .openFormManagement()
            .openShowGuidanceForQuestions()
            .clickOnString(org.odk.collect.strings.R.string.guidance_yes)
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())

            .startBlankForm("hints textq")
            .assertText("Hint 1")
            .checkIfElementIsGone(R.id.help_icon)
            .assertText("1 very very very very very very very very very very long text")
    }

    @Test
    fun guidanceForQuestion_ShouldBeCollapsedByDefault() {
        rule.withProject(testDependencies.server, "hints_textq.xml")
            .startBlankForm("hints textq")
            .assertText("Hint 1")
            .checkIsIdDisplayed(R.id.help_icon)
            .assertTextDoesNotExist("1 very very very very very very very very very very long text")
            .clickOnText("Hint 1")
            .assertText("1 very very very very very very very very very very long text")
    }
}

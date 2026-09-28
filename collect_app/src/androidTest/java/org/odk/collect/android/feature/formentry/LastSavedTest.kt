package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

/**
 * Tests the [`last-saved` "virtual endpoint" for the last saved instance of a form](https://getodk.github.io/xforms-spec/#virtual-endpoints).
 */
@RunWith(AndroidJUnit4::class)
class LastSavedTest {

    private val rule = CollectTestRule(useDemoProject = false)
    private val testDependencies = TestDependencies()

    @get:Rule
    var ruleChain: RuleChain = chain(testDependencies).around(rule)

    @Test
    fun valuesFromLastSavedInstanceCanBeUsedInForm() {
        testDependencies.server.addForm("one-question-last-saved.xml")

        rule.withProject(testDependencies.server, matchExactly = true)
            .startBlankForm("One Question Last Saved")
            .fillOutAndFinalize(
                FormEntryPage.QuestionAndAnswer("what is your age", "36")
            )

            .startBlankForm("One Question Last Saved")
            .assertText("36")
    }

    @Test
    fun carriesOverBetweenFormVersions() {
        testDependencies.server.addForm(
            "One Question Last Saved",
            "one_question_last_saved",
            "1",
            "one-question-last-saved.xml"
        )

        val mainMenuPage = rule.withProject(testDependencies.server.url, matchExactly = true)
            .startBlankForm("One Question Last Saved")
            .fillOutAndFinalize(
                FormEntryPage.QuestionAndAnswer("what is your age", "32")
            )

        testDependencies.server.removeForm("One Question Last Saved")
        testDependencies.server.addForm(
            "One Question Last Saved",
            "one_question_last_saved",
            "2",
            "one-question-last-saved-updated.xml"
        )

        mainMenuPage.clickFillBlankForm()
            .clickRefresh()
            .clickOnForm("One Question Last Saved")
            .assertText("32")
    }
}

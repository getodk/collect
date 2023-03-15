package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.pages.FormEntryPage
import org.odk.collect.android.support.rules.FormActivityTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class SavePointTest {

    private val rule = FormActivityTestRule("two-question.xml", "Two Question")

    @get:Rule
    val ruleChain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun savePointIsCreateWhenMovingForwardInForm() {
        rule.startInFormEntry()
            .answerQuestion("What is your name?", "Alexei")
            .swipeToNextQuestion("What is your age?")
            .answerQuestion("What is your age?", "46")
            .let { simulateBatteryDeath() }

            .startInFormHierarchy()
            .assertText("Alexei")
            .assertTextDoesNotExist("46")
            .pressBack(FormEntryPage("Two Question"))
            .assertQuestion("What is your name?")
    }

    @Test
    fun savePointIsCreatedWhenLeavingTheApp() {
        rule.startInFormEntry()
            .answerQuestion("What is your name?", "Alexei")
            .let { simulateProcessRestore() }

            .startInFormHierarchy()
            .assertText("Alexei")
            .pressBack(FormEntryPage("Two Question"))
            .assertQuestion("What is your name?")
    }

    /**
     * Simulates a case where the process is killed without lifecycle clean up (like a phone
     * being battery dying).
     */
    private fun simulateBatteryDeath(): FormActivityTestRule {
        return rule.restartProcess()
    }

    /**
     * Simulate a "process restore" case where an app in the background is killed by Android
     * to reclaim memory, change permissions etc
     */
    private fun simulateProcessRestore(): FormActivityTestRule {
        return rule.saveInstanceStateForActivity()
            .destroyActivity()
            .restartProcess()
    }
}

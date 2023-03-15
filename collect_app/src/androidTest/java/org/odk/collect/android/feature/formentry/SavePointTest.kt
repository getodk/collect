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
            .let { rule.destroyActivity() }
            .restartProcess()

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
            .let { rule.saveInstanceStateForActivity().destroyActivity() }
            .restartProcess()

            .startInFormHierarchy()
            .assertText("Alexei")
            .pressBack(FormEntryPage("Two Question"))
            .assertQuestion("What is your name?")
    }
}

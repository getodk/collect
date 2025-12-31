package org.odk.collect.android.feature.formentry

import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

@RunWith(AndroidJUnit4::class)
class SaveIncompleteTest {
    private val composeRule = createEmptyComposeRule()
    var rule = CollectTestRule()

    @get:Rule
    var chain: RuleChain = chain()
        .around(composeRule)
        .around(rule)

    @Test
    fun viewingSaveIncompleteQuestion_savesCurrentAnswers() {
        rule.startAtMainMenu()
            .copyForm("two-question-save-incomplete.xml")
            .startBlankForm("Two Question Save Incomplete")
            .answerQuestion("What is your name?", "Dez")
            .swipeToNextQuestion("[saveIncomplete] What is your age?")
            .pressBackAndDiscardChanges()

            .clickDrafts(1)
            .clickOnForm("Two Question Save Incomplete")
            .assertText("Dez", composeRule)
    }

    @Test
    fun viewingSaveIncompleteQuestion_whenConstrainsAreViolated_savesCurrentAnswers() {
        rule.startAtMainMenu()
            .copyForm("two-question-save-incomplete-required.xml")
            .startBlankForm("Two Question Save Incomplete Required")
            .answerQuestion("What is your name?", "Dez")
            .swipeToNextQuestion("[saveIncomplete] What is your age?", true)
            .pressBackAndDiscardChanges()

            .clickDrafts(1)
            .clickOnForm("Two Question Save Incomplete Required")
            .assertText("Dez", composeRule)
    }
}

package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.pages.FormEntryPage.QuestionAndAnswer
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

@RunWith(AndroidJUnit4::class)
class SaveIncompleteTest {

    var rule = CollectTestRule()

    @get:Rule
    var chain: RuleChain = chain()
        .around(rule)

    @Test
    fun viewingSaveIncompleteQuestion_savesCurrentAnswers() {
        rule.startAtMainMenu()
            .copyForm("two-question-save-incomplete.xml")
            .startBlankForm("Two Question Save Incomplete")
            .answerQuestion("What is your name?", "Dez")
            .swipeToNextQuestion("[saveIncomplete] What is your age?")
            .pressBackAndIgnoreChanges()

            .clickEditSavedForm(1)
            .clickOnForm("Two Question Save Incomplete")
            .assertText("Dez")
    }

    @Test
    fun viewingSaveIncompleteQuestion_whenConstrainsAreViolated_savesCurrentAnswers() {
        rule.startAtMainMenu()
            .copyForm("two-question-save-incomplete-required.xml")
            .startBlankForm("Two Question Save Incomplete Required")
            .answerQuestion("What is your name?", "Dez")
            .swipeToNextQuestion("[saveIncomplete] What is your age?", true)
            .pressBackAndIgnoreChanges()

            .clickEditSavedForm(1)
            .clickOnForm("Two Question Save Incomplete Required")
            .assertText("Dez")
    }

    @Test
    fun whenEditingAFinalizedForm_viewingSaveIncompleteQuestion_savesCurrentAnswers_andUnfinalizesForm() {
        rule.startAtMainMenu()
            .copyForm("two-question-save-incomplete-required.xml")
            .startBlankForm("Two Question Save Incomplete Required")
            .fillOutAndSave(
                QuestionAndAnswer("What is your name?", "Dez"),
                QuestionAndAnswer("[saveIncomplete] What is your age?", "56", true)
            )
            .assertNumberOfFinalizedForms(1)

            .clickEditSavedForm(1)
            .clickOnForm("Two Question Save Incomplete Required")
            .clickGoToStart()
            .answerQuestion("What is your name?", "Meg")
            .swipeToNextQuestion("[saveIncomplete] What is your age?", true)
            .pressBackAndIgnoreChanges()

            .assertNumberOfFinalizedForms(0)
            .clickEditSavedForm(1)
            .clickOnForm("Two Question Save Incomplete Required")
            .assertText("Meg")
            .assertText("56")
    }

    @Test
    fun whenEditingAFinalizedForm_viewingSaveIncompleteQuestion_whenConstrainsAreViolated_savesCurrentAnswers_andUnfinalizesForm() {
        rule.startAtMainMenu()
            .copyForm("two-question-save-incomplete-required.xml")
            .startBlankForm("Two Question Save Incomplete Required")
            .fillOutAndSave(
                QuestionAndAnswer("What is your name?", "Dez"),
                QuestionAndAnswer("[saveIncomplete] What is your age?", "56", true)
            )
            .assertNumberOfFinalizedForms(1)

            .clickEditSavedForm(1)
            .clickOnForm("Two Question Save Incomplete Required")
            .clickOnQuestion("[saveIncomplete] What is your age?", true)
            .longPressOnQuestion("[saveIncomplete] What is your age?", true)
            .removeResponse()
            .swipeToPreviousQuestion("What is your name?")
            .answerQuestion("What is your name?", "Bradley")
            .swipeToNextQuestion("[saveIncomplete] What is your age?")
            .pressBackAndIgnoreChanges()

            .assertNumberOfFinalizedForms(0)
            .clickEditSavedForm(1)
            .clickOnForm("Two Question Save Incomplete Required")
            .assertText("Bradley")
    }
}

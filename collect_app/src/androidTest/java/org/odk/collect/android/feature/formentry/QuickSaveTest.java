package org.odk.collect.android.feature.formentry;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.pages.FormEntryPage.QuestionAndAnswer;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

@RunWith(AndroidJUnit4.class)
public class QuickSaveTest {

    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void whenFillingForm_clickingSaveIcon_savesCurrentAnswers() {
        rule.startAtMainMenu()
                .copyForm("two-question.xml")
                .startBlankForm("Two Question")
                .fillOut(
                        new QuestionAndAnswer("What is your name?", "Reuben"),
                        new QuestionAndAnswer("What is your age?", "32")
                )
                .clickSave()
                .pressBackAndIgnoreChanges()

                .clickEditSavedForm(1)
                .clickOnForm("Two Question")
                .assertText("Reuben")
                .assertText("32");
    }

    @Test
    public void whenFillingForm_withViolatedConstraintsOnCurrentScreen_clickingSaveIcon_savesCurrentAnswers() {
        rule.startAtMainMenu()
                .copyForm("two-question-required.xml")
                .startBlankForm("Two Question Required")
                .answerQuestion("What is your name?", "Reuben")
                .swipeToNextQuestion("What is your age?", true)
                .clickSave()
                .pressBackAndIgnoreChanges()

                .clickEditSavedForm(1)
                .clickOnForm("Two Question Required")
                .assertText("Reuben");
    }

    @Test
    public void whenEditingANonFinalizedForm_withViolatedConstraintsOnCurrentScreen_clickingSaveIcon_savesCurrentAnswers() {
        rule.startAtMainMenu()
                .copyForm("two-question-required.xml")
                .startBlankForm("Two Question Required")
                .answerQuestion("What is your name?", "Reuben")
                .clickSave()
                .pressBackAndIgnoreChanges()

                .clickEditSavedForm(1)
                .clickOnForm("Two Question Required")
                .clickGoToStart()
                .answerQuestion("What is your name?", "Another Reuben")
                .swipeToNextQuestion("What is your age?", true)
                .clickSave()
                .pressBackAndIgnoreChanges()

                .clickEditSavedForm(1)
                .clickOnForm("Two Question Required")
                .assertText("Another Reuben");
    }

    @Test
    public void whenEditingAFinalizedForm_withViolatedConstraintsOnCurrentScreen_clickingSaveIcon_showsError() {
        rule.startAtMainMenu()
                .copyForm("two-question-required.xml")
                .startBlankForm("Two Question Required")
                .fillOutAndSave(
                        new QuestionAndAnswer("What is your name?", "Reuben"),
                        new QuestionAndAnswer("What is your age?", "32", true)
                )

                .clickEditSavedForm(1)
                .clickOnForm("Two Question Required")
                .clickGoToStart()
                .answerQuestion("What is your name?", "Another Reuben")
                .swipeToNextQuestion("What is your age?", true)
                .longPressOnQuestion("What is your age?", true)
                .removeResponse()
                .clickSave()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_error)

                .pressBackAndIgnoreChanges()

                .clickEditSavedForm(1)
                .clickOnForm("Two Question Required")
                .assertText("Reuben")
                .assertText("32");
    }

    @Test
    public void whenEditingAFinalizedForm_withViolatedConstraintsOnAnotherScreen_clickingSaveIcon_showsConstraintViolation() {
        rule.startAtMainMenu()
                .copyForm("two-question-required.xml")
                .startBlankForm("Two Question Required")
                .fillOutAndSave(
                        new QuestionAndAnswer("What is your name?", "Reuben"),
                        new QuestionAndAnswer("What is your age?", "32", true)
                )

                .clickEditSavedForm(1)
                .clickOnForm("Two Question Required")
                .clickGoToStart()
                .answerQuestion("What is your name?", "Another Reuben")
                .swipeToNextQuestion("What is your age?", true)
                .longPressOnQuestion("What is your age?", true)
                .removeResponse()
                .swipeToPreviousQuestion("What is your name?")
                .clickSave()
                .assertConstraintDisplayed("Sorry, this response is required!")
                .assertQuestion("What is your age?", true)
                .pressBackAndIgnoreChanges()

                .clickEditSavedForm(1)
                .clickOnForm("Two Question Required")
                .assertText("Reuben")
                .assertText("32");
    }
}

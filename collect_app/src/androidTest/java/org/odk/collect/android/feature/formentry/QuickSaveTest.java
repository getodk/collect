package org.odk.collect.android.feature.formentry;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.pages.FormEntryPage.QuestionAndAnswer;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.PageComposeRule;
import org.odk.collect.android.support.rules.TestRuleChain;

@RunWith(AndroidJUnit4.class)
public class QuickSaveTest {
    private final PageComposeRule pageComposeRule = new PageComposeRule();
    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain()
            .around(pageComposeRule)
            .around(pageComposeRule.getComposeRule())
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
                .pressBackAndDiscardChanges()

                .clickDrafts(1)
                .clickOnForm("Two Question")
                .assertAnswer("Reuben")
                .assertAnswer("32");
    }

    @Test
    public void whenFillingForm_withViolatedConstraintsOnCurrentScreen_clickingSaveIcon_savesCurrentAnswers() {
        rule.startAtMainMenu()
                .copyForm("two-question-required.xml")
                .startBlankForm("Two Question Required")
                .answerQuestion("What is your name?", "Reuben")
                .swipeToNextQuestion("What is your age?", true)
                .clickSave()
                .pressBackAndDiscardChanges()

                .clickDrafts(1)
                .clickOnForm("Two Question Required")
                .assertAnswer("Reuben");
    }

    @Test
    public void whenEditingANonFinalizedForm_withViolatedConstraintsOnCurrentScreen_clickingSaveIcon_savesCurrentAnswers() {
        rule.startAtMainMenu()
                .copyForm("two-question-required.xml")
                .startBlankForm("Two Question Required")
                .answerQuestion("What is your name?", "Reuben")
                .clickSave()
                .pressBackAndDiscardChanges()

                .clickDrafts(1)
                .clickOnForm("Two Question Required")
                .clickGoToStart()
                .answerQuestion("What is your name?", "Another Reuben")
                .swipeToNextQuestion("What is your age?", true)
                .clickSave()
                .pressBackAndDiscardChanges()

                .clickDrafts(1)
                .clickOnForm("Two Question Required")
                .assertAnswer("Another Reuben");
    }
}

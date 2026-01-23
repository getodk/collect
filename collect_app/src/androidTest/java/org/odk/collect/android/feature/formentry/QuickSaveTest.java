package org.odk.collect.android.feature.formentry;

import static androidx.compose.ui.test.junit4.AndroidComposeTestRule_androidKt.createEmptyComposeRule;

import androidx.compose.ui.test.junit4.ComposeTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.pages.FormEntryPage.QuestionAndAnswer;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

@RunWith(AndroidJUnit4.class)
public class QuickSaveTest {
    private final ComposeTestRule composeRule = createEmptyComposeRule();
    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain()
            .around(composeRule)
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
                .assertText("Reuben", composeRule)
                .assertText("32", composeRule);
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
                .assertText("Reuben", composeRule);
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
                .assertText("Another Reuben", composeRule);
    }
}

package org.odk.collect.android.feature.formentry;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.pages.FormEntryPage.QuestionAndAnswer;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SaveOrDiscardFormDialog;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.ManagedComposeRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.testshared.AssertionFramework;

@RunWith(AndroidJUnit4.class)
public class QuittingFormTest {
    private final ManagedComposeRule managedComposeRule = new ManagedComposeRule();

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(managedComposeRule)
            .around(managedComposeRule.getComposeRule())
            .around(rule);

    @Test
    public void whenFillingForm_pressingBack_andClickingSaveChanges_savesCurrentAnswers() {
        rule.startAtMainMenu()
                .copyForm("two-question.xml")
                .startBlankForm("Two Question")
                .fillOut(
                        new QuestionAndAnswer("What is your name?", "Reuben"),
                        new QuestionAndAnswer("What is your age?", "10")
                )
                .pressBack(new SaveOrDiscardFormDialog<>(new MainMenuPage()))
                .clickSaveChanges()

                .assertNumberOfFinalizedForms(0)
                .clickDrafts(1)
                .clickOnForm("Two Question")
                .assertText("Reuben", AssertionFramework.COMPOSE)
                .assertText("10", AssertionFramework.COMPOSE);
    }

    @Test
    public void whenFillingForm_pressingBack_andClickingIgnoreChanges_doesNotSaveForm() {
        rule.startAtMainMenu()
                .copyForm("two-question.xml")
                .startBlankForm("Two Question")
                .answerQuestion("What is your name?", "Reuben")
                .pressBack(new SaveOrDiscardFormDialog<>(new MainMenuPage()))
                .clickDiscardForm()

                .assertNumberOfEditableForms(0);
    }

    @Test
    public void whenFillingForm_saving_andPressingBack_andClickingIgnoreChanges_savesAnswersBeforeSave() {
        rule.startAtMainMenu()
                .copyForm("two-question.xml")
                .startBlankForm("Two Question")
                .answerQuestion("What is your name?", "Reuben")
                .clickSave()
                .swipeToNextQuestion("What is your age?")
                .answerQuestion("What is your age?", "10")
                .pressBackAndDiscardChanges()

                .clickDrafts(1)
                .clickOnForm("Two Question")
                .assertText("Reuben", AssertionFramework.COMPOSE)
                .assertTextDoesNotExist("10", AssertionFramework.COMPOSE);
    }

    @Test
    public void whenFillingForm_withViolatedConstraintsOnCurrentScreen_pressingBack_andClickingSaveChanges_savesCurrentAnswers() {
        rule.startAtMainMenu()
                .copyForm("two-question-required.xml")
                .startBlankForm("Two Question Required")
                .answerQuestion("What is your name?", "Reuben")
                .swipeToNextQuestion("What is your age?", true)
                .closeSoftKeyboard()
                .pressBack(new SaveOrDiscardFormDialog<>(new MainMenuPage()))
                .clickSaveChanges()

                .clickDrafts(1)
                .clickOnForm("Two Question Required")
                .assertText("Reuben", AssertionFramework.COMPOSE);
    }

    @Test
    public void whenEditingANonFinalizedForm_withViolatedConstraintsOnCurrentScreen_pressingBack_andClickingSaveChanges_savesCurrentAnswers() {
        rule.startAtMainMenu()
                .copyForm("two-question-required.xml")
                .startBlankForm("Two Question Required")
                .answerQuestion("What is your name?", "Reuben")
                .pressBack(new SaveOrDiscardFormDialog<>(new MainMenuPage()))
                .clickSaveChanges()

                .clickDrafts(1)
                .clickOnForm("Two Question Required")
                .clickGoToStart()
                .answerQuestion("What is your name?", "Another Reuben")
                .swipeToNextQuestion("What is your age?", true)
                .closeSoftKeyboard()
                .pressBack(new SaveOrDiscardFormDialog<>(new MainMenuPage()))
                .clickSaveChanges()

                .clickDrafts(1)
                .clickOnForm("Two Question Required")
                .assertText("Another Reuben", AssertionFramework.COMPOSE);
    }
}

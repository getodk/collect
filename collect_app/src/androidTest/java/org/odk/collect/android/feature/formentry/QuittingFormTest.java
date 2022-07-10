package org.odk.collect.android.feature.formentry;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

@RunWith(AndroidJUnit4.class)
public class QuittingFormTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void whenFillingForm_pressingBack_andClickingSaveChanges_savesCurrentAnswers() {
        rule.startAtMainMenu()
                .copyForm("two-question.xml")
                .startBlankForm("Two Question")
                .answerQuestion("What is your name?", "Reuben")
                .swipeToNextQuestion("What is your age?")
                .answerQuestion("What is your age?", "10")

                .pressBack(new SaveOrIgnoreDialog<>("Two Question", new MainMenuPage()))
                .clickSaveChanges()

                .clickEditSavedForm(1)
                .clickOnForm("Two Question")
                .assertText("Reuben")
                .assertText("10");
    }

    @Test
    public void whenFillingForm_pressingBack_andClickingIgnoreChanges_doesNotSaveForm() {
        rule.startAtMainMenu()
                .copyForm("two-question.xml")
                .startBlankForm("Two Question")
                .answerQuestion("What is your name?", "Reuben")

                .pressBack(new SaveOrIgnoreDialog<>("Two Question", new MainMenuPage()))
                .clickIgnoreChanges()

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

                .pressBack(new SaveOrIgnoreDialog<>("Two Question", new MainMenuPage()))
                .clickIgnoreChanges()

                .clickEditSavedForm(1)
                .clickOnForm("Two Question")
                .assertText("Reuben")
                .assertTextDoesNotExist("10");
    }

    @Test
    public void whenFillingForm_withViolatedConstraints_pressingBack_andClickingSaveChanges_savesCurrentAnswers() {
        rule.startAtMainMenu()
                .copyForm("two-question-required.xml")
                .startBlankForm("Two Question Required")
                .answerQuestion("What is your name?", "Reuben")

                .pressBack(new SaveOrIgnoreDialog<>("Two Question Required", new MainMenuPage()))
                .clickSaveChanges()

                .clickEditSavedForm(1)
                .clickOnForm("Two Question Required")
                .assertText("Reuben");
    }

    @Test
    public void whenEditingANonFinalizedForm_withViolatedConstraints_pressingBack_andClickingSaveChanges_savesCurrentAnswers() {
        rule.startAtMainMenu()
                .copyForm("two-question-required.xml")
                .startBlankForm("Two Question Required")
                .answerQuestion("What is your name?", "Reuben")

                .pressBack(new SaveOrIgnoreDialog<>("Two Question Required", new MainMenuPage()))
                .clickSaveChanges()

                .clickEditSavedForm(1)
                .clickOnForm("Two Question Required")
                .clickGoToStart()
                .answerQuestion("What is your name?", "Another Reuben")

                .pressBack(new SaveOrIgnoreDialog<>("Two Question Required", new MainMenuPage()))
                .clickSaveChanges()

                .clickEditSavedForm(1)
                .clickOnForm("Two Question Required")
                .assertText("Another Reuben");
    }

    @Test
    public void whenEditingAFinalizedForm_withViolatedConstraints_pressingBack_andClickingSaveChanges_showsError() {
        rule.startAtMainMenu()
                .copyForm("two-question-required.xml")
                .startBlankForm("Two Question Required")
                .answerQuestion("What is your name?", "Reuben")
                .swipeToNextQuestion("What is your age?", true)
                .answerQuestion("What is your age?", "32")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickEditSavedForm(1)
                .clickOnForm("Two Question Required")
                .clickGoToStart()
                .answerQuestion("What is your name?", "Another Reuben")
                .swipeToNextQuestion("What is your age?", true)
                .longPressOnQuestion("What is your age?", true)
                .removeResponse()

                .closeSoftKeyboard()
                .pressBack(new SaveOrIgnoreDialog<>("Two Question Required", new FormEntryPage("Two Question Required")))
                .clickSaveChanges()
                .checkIsToastWithMessageDisplayed(R.string.data_saved_error)

                .pressBack(new SaveOrIgnoreDialog<>("Two Question Required", new MainMenuPage()))
                .clickIgnoreChanges()

                .clickEditSavedForm(1)
                .clickOnForm("Two Question Required")
                .assertText("Reuben");
    }
}

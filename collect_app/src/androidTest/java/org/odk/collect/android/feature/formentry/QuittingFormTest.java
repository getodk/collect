package org.odk.collect.android.feature.formentry;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
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
}

package org.odk.collect.android.feature.formentry;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog;

@RunWith(AndroidJUnit4.class)
public class QuickSaveTest {

    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void whenFillingOutNewForm_clickingSaveIcon_andIgnoringChanges_savesLatestAnswer() {
        rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .inputText("123")
                .clickSave()
                .closeSoftKeyboard()
                .pressBack(new SaveOrIgnoreDialog<>("One Question", new MainMenuPage()))
                .clickIgnoreChanges()

                .clickEditSavedForm(1)
                .clickOnForm("One Question")
                .assertText("123");
    }

    @Test
    public void whenFillingOutNewForm_clickingSaveIcon_makingChangesAndSaveAndExiting_savesLatestAnswer() {
        rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .inputText("123")
                .clickSave()
                .inputText("456")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickEditSavedForm(1)
                .clickOnForm("One Question")
                .assertText("456");
    }

    @Test
    public void whenEditingForm_clickingSaveIcon_andIgnoringChanges_savesLatestAnswers() {
        rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .inputText("123")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickEditSavedForm(1)
                .clickOnForm("One Question")
                .clickGoToStart()
                .inputText("456")
                .clickSave()
                .closeSoftKeyboard()
                .pressBack(new SaveOrIgnoreDialog<>("One Question", new MainMenuPage()))
                .clickIgnoreChanges()

                .clickEditSavedForm(1)
                .clickOnForm("One Question")
                .assertText("456");
    }

    @Test
    public void whenEditingForm_clickingSaveIcon_andMakingChanges_andIgnoringChanges_savesFirstEdits() {
        rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .inputText("123")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickEditSavedForm(1)
                .clickOnForm("One Question")
                .clickGoToStart()
                .inputText("456")
                .clickSave()
                .inputText("789")
                .closeSoftKeyboard()
                .pressBack(new SaveOrIgnoreDialog<>("One Question", new MainMenuPage()))
                .clickIgnoreChanges()

                .clickEditSavedForm(1)
                .clickOnForm("One Question")
                .assertText("456");
    }
}

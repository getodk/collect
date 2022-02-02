package org.odk.collect.android.feature.formentry;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

@RunWith(AndroidJUnit4.class)
public class SaveAsTest {

    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void fillingFormNameAtEndOfForm_savesInstanceWithName() {
        rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .swipeToEndScreen()
                .fillInFormName("My Favourite Form")
                .clickSaveAndExit()
                .clickSendFinalizedForm(1)
                .assertText("My Favourite Form");
    }

    @Test
    public void editingFormWithSavedName_prefillsName() {
        rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .swipeToEndScreen()
                .fillInFormName("My Favourite Form")
                .clickSaveAndExit()

                .clickEditSavedForm(1)
                .clickOnForm("One Question", "My Favourite Form")
                .clickJumpEndButton()
                .assertText("My Favourite Form")
                .clickSaveAndExit()

                .clickSendFinalizedForm(1)
                .assertText("My Favourite Form");
    }
}

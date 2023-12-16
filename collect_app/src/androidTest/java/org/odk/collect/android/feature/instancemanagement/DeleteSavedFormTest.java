package org.odk.collect.android.feature.instancemanagement;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.pages.DeleteSavedFormPage;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.android.support.pages.MainMenuPage;

@RunWith(AndroidJUnit4.class)
public class DeleteSavedFormTest {

    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void deletingAForm_removesFormFromFinalizedForms() {
        rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .answerQuestion("what is your age", "30")
                .swipeToEndScreen()
                .clickFinalize()

                .clickDeleteSavedForm()
                .clickForm("One Question")
                .clickDeleteSelected(1)
                .clickDeleteForms()
                .assertTextDoesNotExist("One Question")
                .pressBack(new MainMenuPage())
                .assertNumberOfFinalizedForms(0);
    }

    @Test
    public void accessingSortMenuInDeleteSavedInstancesShouldNotCrashTheAppAfterRotatingTheDevice() {
        rule.startAtMainMenu()
                .copyForm("one-question.xml")
                .startBlankForm("One Question")
                .answerQuestion("what is your age", "30")
                .swipeToEndScreen()
                .clickFinalize()
                .clickDeleteSavedForm()
                .rotateToLandscape(new DeleteSavedFormPage())
                .clickOnId(R.id.menu_sort)
                .assertText(org.odk.collect.strings.R.string.sort_by);
    }
}

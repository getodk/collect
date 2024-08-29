package org.odk.collect.android.regression;

import static java.util.Collections.singletonList;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

//Issue NODK-244
@RunWith(AndroidJUnit4.class)
public class FillBlankFormTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void searchExpression_ShouldDisplayWhenItContainsOtherAppearanceName() {
        //TestCase26
        // This form doesn't define an instanceID and also doesn't request encryption so this case
        // would catch regressions for https://github.com/getodk/collect/issues/3340
        rule.startAtMainMenu()
                .copyForm("CSVerrorForm.xml", singletonList("TrapLists.csv"))
                .startBlankForm("CSV error Form")
                .clickOnText("Greg Pommen")
                .swipeToNextQuestion("* Select trap program:")
                .clickOnText("Mountain pine beetle")
                .swipeToNextQuestion("Pick Trap Name:")
                .assertText("2018-COE-MPB-001 @ Wellington")
                .swipeToPreviousQuestion("* Select trap program:")
                .clickOnText("Invasive alien species")
                .swipeToNextQuestion("Pick Trap Name:")
                .assertText("2018-COE-IAS-e-001 @ Coronation")
                .swipeToPreviousQuestion("* Select trap program:")
                .clickOnText("Longhorn beetles")
                .swipeToNextQuestion("Pick Trap Name:")
                .assertText("2018-COE-LGH-M-001 @ Acheson")
                .clickOnText("2018-COE-LGH-L-004 @ Acheson")
                .swipeToNextQuestion("* Were there specimens in the trap:")
                .clickOnText("No")
                .swipeToNextQuestion("Any other notes?")
                .swipeToEndScreen()
                .clickFinalize()
                .checkIsSnackbarWithMessageDisplayed(org.odk.collect.strings.R.string.form_saved);
    }
}

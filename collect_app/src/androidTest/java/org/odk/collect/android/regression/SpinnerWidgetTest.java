package org.odk.collect.android.regression;

import android.Manifest;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.espressoutils.MainMenu;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

import static androidx.test.espresso.Espresso.pressBack;

// Issue number NODK-219
@RunWith(AndroidJUnit4.class)
public class SpinnerWidgetTest extends BaseRegressionTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("selectOneMinimal.xml"));

    @Test
    public void spinnerList_ShouldDisplay() {
        MainMenu.startBlankForm("selectOneMinimal");
        FormEntry.clickOnString(R.string.select_one);
        FormEntry.clickOnAreaWithIndex("CheckedTextView", 2);
        FormEntry.clickOnText("c");
        FormEntry.checkIsTextDisplayed("c");
        FormEntry.checkIfTextDoesNotExist("a");
        FormEntry.checkIfTextDoesNotExist("b");
        pressBack();
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();
    }
}

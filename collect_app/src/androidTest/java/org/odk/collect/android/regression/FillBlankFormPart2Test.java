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

//Issue NODK-244
@RunWith(AndroidJUnit4.class)
public class FillBlankFormPart2Test extends BaseRegressionTest {
    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("g6Error.xml", "regression"))
            .around(new CopyFormRule("g6Error2.xml", "regression"))
            .around(new CopyFormRule("emptyGroupFieldList.xml", "regression"))
            .around(new CopyFormRule("emptyGroupFieldList2.xml", "regression"))
            .around(new CopyFormRule("metadata2.xml", "regression"));

    @Test
    public void app_ShouldNotCrash() {
        //TestCase1
        MainMenu.startBlankForm("g6Error");
        FormEntry.checkIsStringDisplayed(R.string.error_occured);
        FormEntry.clickOk();
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();
        FormEntry.checkIsToastWithMessageDisplayes("Form successfully saved!", main);
        //TestCase2
        MainMenu.startBlankForm("g6Error2");
        FormEntry.putText("bla");
        FormEntry.swipeToNextQuestion();
        FormEntry.checkIsStringDisplayed(R.string.error_occured);
        FormEntry.clickOk();
        FormEntry.swipeToNextQuestion();
        FormEntry.putText("ble");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();
        FormEntry.checkIsToastWithMessageDisplayes("Form successfully saved!", main);
        //TestCase3
        MainMenu.startBlankForm("emptyGroupFieldList");
        FormEntry.clickSaveAndExit();
        FormEntry.checkIsToastWithMessageDisplayes("Form successfully saved!", main);
        //TestCase4
        MainMenu.startBlankForm("emptyGroupFieldList2");
        FormEntry.putText("nana");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();
        FormEntry.checkIsToastWithMessageDisplayes("Form successfully saved!", main);
    }

    @Test
    public void user_ShouldBeAbleToFillTheForm() {
        MainMenu.startBlankForm("metadata2");
        FormEntry.clickSaveAndExit();
        FormEntry.checkIsToastWithMessageDisplayes("Form successfully saved!", main);
    }

}

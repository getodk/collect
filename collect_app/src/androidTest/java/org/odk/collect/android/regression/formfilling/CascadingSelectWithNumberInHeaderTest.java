package org.odk.collect.android.regression.formfilling;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.espressoutils.MainMenu;
import org.odk.collect.android.regression.BaseRegressionTest;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

import java.util.Collections;

// Issue number NODK-207
@RunWith(AndroidJUnit4.class)
public class CascadingSelectWithNumberInHeaderTest extends BaseRegressionTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("numberInCSV.xml", Collections.singletonList("itemSets.csv")));

    @Test
    public void fillForm_ShouldFillFormWithNumberInCsvHeader() {

        MainMenu.startBlankForm("numberInCSV");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnText("Venda de animais");
        FormEntry.checkIsTextDisplayed("1a");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnText("Vendas agrícolas");
        FormEntry.checkIsTextDisplayed("2a");
        FormEntry.swipeToNextQuestion();
        FormEntry.clickOnText("Pensão");
        FormEntry.checkIsTextDisplayed("3a");
        FormEntry.swipeToNextQuestion();
        FormEntry.swipeToNextQuestion();
        FormEntry.clickSaveAndExit();
    }
}

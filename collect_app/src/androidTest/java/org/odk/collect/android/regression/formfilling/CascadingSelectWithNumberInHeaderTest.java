package org.odk.collect.android.regression.formfilling;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.espressoutils.pages.MainMenuPage;
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

        new MainMenuPage(main)
                .startBlankForm("numberInCSV")
                .swipeToNextQuestion()
                .clickOnText("Venda de animais")
                .checkIsTextDisplayed("1a")
                .swipeToNextQuestion()
                .clickOnText("Vendas agrícolas")
                .checkIsTextDisplayed("2a")
                .swipeToNextQuestion()
                .clickOnText("Pensão")
                .checkIsTextDisplayed("3a")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickSaveAndExit();
    }
}

package org.odk.collect.android.regression;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

import java.util.Collections;

// Issue number NODK-207
@RunWith(AndroidJUnit4.class)
public class CascadingSelectWithNumberInHeaderTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void fillForm_ShouldFillFormWithNumberInCsvHeader() {

        rule.startAtMainMenu()
                .copyForm("numberInCSV.xml", Collections.singletonList("itemSets.csv"))
                .startBlankForm("numberInCSV")
                .swipeToNextQuestion("1a")
                .clickOnText("Venda de animais")
                .assertText("1a")
                .swipeToNextQuestion("2a")
                .clickOnText("Vendas agrícolas")
                .assertText("2a")
                .swipeToNextQuestion("3a")
                .clickOnText("Pensão")
                .assertText("3a")
                .swipeToNextQuestion("Thank you for taking the time to complete this form!")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }
}

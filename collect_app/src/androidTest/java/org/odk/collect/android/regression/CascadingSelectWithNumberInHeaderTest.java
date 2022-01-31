package org.odk.collect.android.regression;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.TestRuleChain;

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
                .swipeToNextQuestion()
                .clickOnText("Venda de animais")
                .assertText("1a")
                .swipeToNextQuestion()
                .clickOnText("Vendas agrícolas")
                .assertText("2a")
                .swipeToNextQuestion()
                .clickOnText("Pensão")
                .assertText("3a")
                .swipeToNextQuestion()
                .swipeToEndScreen()
                .clickSaveAndExit();
    }
}

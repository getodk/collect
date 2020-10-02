package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.MainMenuPage;

import java.util.Collections;

// Issue number NODK-377
@RunWith(AndroidJUnit4.class)
public class ExternalSecondaryInstancesTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("internal_select_10.xml"))
            .around(new CopyFormRule("external_select_10.xml", Collections.singletonList("external_data_10.xml")))
            .around(rule);

    @Test
    public void external_ShouldFillTheForm() {

        //TestCase1
        new MainMenuPage(rule)
                .startBlankForm("external select 10")
                .clickOnText("b")
                .swipeToNextQuestion()
                .clickOnText("ba")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void internal_ShouldFillTheForm() {

        //TestCase2
        new MainMenuPage(rule)
                .startBlankForm("internal select 10")
                .clickOnText("c")
                .swipeToNextQuestion()
                .clickOnText("ca")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }
}
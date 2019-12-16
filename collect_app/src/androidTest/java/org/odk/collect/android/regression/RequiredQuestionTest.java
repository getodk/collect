package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

// Issue number NODK-249
@RunWith(AndroidJUnit4.class)
public class RequiredQuestionTest extends BaseRegressionTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("requiredJR275.xml"));

    @Test
    public void requiredQuestions_ShouldDisplayAsterisk() {

        //TestCase1
        new MainMenuPage(rule)
                .startBlankForm("required")
                .checkIsTextDisplayed("* Foo")
                .closeSoftKeyboard()
                .pressBack(new SaveOrIgnoreDialog<>("required", new MainMenuPage(rule), rule))
                .clickIgnoreChanges();
    }

    @Test
    public void requiredQuestions_ShouldDisplayCustomMessage() {

        //TestCase2
        new MainMenuPage(rule)
                .startBlankForm("required")
                .swipeToNextQuestion()
                .checkIsToastWithMessageDisplayed("Custom required message")
                .closeSoftKeyboard()
                .pressBack(new SaveOrIgnoreDialog<>("required", new MainMenuPage(rule), rule))
                .clickIgnoreChanges();
    }
}
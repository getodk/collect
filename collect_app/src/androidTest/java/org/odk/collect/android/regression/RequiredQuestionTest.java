package org.odk.collect.android.regression;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog;

// Issue number NODK-249
@RunWith(AndroidJUnit4.class)
public class RequiredQuestionTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void requiredQuestions_ShouldDisplayAsterisk_andCustomMessageIfSkipped() {
        rule.startAtMainMenu()
                .copyForm("requiredJR275.xml")
                .startBlankForm("required")
                .assertText("* Foo") //TestCase1
                .swipeToNextQuestionWithConstraintViolation("Custom required message");  //TestCase2
    }
}

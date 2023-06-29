package org.odk.collect.android.feature.formentry;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

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

    @Test
    public void pressingValidateInOptionsMenuOnSameScreen_shouldUseLatestAnswers() {
        rule.startAtMainMenu()
                .copyForm("requiredJR275.xml")
                .startBlankForm("required")
                .answerQuestion("Foo", true, "blah")
                .clickOptionsIcon()
                .clickOnString(R.string.validate)
                .assertText(R.string.success_form_validation)
                .assertConstraintNotDisplayed("Custom required message");
    }

    @Test
    public void pressingValidateInOptionsMenuOnDifferentScreen_shouldReturnToQuestionAndShowMessage() {
        rule.startAtMainMenu()
                .copyForm("requiredJR275.xml")
                .startBlankForm("required")
                .clickGoToArrow()
                .clickGoToEnd()
                .clickOptionsIcon()
                .clickOnString(R.string.validate)
                .assertConstraintDisplayed("Custom required message")
                .assertQuestion("Foo", true);
    }

    @Test
    public void pressingValidateInOptionsMenuOnDifferentScreen_shouldReturnToQuestionAndShowMessage_whenTheQuestionIsInFieldList() {
        rule.startAtMainMenu()
                .copyForm("requiredQuestionInFieldList.xml")
                .startBlankForm("requiredQuestionInFieldList")
                .clickGoToArrow()
                .clickGoToEnd()
                .clickOptionsIcon()
                .clickOnString(R.string.validate)
                .assertConstraintDisplayed("Custom required message")
                .assertQuestion("Foo", true);
    }
}

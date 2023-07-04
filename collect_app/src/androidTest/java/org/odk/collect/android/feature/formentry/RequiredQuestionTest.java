package org.odk.collect.android.feature.formentry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.apache.commons.csv.CSVRecord;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.StorageUtils;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

import java.io.IOException;
import java.util.List;

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

    @Test
    public void ifRequiredQuestionIsEmpty_shouldNotBeSavedToAuditLogWhenMovingForward() throws IOException {
        rule.startAtMainMenu()
                .copyForm("requiredJR275.xml")
                .startBlankForm("required")
                .swipeToNextQuestionWithConstraintViolation("Custom required message");

        List<CSVRecord> auditLog = StorageUtils.getAuditLogForFirstInstance();
        assertThat(auditLog.size(), equalTo(1));
        assertThat(auditLog.get(0).get(0), equalTo("form start"));
    }

    @Test
    public void ifRequiredQuestionIsEmpty_shouldNotBeSavedToAuditLogWhenFormValidated() throws IOException {
        rule.startAtMainMenu()
                .copyForm("requiredJR275.xml")
                .startBlankForm("required")
                .clickOptionsIcon()
                .clickOnString(R.string.validate);

        List<CSVRecord> auditLog = StorageUtils.getAuditLogForFirstInstance();
        assertThat(auditLog.size(), equalTo(1));
        assertThat(auditLog.get(0).get(0), equalTo("form start"));
    }

    @Test
    public void ifRequiredQuestionIsInFieldListAndNotFirst_shouldBeValidatedProperly() {
        rule.startAtMainMenu()
                .copyForm("requiredQuestionInFieldList.xml")
                .startBlankForm("requiredQuestionInFieldList")
                .answerQuestion(0, "Foo")
                .swipeToNextQuestionWithConstraintViolation("Custom required message2")
                .clickOptionsIcon()
                .clickOnString(R.string.validate)
                .assertConstraintDisplayed("Custom required message2")
                .clickGoToArrow()
                .clickGoToEnd()
                .clickSaveAndExitWithError("Custom required message2");
    }
}

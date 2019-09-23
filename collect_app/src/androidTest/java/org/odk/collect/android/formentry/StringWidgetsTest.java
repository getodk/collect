package org.odk.collect.android.formentry;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.espressoutils.pages.FormEntryPage;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.test.FormLoadingUtils;

public class StringWidgetsTest {
    private static final String TEST_FORM = "string_widgets.xml";

    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor(TEST_FORM);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule(TEST_FORM, null));

    @Test
    public void stringWidget_ShouldAllowUsingDifferentChars() {
        new FormEntryPage(TEST_FORM, activityTestRule)
                .putText("qwertyuiop !@#$% 1000000")
                .swipeToNextQuestion()
                .swipeToPreviousQuestion()
                .checkIsTextDisplayed("qwertyuiop !@#$% 1000000");
    }

    @Test
    public void integerWidget_ShouldCutNumbersLongerThan9() {
        new FormEntryPage(TEST_FORM, activityTestRule)
                .swipeToNextQuestion()
                .putText("10000000000000")
                .swipeToNextQuestion()
                .swipeToPreviousQuestion()
                .checkIsTextDisplayed("100000000");
    }

    @Test
    //strange behavior noticed here - it keeps only 8 numbers instead of 9
    public void integerWidgetThousandsSep_ShouldCutNumbersLongerThan9AndAddSeparators() {
        new FormEntryPage(TEST_FORM, activityTestRule)
                .swipeToNextQuestion(2)
                .putText("10000000000000")
                .swipeToNextQuestion()
                .swipeToPreviousQuestion()
                .checkIsTextDisplayed("10,000,000");
    }

    @Test
    public void decimalWidget_ShouldCutNumbersLongerThan15() {
        new FormEntryPage(TEST_FORM, activityTestRule)
                .swipeToNextQuestion(3)
                .putText("1000000000000.555")
                .swipeToNextQuestion()
                .swipeToPreviousQuestion()
                .checkIsTextDisplayed("1000000000000.5");
    }

    @Test
    public void decimalWidgetThousandsSep_ShouldCutNumbersLongerThan15AndAddSeparators() {
        new FormEntryPage(TEST_FORM, activityTestRule)
                .swipeToNextQuestion(4)
                .putText("1000000000000.555")
                .swipeToNextQuestion()
                .swipeToPreviousQuestion()
                .checkIsTextDisplayed("1,000,000,000,000.5");
    }

    @Test
    public void stringNumberWidget_ShouldAllowEnteringLongNumbers() {
        new FormEntryPage(TEST_FORM, activityTestRule)
                .swipeToNextQuestion(5)
                .putText("100000000000000000000000")
                .swipeToNextQuestion()
                .swipeToPreviousQuestion()
                .checkIsTextDisplayed("100000000000000000000000");
    }

    @Test
    public void stringNumberWidgetThousandsSep_ShouldAllowEnteringLongNumbersAndAddSeparators() {
        new FormEntryPage(TEST_FORM, activityTestRule)
                .swipeToNextQuestion(6)
                .putText("100000000000000000000000")
                .swipeToNextQuestion()
                .swipeToPreviousQuestion()
                .checkIsTextDisplayed("100,000,000,000,000,000,000,000");
    }

    @Test
    public void exStringWidget_ShouldAllowUsingDifferentChars() {
        new FormEntryPage(TEST_FORM, activityTestRule)
                .swipeToNextQuestion(7)
                .clickOnLaunchButton()
                .putText("qwertyuiop !@#$% 1000000")
                .swipeToNextQuestion()
                .swipeToPreviousQuestion()
                .checkIsTextDisplayed("qwertyuiop !@#$% 1000000");
    }

    @Test
    public void exIntegerWidget_ShouldCutNumbersLongerThan9() {
        new FormEntryPage(TEST_FORM, activityTestRule)
                .swipeToNextQuestion(8)
                .clickOnLaunchButton()
                .putText("10000000000000")
                .swipeToNextQuestion()
                .swipeToPreviousQuestion()
                .checkIsTextDisplayed("100000000");
    }

    @Test
    //strange behavior noticed here - it keeps only 8 numbers instead of 9
    public void exIntegerWidgetThousandsSep_ShouldCutNumbersLongerThan9AndAddSeparators() {
        new FormEntryPage(TEST_FORM, activityTestRule)
                .swipeToNextQuestion(9)
                .clickOnLaunchButton()
                .putText("10000000000000")
                .swipeToNextQuestion()
                .swipeToPreviousQuestion()
                .checkIsTextDisplayed("10,000,000");
    }

    @Test
    public void exDecimalWidget_ShouldCutNumbersLongerThan15() {
        new FormEntryPage(TEST_FORM, activityTestRule)
                .swipeToNextQuestion(10)
                .clickOnLaunchButton()
                .putText("1000000000000.555")
                .swipeToNextQuestion()
                .swipeToPreviousQuestion()
                .checkIsTextDisplayed("1000000000000.5");
    }

    @Test
    public void exDecimalWidgetThousandsSep_ShouldCutNumbersLongerThan15AndAddSeparators() {
        new FormEntryPage(TEST_FORM, activityTestRule)
                .swipeToNextQuestion(11)
                .clickOnLaunchButton()
                .putText("1000000000000.555")
                .swipeToNextQuestion()
                .swipeToPreviousQuestion()
                .checkIsTextDisplayed("1,000,000,000,000.5");
    }
}

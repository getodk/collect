package org.odk.collect.android.formentry;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.espressoutils.FormEntry;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.test.FormLoadingUtils;

@RunWith(AndroidJUnit4.class)
public class SelectMinimalTest {

    @Rule
    public IntentsTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor("select_minimal.xml");

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("select_minimal.xml"));

    @Test
    public void longLabelsShouldBeDisplayed() {
        FormEntry.clickOnText("Select One Answer");
        FormEntry.checkIsTextDisplayed("Integer a eleifend libero, sit amet tincidunt lacus. Donec orci tellus, facilisis et ultricies vel");
        FormEntry.checkIsTextDisplayed("Nam varius, lectus non consectetur tincidunt, augue augue dapibus dolor, nec convallis ligula erat eget");
        FormEntry.clickOnText("Integer a eleifend libero, sit amet tincidunt lacus. Donec orci tellus, facilisis et ultricies vel");
        FormEntry.checkIsTextDisplayed("Integer a eleifend libero, sit amet tincidunt lacus. Donec orci tellus, facilisis et ultricies vel");
        FormEntry.swipeToNextQuestion();
        FormEntry.showSpinnerMultipleDialog();
        FormEntry.checkIsTextDisplayed("Integer a eleifend libero, sit amet tincidunt lacus. Donec orci tellus, facilisis et ultricies vel");
        FormEntry.checkIsTextDisplayed("Nam varius, lectus non consectetur tincidunt, augue augue dapibus dolor, nec convallis ligula erat eget");
        FormEntry.clickOnText("Integer a eleifend libero, sit amet tincidunt lacus. Donec orci tellus, facilisis et ultricies vel");
        FormEntry.clickOk();
        FormEntry.checkIsTextDisplayed("Selected: Integer a eleifend libero, sit amet tincidunt lacus. Donec orci tellus, facilisis et ultricies vel");
    }
}

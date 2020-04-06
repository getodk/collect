package org.odk.collect.android.feature.formentry;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.FormLoadingUtils;

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
            .around(new CopyFormRule("select_minimal.xml", true));

    @Test
    public void longLabelsShouldBeDisplayed() {
        new FormEntryPage("Select minimal", activityTestRule)
                .clickOnText("Select One Answer")
        .assertText("Integer a eleifend libero, sit amet tincidunt lacus. Donec orci tellus, facilisis et ultricies vel")
        .assertText("Nam varius, lectus non consectetur tincidunt, augue augue dapibus dolor, nec convallis ligula erat eget")
        .clickOnText("Integer a eleifend libero, sit amet tincidunt lacus. Donec orci tellus, facilisis et ultricies vel")
        .assertText("Integer a eleifend libero, sit amet tincidunt lacus. Donec orci tellus, facilisis et ultricies vel")
        .swipeToNextQuestion()
        .showSpinnerMultipleDialog()
        .assertText("Integer a eleifend libero, sit amet tincidunt lacus. Donec orci tellus, facilisis et ultricies vel")
        .assertText("Nam varius, lectus non consectetur tincidunt, augue augue dapibus dolor, nec convallis ligula erat eget")
        .clickOnText("Integer a eleifend libero, sit amet tincidunt lacus. Donec orci tellus, facilisis et ultricies vel")
        .clickOKOnDialog()
        .assertText("Selected: Integer a eleifend libero, sit amet tincidunt lacus. Donec orci tellus, facilisis et ultricies vel");
    }
}

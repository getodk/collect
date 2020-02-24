package org.odk.collect.android.formentry;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.MainMenuPage;

@RunWith(AndroidJUnit4.class)
public class AddRepeatTest {

    private static final String FORM = "one-question-repeat.xml";

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE
            ))
            .around(new ResetStateRule())
            .around(new CopyFormRule(FORM));

    @Rule
    public ActivityTestRule<MainMenuActivity> rule = new ActivityTestRule<>(MainMenuActivity.class);

    @Test
    public void whenInARepeat_clickingPlus_andClickingAdd_addsAnotherRepeat() {
        new MainMenuPage(rule)
                .startBlankForm("One Question Repeat")
                .assertText("Person > 1")
                .clickPlus("Person")
                .clickOnAdd(new FormEntryPage("One Question Repeat", rule))
                .assertText("Person > 2");
    }

    @Test
    public void whenInARepeat_clickingPlus_andClickingDoNotAdd_returns() {
        new MainMenuPage(rule)
                .startBlankForm("One Question Repeat")
                .assertText("Person > 1")
                .clickPlus("Person")
                .clickOnDoNotAdd(new FormEntryPage("One Question Repeat", rule))
                .assertText("Person > 1");
    }
}

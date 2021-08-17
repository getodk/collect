package org.odk.collect.android.feature.formentry;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.MainMenuPage;

@RunWith(AndroidJUnit4.class)
public class NestedRepeatTest {

    private final CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
            .around(new ResetStateRule())
            .around(new CopyFormRule("NestedRepeats.xml"))
            .around(rule);

    @Test
    public void nestedRepeatsCreatedWithOuterRepeat() {
        new MainMenuPage()
                .startBlankForm("NestedRepeats")
                .assertText("Person > 1")
                .clickPlus("Person")
                .clickOnAdd(new FormEntryPage(""))
                .assertText("Person > 2")
                .swipeToNextQuestion("name?")//Already there?
                .assertText("Person > 2 > Child > 1")
                .swipeToNextQuestion("colour?")//Already there?
                .assertText("Person > 2 > Child > 1 > Pet > 1");

    }

}

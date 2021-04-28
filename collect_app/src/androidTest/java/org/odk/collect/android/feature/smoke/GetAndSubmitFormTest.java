package org.odk.collect.android.feature.smoke;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.pages.MainMenuPage;

@RunWith(AndroidJUnit4.class)
public class GetAndSubmitFormTest {

    private final  CollectTestRule rule = new CollectTestRule();
    private final TestDependencies testDependencies = new TestDependencies();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.GET_ACCOUNTS
            ))
            .around(new ResetStateRule(testDependencies))
            .around(rule);

    @Test
    public void canGetBlankForm_fillItIn_andSubmit() {
        testDependencies.server.addForm("One Question", "one-question", "1", "one-question.xml");

        rule.mainMenu()
                .setServer(testDependencies.server.getURL())
                .clickGetBlankForm()
                .clickGetSelected()
                .assertText("One Question (Version:: 1 ID: one-question) - Success")
                .clickOK(new MainMenuPage())

                .startBlankForm("One Question")
                .swipeToEndScreen()
                .clickSaveAndExit()

                .clickSendFinalizedForm(1)
                .clickOnForm("One Question")
                .clickSendSelected()
                .assertText("One Question - Success");
    }
}

package org.odk.collect.android.feature.instancemanagement;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.MainMenuPage;

@RunWith(AndroidJUnit4.class)
public class SendFinalizedFormTest {

    private final TestDependencies testDependencies = new TestDependencies();

    public IntentsTestRule<MainMenuActivity> rule = new IntentsTestRule<>(MainMenuActivity.class);

    @Rule
    public RuleChain chain = TestRuleChain.chain(testDependencies)
            .around(GrantPermissionRule.grant(Manifest.permission.GET_ACCOUNTS))
            .around(rule);

    @Test
    public void whenGoogleUsedAsServer_sendsSubmissionToSheet() {
        MainMenuPage page = new MainMenuPage(rule).assertOnPage()
                .copyForm("one-question-google.xml")
                .startBlankForm("One Question Google")
                .answerQuestion("what is your age", "47")
                .swipeToEndScreen()
                .clickSaveAndExit();

        testDependencies.googleAccountPicker.setDeviceAccount("dani@davey.com");
        testDependencies.googleApi.setAccount("dani@davey.com");

        page.setGoogleAccount("dani@davey.com")
                .clickSendFinalizedForm(1)
                .clickOnForm("One Question Google")
                .clickSendSelected()
                .assertText("One Question Google - Success");
    }
}

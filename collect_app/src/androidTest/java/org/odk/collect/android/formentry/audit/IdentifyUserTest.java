package org.odk.collect.android.formentry.audit;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.espressoutils.pages.MainMenuPage;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

@RunWith(AndroidJUnit4.class)
public class IdentifyUserTest {

    private static final String IDENTIFY_USER_AUDIT_FORM = "identify-user-audit.xml";

    @Rule
    public ActivityTestRule<MainMenuActivity> rule = new ActivityTestRule<>(MainMenuActivity.class);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
            .around(new ResetStateRule())
            .around(new CopyFormRule(IDENTIFY_USER_AUDIT_FORM));

    @Test
    public void openingForm_andThenEnteringIdentity_proceedsToForm() {
        new MainMenuPage(rule)
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .enterIdentity("Lucius")
                .clickKeyboardEnter()
                .swipeToNextQuestion()
                .clickSaveAndExit();
    }
}

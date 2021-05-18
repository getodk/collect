package org.odk.collect.android.feature.formentry.audit;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.IdentifyUserPromptPage;
import org.odk.collect.android.support.pages.MainMenuPage;

@RunWith(AndroidJUnit4.class)
public class IdentifyUserTest {

    private static final String IDENTIFY_USER_AUDIT_FORM = "identify-user-audit.xml";
    private static final String IDENTIFY_USER_AUDIT_FALSE_FORM = "identify-user-audit-false.xml";

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(new CopyFormRule(IDENTIFY_USER_AUDIT_FORM))
            .around(new CopyFormRule(IDENTIFY_USER_AUDIT_FALSE_FORM))
            .around(rule);

    @Test
    public void openingForm_andThenEnteringIdentity_proceedsToForm() {
        new MainMenuPage()
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .enterIdentity("Lucius")
                .clickKeyboardEnter()
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void openingSavedForm_promptsForIdentity() {
        new MainMenuPage()
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .enterIdentity("Lucius")
                .clickKeyboardEnter()
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnFormWithIdentityPrompt("Identify User");
    }

    @Test
    public void openingForm_andEnteringBlankIdentity_remainsOnIdentityPrompt() {
        new MainMenuPage()
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .enterIdentity("  ")
                .clickKeyboardEnterWithValidationError();
    }

    @Test
    public void openingForm_andPressingBack_returnsToMainMenu() {
        new MainMenuPage()
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .closeSoftKeyboard()
                .pressBack(new MainMenuPage());
    }

    @Test
    public void openingForm_andRotating_remainsOnIdentityPrompt() {
        new MainMenuPage()
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .enterIdentity("Blah")
                .rotateToLandscape(new IdentifyUserPromptPage("Identify User"))
                .assertText("Blah");
    }

    @Test
    public void openingForm_andPressingCloseCross_returnsToMainMenu() {
        new MainMenuPage()
                .clickFillBlankForm()
                .clickOnFormWithIdentityPrompt("Identify User")
                .pressClose();
    }

    @Test
    public void openFormWithIdentifyUserFalse_proceedsToForm() {
        new MainMenuPage()
                .clickFillBlankForm()
                .clickOnForm("Identify User False")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }
}

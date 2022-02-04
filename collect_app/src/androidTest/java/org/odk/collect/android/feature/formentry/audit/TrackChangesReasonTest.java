package org.odk.collect.android.feature.formentry.audit;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.ScreenshotOnFailureTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.android.support.pages.ChangesReasonPromptPage;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog;

@RunWith(AndroidJUnit4.class)
public class TrackChangesReasonTest {

    private static final String TRACK_CHANGES_REASON_ON_EDIT_FORM = "track-changes-reason-on-edit.xml";

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Rule
    public TestRule screenshotFailRule = new ScreenshotOnFailureTestRule();

    @Test
    public void openingAFormToEdit_andChangingAValue_andClickingSaveAndExit_andEnteringReason_andClickingSave_returnsToMainMenu() {
        rule.startAtMainMenu()
                .copyForm(TRACK_CHANGES_REASON_ON_EDIT_FORM)
                .startBlankForm("Track Changes Reason")
                .inputText("Nothing much...")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnForm("Track Changes Reason")
                .clickGoToStart()
                .inputText("Nothing much!")
                .swipeToEndScreen()
                .clickSaveAndExitWithChangesReasonPrompt()
                .enterReason("Needed to be more exciting and less mysterious")
                .clickSave();
    }

    @Test
    public void openingAFormToEdit_andChangingAValue_andClickingSaveAndExit_andPressingBack_returnsToForm() {
        rule.startAtMainMenu()
                .copyForm(TRACK_CHANGES_REASON_ON_EDIT_FORM)
                .startBlankForm("Track Changes Reason")
                .inputText("Nothing much...")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnForm("Track Changes Reason")
                .clickGoToStart()
                .inputText("Nothing much!")
                .swipeToEndScreen()
                .clickSaveAndExitWithChangesReasonPrompt()
                .closeSoftKeyboard()
                .pressBack(new FormEntryPage("Track Changes Reason"))
                .assertText(R.string.save_form_as);
    }

    @Test
    public void openingAFormToEdit_andChangingAValue_andClickingSaveAndExit_andClickingCross_returnsToForm() {
        rule.startAtMainMenu()
                .copyForm(TRACK_CHANGES_REASON_ON_EDIT_FORM)
                .startBlankForm("Track Changes Reason")
                .inputText("Nothing much...")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnForm("Track Changes Reason")
                .clickGoToStart()
                .inputText("Nothing much!")
                .swipeToEndScreen()
                .clickSaveAndExitWithChangesReasonPrompt()
                .closeSoftKeyboard()
                .pressClose(new FormEntryPage("Track Changes Reason"))
                .assertText(R.string.save_form_as);
    }

    @Test
    public void openingAFormToEdit_andChangingAValue_andClickingSaveAndExit_andRotating_remainsOnPrompt() {
        rule.startAtMainMenu()
                .copyForm(TRACK_CHANGES_REASON_ON_EDIT_FORM)
                .startBlankForm("Track Changes Reason")
                .inputText("Nothing much...")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnForm("Track Changes Reason")
                .clickGoToStart()
                .inputText("Nothing much!")
                .swipeToEndScreen()
                .clickSaveAndExitWithChangesReasonPrompt()
                .enterReason("Something")
                .rotateToLandscape(new ChangesReasonPromptPage("Track Changes Reason"))
                .assertText("Something")
                .closeSoftKeyboard()
                .clickSave();
    }

    @Test
    public void openingAFormToEdit_andChangingAValue_andPressingBack_andClickingSaveChanges_promptsForReason() {
        rule.startAtMainMenu()
                .copyForm(TRACK_CHANGES_REASON_ON_EDIT_FORM)
                .startBlankForm("Track Changes Reason")
                .inputText("Nothing much...")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnForm("Track Changes Reason")
                .clickGoToStart()
                .inputText("Nothing much!")
                .closeSoftKeyboard()
                .pressBack(new SaveOrIgnoreDialog<>("Track Changes Reason", new ChangesReasonPromptPage("Track Changes Reason")))
                .clickSaveChanges();
    }

    @Test
    public void openingAFormToEdit_andChangingAValue_andPressingBack_andIgnoringChanges_returnsToMainMenu() {
        rule.startAtMainMenu()
                .copyForm(TRACK_CHANGES_REASON_ON_EDIT_FORM)
                .startBlankForm("Track Changes Reason")
                .inputText("Nothing much...")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnForm("Track Changes Reason")
                .clickGoToStart()
                .inputText("Nothing much!")
                .closeSoftKeyboard()
                .pressBack(new SaveOrIgnoreDialog<>("Track Changes Reason", new MainMenuPage()))
                .clickIgnoreChanges();
    }

    @Test
    public void openingAFormToEdit_andNotChangingAValue_andClickingSaveAndExit_returnsToMainMenu() {
        rule.startAtMainMenu()
                .copyForm(TRACK_CHANGES_REASON_ON_EDIT_FORM)
                .startBlankForm("Track Changes Reason")
                .inputText("Nothing much...")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnForm("Track Changes Reason")
                .clickGoToStart()
                .closeSoftKeyboard()
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void openingFormToEdit_andChangingValue_andClickingSave_promptsForReason() {
        rule.startAtMainMenu()
                .copyForm(TRACK_CHANGES_REASON_ON_EDIT_FORM)
                .startBlankForm("Track Changes Reason")
                .inputText("Nothing much...")
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickEditSavedForm()
                .clickOnForm("Track Changes Reason")
                .clickGoToStart()
                .inputText("Nothing much!")
                .clickSaveWithChangesReasonPrompt()
                .enterReason("Bah")
                .clickSave(new FormEntryPage("Track Changes Reason"))
                .assertQuestion("What up?");
    }
}

package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.AdminSettingsPage;
import org.odk.collect.android.support.pages.ExitFormDialog;
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;

//Issue NODK-243
public class FormEntrySettingsTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
            .around(new ResetStateRule())
            .around(new CopyFormRule("All_widgets.xml"))
            .around(rule);

    @SuppressWarnings("PMD.AvoidCallingFinalize")
    @Test
    public void movingBackwards_shouldBeTurnedOn() {
        new MainMenuPage()
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .openFormManagement()
                .openConstraintProcessing()
                .clickOnString(R.string.constraint_behavior_on_finalize)
                .pressBack(new GeneralSettingsPage())
                .pressBack(new MainMenuPage())
                .openProjectSettingsDialog()
                .clickAdminSettings()
                .clickFormEntrySettings()
                .clickMovingBackwards()
                .assertText(R.string.moving_backwards_disabled_title)
                .assertText(R.string.yes)
                .assertText(R.string.no)
                .clickOnString(R.string.yes)
                .checkIfSaveFormOptionIsDisabled()
                .pressBack(new AdminSettingsPage())
                .pressBack(new MainMenuPage())
                .openProjectSettingsDialog()
                .clickGeneralSettings()
                .openFormManagement()
                .scrollToConstraintProcessing()
                .checkIfConstraintProcessingIsDisabled()
                .assertTextDoesNotExist(R.string.constraint_behavior_on_finalize)
                .assertText(R.string.constraint_behavior_on_swipe)
                .pressBack(new GeneralSettingsPage())
                .pressBack(new MainMenuPage())
                .checkIfElementIsGone(R.id.review_data)
                .startBlankForm("All widgets")
                .swipeToNextQuestion()
                .closeSoftKeyboard()
                .swipeToPreviousQuestion("String widget")
                .pressBack(new ExitFormDialog("All widgets"))
                .assertText(R.string.do_not_save)
                .assertTextDoesNotExist(R.string.keep_changes)
                .clickOnString(R.string.do_not_save);
    }
}

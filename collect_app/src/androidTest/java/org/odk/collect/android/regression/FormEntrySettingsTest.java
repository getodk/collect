package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.support.pages.AdminSettingsPage;
import org.odk.collect.android.support.pages.ExitFormDialog;
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;

//Issue NODK-243
public class FormEntrySettingsTest extends BaseRegressionTest {
    @Rule
    public RuleChain ruleChain = RuleChain
            .outerRule(new ResetStateRule());

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("All_widgets.xml"));

    @SuppressWarnings("PMD.AvoidCallingFinalize")
    @Test
    public void movingBackwards_shouldBeTurnedOn() {
        new MainMenuPage(rule)
                .clickOnMenu()
                .clickGeneralSettings()
                .openFormManagement()
                .openConstraintProcessing()
                .clickOnString(R.string.constraint_behavior_on_finalize)
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .clickOnMenu()
                .clickAdminSettings()
                .clickFormEntrySettings()
                .clickMovingBackwards()
                .checkIsStringDisplayed(R.string.moving_backwards_disabled_title)
                .checkIsStringDisplayed(R.string.yes)
                .checkIsStringDisplayed(R.string.no)
                .clickOnString(R.string.yes)
                .checkIfSaveFormOptionIsDisabled()
                .pressBack(new AdminSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .clickOnMenu()
                .clickGeneralSettings()
                .openFormManagement()
                .scrollToConstraintProcessing()
                .checkIfConstraintProcessingIsDisabled()
                .checkIfTextDoesNotExist(R.string.constraint_behavior_on_finalize)
                .checkIsStringDisplayed(R.string.constraint_behavior_on_swipe)
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))
                .checkIfElementIsGone(R.id.review_data)
                .startBlankForm("All widgets")
                .swipeToNextQuestion()
                .swipeToPreviousQuestion()
                .assertText("String widget")
                .closeSoftKeyboard()
                .pressBack(new ExitFormDialog("All widgets", rule))
                .checkIsStringDisplayed(R.string.do_not_save)
                .checkIfTextDoesNotExist(R.string.keep_changes)
                .clickOnString(R.string.do_not_save);
    }

}


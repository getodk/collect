package org.odk.collect.android.regression;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.android.support.pages.AccessControlPage;
import org.odk.collect.android.support.pages.ExitFormDialog;
import org.odk.collect.android.support.pages.ProjectSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;

//Issue NODK-243
public class FormEntrySettingsTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @SuppressWarnings("PMD.AvoidCallingFinalize")
    @Test
    public void movingBackwards_shouldBeTurnedOn() {
        rule.startAtMainMenu()
                .copyForm("All_widgets.xml")
                .openProjectSettingsDialog()
                .clickSettings()
                .openFormManagement()
                .openConstraintProcessing()
                .clickOnString(R.string.constraint_behavior_on_finalize)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())
                .openProjectSettingsDialog()
                .clickSettings()
                .clickAccessControl()
                .clickFormEntrySettings()
                .clickMovingBackwards()
                .assertText(R.string.moving_backwards_disabled_title)
                .assertText(R.string.yes)
                .assertText(R.string.no)
                .clickOnString(R.string.yes)
                .checkIfSaveFormOptionIsDisabled()
                .pressBack(new AccessControlPage())
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())
                .openProjectSettingsDialog()
                .clickSettings()
                .openFormManagement()
                .scrollToConstraintProcessing()
                .checkIfConstraintProcessingIsDisabled()
                .assertTextDoesNotExist(R.string.constraint_behavior_on_finalize)
                .assertText(R.string.constraint_behavior_on_swipe)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())
                .checkIfElementIsGone(R.id.review_data)
                .startBlankForm("All widgets")
                .swipeToNextQuestion("String widget")
                .closeSoftKeyboard()
                .swipeToPreviousQuestion("String widget")
                .pressBack(new ExitFormDialog("All widgets"))
                .assertText(R.string.do_not_save)
                .assertTextDoesNotExist(R.string.keep_changes)
                .clickOnString(R.string.do_not_save);
    }
}

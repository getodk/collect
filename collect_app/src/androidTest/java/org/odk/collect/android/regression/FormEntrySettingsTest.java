package org.odk.collect.android.regression;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.support.pages.AccessControlPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.ProjectSettingsPage;
import org.odk.collect.android.support.pages.SaveOrDiscardFormDialog;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

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
                .copyForm("all-widgets.xml")
                .openProjectSettingsDialog()
                .clickSettings()
                .openFormManagement()
                .openConstraintProcessing()
                .clickOnString(org.odk.collect.strings.R.string.constraint_behavior_on_finalize)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())
                .openProjectSettingsDialog()
                .clickSettings()
                .clickAccessControl()
                .clickFormEntrySettings()
                .clickMovingBackwards()
                .assertText(org.odk.collect.strings.R.string.moving_backwards_disabled_title)
                .assertText(org.odk.collect.strings.R.string.yes)
                .assertText(org.odk.collect.strings.R.string.no)
                .clickOnString(org.odk.collect.strings.R.string.yes)
                .assertSaveAsDraftInFormEntryDisabled()
                .pressBack(new AccessControlPage())
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())
                .openProjectSettingsDialog()
                .clickSettings()
                .openFormManagement()
                .scrollToConstraintProcessing()
                .checkIfConstraintProcessingIsDisabled()
                .assertTextDoesNotExist(org.odk.collect.strings.R.string.constraint_behavior_on_finalize)
                .assertText(org.odk.collect.strings.R.string.constraint_behavior_on_swipe)
                .pressBack(new ProjectSettingsPage())
                .pressBack(new MainMenuPage())
                .checkIfElementIsGone(R.id.review_data)
                .startBlankForm("All widgets")
                .swipeToNextQuestion("String widget")
                .closeSoftKeyboard()
                .swipeToPreviousQuestion("String widget")
                .pressBack(new SaveOrDiscardFormDialog<>(new MainMenuPage(), false))
                .assertText(org.odk.collect.strings.R.string.do_not_save)
                .assertTextDoesNotExist(org.odk.collect.strings.R.string.keep_changes)
                .clickOnString(org.odk.collect.strings.R.string.do_not_save);
    }
}

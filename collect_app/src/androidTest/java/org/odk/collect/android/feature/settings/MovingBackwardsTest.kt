package org.odk.collect.android.feature.settings

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.pages.AccessControlPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain

@RunWith(AndroidJUnit4::class)
class MovingBackwardsTest {
    private val rule = CollectTestRule()

    @get:Rule
    var ruleChain: RuleChain = TestRuleChain.chain().around(rule)

    @Test
    fun whenMovingBackwardDisabledWithPreventingUsersFormBypassingIt_relatedOptionsShouldBeUpdated() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .clickFormEntrySettings()
            .clickOnString(org.odk.collect.strings.R.string.finalize)

            // before disabling moving backward
            .pressBack(AccessControlPage())
            .pressBack(ProjectSettingsPage())
            .openFormManagement()
            .openConstraintProcessing()
            .clickOnString(org.odk.collect.strings.R.string.constraint_behavior_on_finalize)
            .pressBack(ProjectSettingsPage())

            .clickAccessControl()
            .clickFormEntrySettings()
            .assertGoToPromptEnabled()
            .assertGoToPromptChecked()

            .assertSaveAsDraftInFormEntryEnabled()
            .assertSaveAsDraftInFormEntryChecked()

            .assertSaveAsDraftInFormEndDisabled()
            .assertSaveAsDraftInFormEndChecked()

            .assertFinalizeEnabled()
            .assertFinalizeUnchecked()

            .clickMovingBackwards()
            .clickOnString(org.odk.collect.strings.R.string.yes)

            // after disabling moving backward - the state of the 4 related options is reversed
            .assertGoToPromptDisabled()
            .assertGoToPromptUnchecked()

            .assertSaveAsDraftInFormEntryDisabled()
            .assertSaveAsDraftInFormEntryUnchecked()

            .assertSaveAsDraftInFormEndDisabled()
            .assertSaveAsDraftInFormEndUnchecked()

            .assertFinalizeDisabled()
            .assertFinalizeChecked()

            .pressBack(AccessControlPage())
            .pressBack(ProjectSettingsPage())
            .openFormManagement()
            .scrollToConstraintProcessing()
            .checkIfConstraintProcessingIsDisabled()
            .assertTextDoesNotExist(org.odk.collect.strings.R.string.constraint_behavior_on_finalize)
            .assertText(org.odk.collect.strings.R.string.constraint_behavior_on_swipe)
    }

    @Test
    fun whenMovingBackwardDisabledWithoutPreventingUsersFormBypassingIt_relatedOptionsShouldNotBeUpdated() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .clickFormEntrySettings()
            .clickOnString(org.odk.collect.strings.R.string.finalize)

            // before disabling moving backward
            .pressBack(AccessControlPage())
            .pressBack(ProjectSettingsPage())
            .openFormManagement()
            .openConstraintProcessing()
            .clickOnString(org.odk.collect.strings.R.string.constraint_behavior_on_finalize)
            .pressBack(ProjectSettingsPage())

            .clickAccessControl()
            .clickFormEntrySettings()
            .assertGoToPromptEnabled()
            .assertGoToPromptChecked()

            .assertSaveAsDraftInFormEntryEnabled()
            .assertSaveAsDraftInFormEntryChecked()

            .assertSaveAsDraftInFormEndDisabled()
            .assertSaveAsDraftInFormEndChecked()

            .assertFinalizeEnabled()
            .assertFinalizeUnchecked()

            .clickMovingBackwards()
            .clickOnString(org.odk.collect.strings.R.string.no)

            // after disabling moving backward - the state of the 4 related options is reversed
            .assertGoToPromptEnabled()
            .assertGoToPromptChecked()

            .assertSaveAsDraftInFormEntryEnabled()
            .assertSaveAsDraftInFormEntryChecked()

            .assertSaveAsDraftInFormEndDisabled()
            .assertSaveAsDraftInFormEndChecked()

            .assertFinalizeEnabled()
            .assertFinalizeUnchecked()

            .pressBack(AccessControlPage())
            .pressBack(ProjectSettingsPage())
            .openFormManagement()
            .scrollToConstraintProcessing()
            .checkIfConstraintProcessingIsEnabled()
            .assertText(org.odk.collect.strings.R.string.constraint_behavior_on_finalize)
            .assertTextDoesNotExist(org.odk.collect.strings.R.string.constraint_behavior_on_swipe)
    }
}

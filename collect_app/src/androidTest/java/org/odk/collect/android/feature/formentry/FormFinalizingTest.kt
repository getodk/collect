package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.support.pages.AccessControlPage
import org.odk.collect.android.support.pages.FormEndPage
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.pages.ProjectSettingsPage
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

@RunWith(AndroidJUnit4::class)
class FormFinalizingTest {
    private val rule = CollectTestRule()

    @get:Rule
    val copyFormChain: RuleChain = chain().around(rule)

    @Test
    fun fillingForm_andPressingSaveAsDraft_doesNotFinalizesForm() {
        rule.startAtMainMenu()
            .copyForm(FORM)
            .assertNumberOfFinalizedForms(0)
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAsDraft()
            .assertNumberOfEditableForms(1)
            .assertNumberOfFinalizedForms(0)
    }

    @Test
    fun fillingForm_andPressingFinalize_finalizesForm() {
        rule.startAtMainMenu()
            .copyForm(FORM)
            .assertNumberOfFinalizedForms(0)
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickFinalize()
            .assertNumberOfEditableForms(1)
            .assertNumberOfFinalizedForms(1)
    }

    @Test
    fun fillingForm_andPressingBack_andPressingSave_doesNotFinalizesForm() {
        rule.startAtMainMenu()
            .copyForm(FORM)
            .assertNumberOfFinalizedForms(0)
            .startBlankForm("One Question")
            .closeSoftKeyboard()
            .pressBack(SaveOrIgnoreDialog("One Question", MainMenuPage()))
            .clickSaveChanges()
            .assertNumberOfEditableForms(1)
            .assertNumberOfFinalizedForms(0)
    }

    @Test
    fun disablingSaveAsDraftInSettings_disablesItInTheEndScreen() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .clickFormEntrySettings()
            .clickOnString(R.string.save_as_draft)
            .pressBack(AccessControlPage())
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .copyForm(FORM)
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAsDraft(FormEndPage("One Question"))
    }

    @Test
    fun disablingFinalizeInSettings_disablesItInTheEndScreen() {
        rule.startAtMainMenu()
            .openProjectSettingsDialog()
            .clickSettings()
            .clickAccessControl()
            .clickFormEntrySettings()
            .clickOnString(R.string.finalize)
            .pressBack(AccessControlPage())
            .pressBack(ProjectSettingsPage())
            .pressBack(MainMenuPage())
            .copyForm(FORM)
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickFinalize(FormEndPage("One Question"))
    }

    companion object {
        private const val FORM = "one-question.xml"
    }
}
